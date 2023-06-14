package com.hungama.music.utils.customview.blurview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.hungama.music.utils.customview.blurview.CustomBlurView

class CustomShapeBlurView(context: Context?, attrs: AttributeSet?) : CustomBlurView(
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
            //canvas.drawRoundRect(mRectF,mLeftRadius, mLeftRadius, mPaint);
            //canvas.drawRoundRect(mLeftRadius, mTopRadius, mRightRadius, mBottomRadius, mLeftRadius,mLeftRadius,mPaint);
            val corners = floatArrayOf(
                mTopLeftRadius, mTopLeftRadius,  // Top left radius in px
                mTopRightRadius, mTopRightRadius,  // Top right radius in px
                mBottomRightRadius, mBottomRightRadius,  // Bottom right radius in px
                mBottomLeftRadius, mBottomLeftRadius // Bottom left radius in px
            )
            val path = Path()
            path.addRoundRect(mRectF, corners, Path.Direction.CW)
            canvas.drawPath(path, mPaint)
            mPaint.reset()
            mPaint.isAntiAlias = true
            mPaint.color = overlayColor
            //canvas.drawRoundRect(mRectF,mLeftRadius, mLeftRadius, mPaint);
            //canvas.drawRoundRect(mLeftRadius, mTopRadius, mRightRadius, mBottomRadius, mLeftRadius,mLeftRadius,mPaint);
            path.addRoundRect(mRectF, corners, Path.Direction.CW)
            canvas.drawPath(path, mPaint)
        }
    }

    init {
        mPaint = Paint()
        mRectF = RectF()
    }
}