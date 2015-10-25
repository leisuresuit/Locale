package com.example.locale.util;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;
import java.util.Locale;

/**
 * Created by larwang on 10/24/15.
 */
public class LocaleUtil {
    private static final String LOG_TAG = "LocaleUtil";

    private LocaleUtil() {}

    public static boolean setDefaultLocale(Context context, Locale locale) {
        boolean ret;

        if (!(ret = setDefaultLocale(locale))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (grantSuperUser(context)) {
                    ret = setDefaultLocale(locale);
                }
            }
        }

        return ret;
    }

    private static boolean setDefaultLocale(Locale locale) {
        boolean ret = false;
        IActivityManager am = ActivityManagerNative.getDefault();
        Configuration config;
        try {
            config = am.getConfiguration();
            config.locale = locale;
            am.updateConfiguration(config);

            // Trigger the dirty bit for the Settings Provider.
            BackupManager.dataChanged("com.android.providers.settings");
            ret = true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
        return ret;
    }

    private static boolean grantSuperUser(Context context) {
        boolean ret = false;
        DataOutputStream out = null;
        try {
            Process su = Runtime.getRuntime().exec("su");
            out = new DataOutputStream(su.getOutputStream());
            String cmd = "pm grant " + context.getApplicationContext().getPackageName() + " android.permission.CHANGE_CONFIGURATION\n";
            out.writeBytes(cmd);
            out.flush();
            out.writeBytes("exit\n");
            out.flush();
            int suResult = su.waitFor();
            ret = (suResult == 0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignored) {
                }
            }
        }

        return ret;
    }

}
