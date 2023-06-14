package com.hungama.music.utils.fontmanger;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.hungama.music.R;

public class FontTextView extends AppCompatTextView {
    private boolean isBrandingIcon, isSolidIcon;

    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FontTextView,
                0, 0);
        isSolidIcon = a.getBoolean(R.styleable.FontTextView_solid_icon, false);
        isBrandingIcon = a.getBoolean(R.styleable.FontTextView_brand_icon, false);
        init();
    }

    private void init() {
        if (isBrandingIcon)
            setTypeface(FontCache.get(getContext(), "icomoon.ttf"));
        else if (isSolidIcon)
            setTypeface(FontCache.get(getContext(), "icomoon.ttf"));
        else
            setTypeface(FontCache.get(getContext(), "icomoon.ttf"));
    }
}
