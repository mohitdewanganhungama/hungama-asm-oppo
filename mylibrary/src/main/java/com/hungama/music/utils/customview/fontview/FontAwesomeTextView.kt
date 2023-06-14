package com.hungama.music.utils.customview.fontview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class FontAwesomeTextView : AppCompatTextView {
    /**
     * @param context context passed
     * *
     * @param attrs attributes in xml
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        typeface = FontAwesome.getTypeface(context)
    }

    /**
     * @param context context passed
     * *
     * @param attrs attributes in xml
     * *
     * @param defStyleAttr style attributes
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        typeface = FontAwesome.getTypeface(context)
    }

    /**
     * @param context context passed
     */
    constructor(context: Context) : super(context) {
        typeface = FontAwesome.getTypeface(context)
    }
}