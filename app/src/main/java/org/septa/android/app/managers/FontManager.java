package org.septa.android.app.managers;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.septa.android.app.R;

public class FontManager {
    private static FontManager instance;
    private final Map<String, Typeface> typefaceCache;

    private FontManager() {
        typefaceCache = new HashMap<>();
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }

        return instance;
    }

    public Typeface getTypeface(Context context, int resId) {
        String customFontName = context.getString(resId);
        return getTypeface(context, customFontName);
    }

    public Typeface getTypeface(Context context, String customFontName) {
        Typeface typeface = typefaceCache.get(customFontName);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), customFontName);
            typefaceCache.put(customFontName, typeface);
        }
        return typeface;
    }

    public void setFont(TextView textView, AttributeSet attrs) {
        String customFontName = getFontName(textView.getContext(), attrs);
        setFont(textView, customFontName);
    }

    public void setFont(TextView textView, String customFontName) {
        if (customFontName == null || textView == null) {
            return;
        }

        Typeface typeface = typefaceCache.get(customFontName);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(textView.getContext().getAssets(), customFontName);
            typefaceCache.put(customFontName, typeface);
        }

        textView.setTypeface(typeface);
    }

    public static String getFontName(Context c, AttributeSet attrs) {
        TypedArray typedArray = c.obtainStyledAttributes(attrs, R.styleable.CustomFonts);
        String customFontName = typedArray.getString(R.styleable.CustomFonts_font);
        typedArray.recycle();
        return customFontName;
    }
}
