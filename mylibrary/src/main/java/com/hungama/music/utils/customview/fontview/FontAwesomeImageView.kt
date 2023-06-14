package com.hungama.music.utils.customview.fontview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.hungama.music.R

/**
 * DroidAwesomeImageView Class. An ImageView subclass with pre set TypeFace to FontAwesome
 */
class FontAwesomeImageView : AppCompatImageView {

    /**
     * @param context context passed
     */
    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    /**
     * @param context context passed
     * *
     * @param attrs attributes in xml
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    /**
     * @param context context passed
     * *
     * @param attrs attributes in xml
     * *
     * @param defStyleAttr style attributes
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    /**
     * @param context context passed
     * *
     * @param attrs attributes in xml
     * *
     * @param defStyleAttr style attributes
     * *
     * @param defStyleRes style resource
     * * init function to avoid repetition
     */
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DroidAwesomeTextDrawable, defStyleAttr, defStyleRes)
        try {
            val text = typedArray.getString(R.styleable.DroidAwesomeTextDrawable_fontText)
            val textSize = typedArray.getDimension(R.styleable.DroidAwesomeTextDrawable_fontTextSize, 14f)
            val textColor = typedArray.getColor(R.styleable.DroidAwesomeTextDrawable_fontTextColor, ContextCompat.getColor(context, R.color.colorWhite))
            val textDrawable = TextDrawable(context)
            textDrawable.text = text
            if (textColor != 0)
                textDrawable.setTextColor(textColor)
            else
                textDrawable.setTextColor(TextView(context).textColors)
            //textDrawable.setTextSize(Dimension.SP, textSize)
            textDrawable.setRawTextSize(textSize)
            textDrawable.typeface = FontAwesome.getTypeface(context)
            setImageDrawable(textDrawable)
        }catch (e:Exception){

        } finally {
            typedArray.recycle()
        }
    }

    /**
     * @param iconText FontAwesome icon text
     * *
     * @param iconColorRes set color to icon
     */
    fun setIcon(iconText: String, iconColorRes: Int) {
        setIcon(iconText, 0f, iconColorRes)
    }

    /**
     * @param iconText FontAwesome icon text
     * *
     * @param iconColorRes set color to icon
     * *
     * @param iconSizeSP set icon size
     */
    @JvmOverloads fun setIcon(iconText: String, iconSizeSP: Float = 0f, iconColorRes: Int = 0) {
        val textDrawable = TextDrawable(context)
        textDrawable.text = iconText
        if (iconColorRes != 0) {
            textDrawable.setTextColor(iconColorRes)
        } else {
            textDrawable.setTextColor(TextView(context).textColors)
        }
        if (iconSizeSP != 0f) {
            textDrawable.setTextSize(TypedValue.COMPLEX_UNIT_SP, iconSizeSP)
        }
        textDrawable.typeface = FontAwesome.getTypeface(context)
        setImageDrawable(textDrawable)
    }
}