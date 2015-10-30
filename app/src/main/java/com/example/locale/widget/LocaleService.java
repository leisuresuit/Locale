package com.example.locale.widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.example.locale.R;
import com.example.locale.util.LocaleUtil;

import java.util.Locale;

public class LocaleService extends IntentService {
    public static final String ACTION_LOCALE_SERVICE = LocaleService.class.getName();
    public static final String EXTRA_LOCALE = "locale";
    public static final String EXTRA_IS_APP_WIDGET = "is_app_widget";
    public static final String EXTRA_SET_LOCALE_RESULT = "set_locale_result";

    public LocaleService() {
        super("LocaleService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Locale locale = (Locale) intent.getSerializableExtra(EXTRA_LOCALE);
        if (locale != null) {
            final Context context = LocaleService.this;
            final boolean success = LocaleUtil.setDefaultLocale(context, locale);
            if (success) {
                // Device seems to need to settle down after setting the locale,
                // or else a Toast isn't shown by the activity.
                try {
                    Thread.currentThread().sleep(250);
                } catch (InterruptedException e) {
                }
            }
            Intent responseIntent = new Intent();
            responseIntent.setAction(ACTION_LOCALE_SERVICE);
            responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
            responseIntent.putExtra(EXTRA_SET_LOCALE_RESULT, success);
            responseIntent.putExtra(EXTRA_LOCALE, locale);
            sendBroadcast(responseIntent);

            final boolean isAppWidget = intent.getBooleanExtra(EXTRA_IS_APP_WIDGET, false);
            if (isAppWidget) {
                Handler mainHandler = new Handler(getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(context, getString(R.string.app_widget_set_locale, locale.toString()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, R.string.app_widget_root_or_permission_required, Toast.LENGTH_LONG).show();
                        }
                    }
                };
                mainHandler.post(myRunnable);
            }
        }
    }
}
