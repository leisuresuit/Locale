package com.example.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.supportv7.widget.decorator.DividerItemDecoration;
import com.example.locale.util.ImageUtil;
import com.example.locale.util.LocaleUtil;
import com.example.locale.widget.LocaleService;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements LocaleAdapter.LocaleListener, SearchView.OnQueryTextListener, View.OnClickListener {

    private static final String FILTER = "filter";

    private LocaleAdapter mAdapter;
    private Toolbar mToolbar;
    @Bind(R.id.loading) ContentLoadingProgressBar mLoading;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    private AsyncTask mPaletteTask;
    private String mFilter;
    private LocaleBroadcastReceiver mLocaleBroadcastReceiver;
    private SearchView mSearchView;
    @Bind(R.id.add_locale) FloatingActionButton mAddLocale;

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (mSearchView != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }

            if (dy > 0) {
                mAddLocale.hide();
            } else {
                mAddLocale.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocaleUtil.initCustomLocales(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();

        mAddLocale.setOnClickListener(this);

        mLoading.hide();

        mFilter = (savedInstanceState != null) ? savedInstanceState.getString(FILTER) : null;
        mAdapter = new LocaleAdapter(mFilter);
        mAdapter.setLocaleListener(this);

        initRecyclerView();

        showDefaultLocale();

        mLocaleBroadcastReceiver = new LocaleBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(LocaleService.ACTION_LOCALE_SERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mLocaleBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FILTER, mFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchView.setQuery(mFilter, false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_licenses:
                showLicenses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mLocaleBroadcastReceiver);

        mRecyclerView.removeOnScrollListener(mScrollListener);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_locale:
                final View view = getLayoutInflater().inflate(R.layout.add_locale, null);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.add_locale)
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CharSequence language = ((EditText) view.findViewById(R.id.language_code)).getText();
                                CharSequence country = ((EditText) view.findViewById(R.id.country_code)).getText();
                                if (!TextUtils.isEmpty(language) && !TextUtils.isEmpty(country)) {
                                    addCustomLocale(new Locale(language.toString(), country.toString()));
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
        }
    }

    private void addCustomLocale(Locale locale) {
        int position = mAdapter.addCustomLocale(this, locale);
        mRecyclerView.scrollToPosition(position);
    }

    private void showLicenses() {
        WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.licenses_title)
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initRecyclerView() {
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLocale(final Locale locale) {
        if (Locale.getDefault().equals(locale)) {
            Toast.makeText(MainActivity.this, getString(R.string.app_widget_set_locale, locale.toString()), Toast.LENGTH_SHORT).show();
            return;
        }

        mLoading.show();
        mFilter = null;
        Intent intent = new Intent(this, LocaleService.class);
        intent.putExtra(LocaleService.EXTRA_LOCALE, locale);
        startService(intent);
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

    private class LocaleBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            boolean result = intent.getBooleanExtra(LocaleService.EXTRA_SET_LOCALE_RESULT, false);
            if (result) {
                final Locale locale = (Locale) intent.getSerializableExtra(LocaleService.EXTRA_LOCALE);
                AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        mAdapter.setFilter(null);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Locale.setDefault(locale);
                        showDefaultLocale();

                        Configuration config = new Configuration();
                        config.locale = locale;
                        Resources resources = getResources();
                        resources.updateConfiguration(config, resources.getDisplayMetrics());

                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, getString(R.string.app_widget_set_locale, locale.toString()), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getString(R.string.root_or_permission_required, getApplicationContext().getPackageName()))
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

}
