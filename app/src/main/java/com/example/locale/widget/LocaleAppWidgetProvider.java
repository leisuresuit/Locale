package com.example.locale.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.locale.R;
import com.example.locale.util.ImageUtil;

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

            Intent clickIntent = new Intent(context, LocaleService.class);
            clickIntent.putExtra(LocaleService.EXTRA_LOCALE, locale);
            clickIntent.putExtra(LocaleService.EXTRA_IS_APP_WIDGET, true);
            PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetId, clickIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}

