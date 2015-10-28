package com.example.locale.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.locale.R;
import com.example.locale.util.ImageUtil;
import com.example.locale.util.LocaleUtil;

import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class LocaleAppWidgetProvider extends AppWidgetProvider {
    static final String PREFS = "prefs_app_widget";
    static final String LOCALE_LANGUAGE = "language";
    static final String LOCALE_COUNTRY = "country";
    private static final String ACTION_CLICKED = LocaleAppWidgetProvider.class.getName() + ".action_clicked";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = appWidgetIds.length - 1; i >= 0; i--) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        for (int i = appWidgetIds.length - 1; i >= 0; i--) {
            int appWidgetId = appWidgetIds[i];
            SharedPreferences prefs = context.getSharedPreferences(PREFS + appWidgetId, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(LOCALE_LANGUAGE);
            editor.remove(LOCALE_COUNTRY);
            editor.commit();
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS + appWidgetId, Context.MODE_PRIVATE);
        String lang = prefs.getString(LOCALE_LANGUAGE, null);
        String country = prefs.getString(LOCALE_COUNTRY, null);
        if (lang != null && country != null) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locale_app_widget);

            Locale locale = new Locale(lang, country);
            views.setImageViewResource(R.id.image, ImageUtil.getFlagIconId(context, country));
            views.setTextViewText(R.id.text, locale.toString());

            Intent clickIntent = new Intent(context, LocaleAppWidgetProvider.class);
            clickIntent.setAction(ACTION_CLICKED);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_CLICKED.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            SharedPreferences prefs = context.getSharedPreferences(LocaleAppWidgetProvider.PREFS + appWidgetId, Context.MODE_PRIVATE);
            String lang = prefs.getString(LocaleAppWidgetProvider.LOCALE_LANGUAGE, null);
            String country = prefs.getString(LocaleAppWidgetProvider.LOCALE_COUNTRY, null);
            if (lang != null && country != null) {
                Locale locale = new Locale(lang, country);
                if (LocaleUtil.setDefaultLocale(context, locale)) {
                    Toast.makeText(context, context.getString(R.string.app_widget_set_locale, locale.toString()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.app_widget_root_or_permission_required, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onReceive(context, intent);
        }
    }

}

