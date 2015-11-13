package com.example.locale;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.locale.util.ImageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by larwang on 10/23/15.
 */
public class LocaleAdapter extends RecyclerView.Adapter<LocaleAdapter.LocaleViewHolder> {
    public interface LocaleListener {
        void onLocale(Locale locale);
    }

    private String mFilter;
    private Locale[] mLocales;
    private LocaleListener mListener;

    public LocaleAdapter() {
        this(null);
    }

    public LocaleAdapter(String filter) {
        init(TextUtils.isEmpty(filter) ? "" : filter);
    }

    public void setLocaleListener(LocaleListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mLocales.length;
    }

    @Override
    public LocaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.locale_item, parent, false);
        return new LocaleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LocaleViewHolder holder, int position) {
        final Locale locale = mLocales[position];
        holder.icon.setImageDrawable(ImageUtil.getFlagIcon(holder.icon.getContext(), locale.getCountry()));
        holder.name.setText(locale.getDisplayName());
        holder.nameNative.setText(locale.getDisplayName(locale));
        holder.locale.setText(locale.toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onLocale(locale);
                }
            }
        });
    }

    private void init(String f) {
        mFilter = f;

        Locale[] locales = Locale.getAvailableLocales();
        if (TextUtils.isEmpty(mFilter)) {
            mLocales = locales;
        } else {
            String filter = mFilter.toLowerCase();
            List<Locale> arrayList = new ArrayList<>();
            for (Locale locale : locales) {
                if (locale.getDisplayLanguage().toLowerCase().startsWith(filter) ||
                        locale.getDisplayCountry().toLowerCase().startsWith(filter) ||
                        locale.getCountry().equalsIgnoreCase(filter) ||
                        locale.getLanguage().equalsIgnoreCase(filter) ||
                        locale.toString().toLowerCase().startsWith(filter.toLowerCase())) {

                    arrayList.add(locale);
                }
            }
            Collections.sort(arrayList, new Comparator<Locale>() {
                @Override
                public int compare(Locale lhs, Locale rhs) {
                    return lhs.getDisplayName().compareTo(rhs.getDisplayName());
                }
            });
            mLocales = new Locale[arrayList.size()];
            arrayList.toArray(mLocales);
        }
    }

    public boolean setFilter(String filter) {
        boolean result;
        if (result = (!TextUtils.equals(mFilter, TextUtils.isEmpty(filter) ? "" : filter))) {
            init(filter);
        }
        return result;
    }

    static class LocaleViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.name_native) TextView nameNative;
        @Bind(R.id.locale) TextView locale;
        @Bind(R.id.remove_custom) View buttonRemove;

        LocaleViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

}
