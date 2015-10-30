package com.example.locale.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.example.locale.R;

/**
 * Created by larwang on 10/24/15.
 */
public class ImageUtil {
    private ImageUtil() {}

    public static @DrawableRes int getFlagIconId(Context context, String countryCode) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier(countryCode.toLowerCase() + "_flag", "drawable", context.getPackageName());
        return (id > 0) ? id : R.mipmap.ic_launcher;
    }

    public static Drawable getFlagIcon(Context context, String countryCode) {
        return context.getResources().getDrawable(getFlagIconId(context, countryCode));
    }

}
