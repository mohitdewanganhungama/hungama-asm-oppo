package com.hungama.music.utils.customview.bottomsheet

import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt

internal fun hasMinimumSdk(minimumSdk: Int) = Build.VERSION.SDK_INT >= minimumSdk

internal fun hasMaximumSdk(maximumSdk: Int) = Build.VERSION.SDK_INT <= maximumSdk

@ColorInt
internal fun calculateColor(@ColorInt to: Int, ratio: Float): Int {
    val alpha = (MAX_ALPHA - (MAX_ALPHA * ratio)).toInt()
    return Color.argb(alpha, Color.red(to), Color.green(to), Color.blue(to))
}