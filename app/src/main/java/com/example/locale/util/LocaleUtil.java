package com.example.locale.util;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by larwang on 10/24/15.
 */
public class LocaleUtil {
    private static final String LOG_TAG = "LocaleUtil";
    private static final String FILE_CUSTOM_LOCALES = "custom_locales";

    private static final List<Locale> mCustomLocales = new ArrayList<>();

    private LocaleUtil() {}

    public static void initCustomLocales(Context context) {
        mCustomLocales.clear();
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = context.openFileInput(FILE_CUSTOM_LOCALES);
            is = new ObjectInputStream(fis);

            Locale locale = null;
            do {
                try {
                    locale = (Locale) is.readObject();
                    if (locale != null) {
                        mCustomLocales.add(locale);
                    }
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
            while (locale != null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static int addCustomLocale(Context context, Locale locale) {
        mCustomLocales.add(locale);
        saveCustomLocales(context);
        return mCustomLocales.size() - 1;
    }

    public static int removeCustomLocale(Context context, Locale locale) {
        int position = -1;
        Locale[] customLocales = LocaleUtil.getCustomLocales();
        for (int i = customLocales.length - 1; i >= 0; i--) {
            if (customLocales[i].equals(locale)) {
                position = i;
                mCustomLocales.remove(i);
                break;
            }
        }
        saveCustomLocales(context);
        return position;
    }

    private static void saveCustomLocales(Context context) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            fos = context.openFileOutput(FILE_CUSTOM_LOCALES, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            for (Locale locale : mCustomLocales) {
                os.writeObject(locale);
            }
            os.writeObject(null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static Locale[] getCustomLocales() {
        return mCustomLocales.toArray(new Locale[mCustomLocales.size()]);
    }

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
