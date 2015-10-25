package com.example.locale;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.supportv7.widget.decorator.DividerItemDecoration;
import com.example.locale.util.ImageUtil;
import com.example.locale.util.LocaleUtil;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocaleAdapter.LocaleListener, SearchView.OnQueryTextListener {
    private static final String FILTER = "filter";

    private LocaleAdapter mAdapter;
    private Toolbar mToolbar;
    private ContentLoadingProgressBar mLoading;
    private AsyncTask mPaletteTask;
    private String mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initToolbar();

        mLoading = (ContentLoadingProgressBar) findViewById(R.id.loading);
        mLoading.hide();

        mFilter = (savedInstanceState != null) ? savedInstanceState.getString(FILTER) : null;
        mAdapter = new LocaleAdapter(mFilter);
        mAdapter.setLocaleListener(this);

        initRecyclerView();

        showDefaultLocale();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FILTER, mFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQuery(mFilter, false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mPaletteTask != null) {
            mPaletteTask.cancel(true);
            mPaletteTask = null;
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!TextUtils.equals(newText, mFilter)) {
            mLoading.show();

            mFilter = newText;
            AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mAdapter.setFilter(mFilter);
                    return null;
                }

                @Override
                protected void onPostExecute(Void ignored) {
                    mLoading.hide();

                    mAdapter.notifyDataSetChanged();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // no-op
        return true;
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLocale(final Locale locale) {
        if (Locale.getDefault().equals(locale)) {
            return;
        }

        mLoading.show();

        mFilter = null;
        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean result = LocaleUtil.setDefaultLocale(MainActivity.this, locale);
                if (result) {
                    mAdapter.setFilter(null);
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mLoading.hide();

                if (result) {
                    Locale.setDefault(locale);
                    showDefaultLocale();

                    Configuration config = new Configuration();
                    config.locale = locale;
                    Resources resources = getResources();
                    resources.updateConfiguration(config, resources.getDisplayMetrics());

                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, getString(R.string.app_widget_set_locale, locale.toString()), Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(getString(R.string.root_or_permission_required, getApplicationContext().getPackageName()))
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            }
        });
    }

    private void showDefaultLocale() {
        Locale locale = Locale.getDefault();
        setTitle(locale.getDisplayName());
        BitmapDrawable d = (BitmapDrawable) ImageUtil.getFlagIcon(this, locale.getCountry());
        if (d != null) {
            final Bitmap bitmap = d.getBitmap();
            mPaletteTask = new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    mPaletteTask = null;
                    int color = palette.getDarkVibrantColor(0);
                    mToolbar.setBackgroundColor(color);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getDarkerColor(color));
                    }
                }
            });
        }
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

}
