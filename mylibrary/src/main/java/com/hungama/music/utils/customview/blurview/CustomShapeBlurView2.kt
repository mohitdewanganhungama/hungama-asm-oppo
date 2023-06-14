package com.hungama.music.utils.customview.blurview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.hungama.music.utils.customview.blurview.CustomBlurView
import com.hungama.music.R

class CustomShapeBlurView2(context: Context?, attrs: AttributeSet?) : CustomBlurView(
    context!!, attrs
) {
    var mPaint: Paint
    var mRectF: RectF

    /**
     * Custom oval shape
     */
    override fun drawBlurredBitmap(
        canvas: Canvas,
        blurredBitmap: Bitmap?,
        overlayColor: Int,
        mTopLeftRadius: Float,
        mTopRightRadius: Float,
        mBottomLeftRadius: Float,
        mBottomRightRadius: Float
    ) {
        if (blurredBitmap != null) {
            mRectF.right = width.toFloat()
            mRectF.bottom = height.toFloat()
            mPaint.reset()
            mPaint.isAntiAlias = true
            val shader = BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val matrix = Matrix()
            matrix.postScale(
                mRectF.width() / blurredBitmap.width,
                mRectF.height() / blurredBitmap.height
            )
            shader.setLocalMatrix(matrix)
            mPaint.shader = shader
            canvas.drawRoundRect(
                mRectF,
                resources.getDimensionPixelSize(R.dimen.dimen_4).toFloat(),
                resources.getDimensionPixelSize(
                    R.dimen.dimen_4
                ).toFloat(),
                mPaint
            )
            mPaint.reset()
            mPaint.isAntiAlias = true
            mPaint.color = overlayColor
            canvas.drawRoundRect(
                mRectF,
                resources.getDimensionPixelSize(R.dimen.dimen_4).toFloat(),
                resources.getDimensionPixelSize(
                    R.dimen.dimen_4
                ).toFloat(),
                mPaint
            )
        }
    }

    init {
        mPaint = Paint()
        mRectF = RectF()
    }
}