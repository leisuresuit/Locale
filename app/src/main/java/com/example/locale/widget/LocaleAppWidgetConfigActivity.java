package com.example.locale.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.locale.MainActivity;
import com.example.locale.R;

import java.util.Locale;

/**
 * Created by larwang on 10/27/15.
 */
public class LocaleAppWidgetConfigActivity extends MainActivity {
    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppWidgetId = getIntent().getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        setTitle(R.string.app_widget_config);
    }

    @Override
    public void onLocale(final Locale locale) {
        SharedPreferences prefs = getSharedPreferences(LocaleAppWidgetProvider.PREFS + mAppWidgetId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LocaleAppWidgetProvider.LOCALE_LANGUAGE, locale.getLanguage());
        editor.putString(LocaleAppWidgetProvider.LOCALE_COUNTRY, locale.getCountry());
        editor.commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        LocaleAppWidgetProvider.updateAppWidget(this, appWidgetManager, mAppWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
