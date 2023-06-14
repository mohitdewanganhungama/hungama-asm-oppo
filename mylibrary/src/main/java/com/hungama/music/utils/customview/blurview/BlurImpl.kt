package com.hungama.music.utils.customview.blurview

import android.content.Context
import android.graphics.Bitmap

internal interface BlurImpl {
    fun prepare(context: Context?, buffer: Bitmap?, radius: Float): Boolean
    fun release()
    fun blur(input: Bitmap?, output: Bitmap?)
}