package com.hungama.music.utils.customview.blurview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import com.hungama.music.R
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.RuntimeException

/**
 * A realtime blurring overlay (like iOS UIVisualEffectView). Just put it above
 * the view you want to blur and it doesn't have to be in the same ViewGroup
 *
 *  * realtimeBlurRadius (10dp)
 *  * realtimeDownsampleFactor (4)
 *  * realtimeOverlayColor (#aaffffff)
 *
 */
open class CustomBlurView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mDownsampleFactor // default 4
            : Float = 4f
    private var mOverlayColor // default #aaffffff
            : Int = 0
    private var mBlurRadius // default 10dp (0 < r <= 25)
            : Float = 10f
    private var mBlurImpl: BlurImpl? = null
    private var mDirty = false
    private var mBitmapToBlur: Bitmap? = null
    private var mBlurredBitmap: Bitmap? = null
    private var mBlurringCanvas: Canvas? = null
    private var mIsRendering = false
    private var mPaint: Paint? = null
    private val mRectSrc = Rect()
    private val mRectDst = Rect()

    // mDecorView should be the root view of the activity (even if you are on a different window like a dialog)
    private var mDecorView: View? = null

    // If the view is on different root view (usually means we are on a PopupWindow),
    // we need to manually call invalidate() in onPreDraw(), otherwise we will not be able to see the changes
    private var mDifferentRoot = false
    private var mTopLeftRadius: Float = 7f
    private var mBottomLeftRadius: Float = 7f
    private var mBottomRightRadius: Float = 7f
    private var mTopRightRadius: Float = 7f // fallback to empty impl, which doesn't have blur effect// class not found or unsatisfied link// initialize RenderScript to load jni impl

    // may throw unsatisfied link error
// class not found or unsatisfied link// initialize RenderScript to load jni impl
    // may throw unsatisfied link error
    // try to use stock impl first
    private val blurImpl: BlurImpl
        get() {
            if (BLUR_IMPL == 0) {
                // try to use stock impl first
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    try {
                        val impl = AndroidStockBlurImpl()
                        val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                        impl.prepare(context, bmp, 4f)
                        impl.release()
                        bmp.recycle()
                        BLUR_IMPL = 3
                    } catch (e: Throwable) {
                    }
                }
            }
            if (BLUR_IMPL == 0) {
                try {
                    javaClass.classLoader.loadClass("androidx.renderscript.RenderScript")
                    // initialize RenderScript to load jni impl
                    // may throw unsatisfied link error
                    val impl = AndroidXBlurImpl()
                    val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                    impl.prepare(context, bmp, 4f)
                    impl.release()
                    bmp.recycle()
                    BLUR_IMPL = 1
                } catch (e: Throwable) {
                    // class not found or unsatisfied link
                }
            }
            if (BLUR_IMPL == 0) {
                try {
                    javaClass.classLoader.loadClass("android.support.v8.renderscript.RenderScript")
                    // initialize RenderScript to load jni impl
                    // may throw unsatisfied link error
                    val impl = SupportLibraryBlurImpl()
                    val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                    impl.prepare(context, bmp, 4f)
                    impl.release()
                    bmp.recycle()
                    BLUR_IMPL = 2
                } catch (e: Throwable) {
                    // class not found or unsatisfied link
                }
            }
            if (BLUR_IMPL == 0) {
                // fallback to empty impl, which doesn't have blur effect
                BLUR_IMPL = -1
            }
            return when (BLUR_IMPL) {
                1 -> AndroidXBlurImpl()
                2 -> SupportLibraryBlurImpl()
                3 -> AndroidStockBlurImpl()
                else -> EmptyBlurImpl()
            }
        }

    fun setBlurRadius(radius: Float) {
        if (mBlurRadius != radius) {
            mBlurRadius = radius
            mDirty = true
            invalidate()
        }
    }

    fun setDownsampleFactor(factor: Float) {
        require(factor > 0) { "Downsample factor must be greater than 0." }
        if (mDownsampleFactor != factor) {
            mDownsampleFactor = factor
            mDirty = true // may also change blur radius
            releaseBitmap()
            invalidate()
        }
    }

    fun setOverlayColor(color: Int) {
        if (mOverlayColor != color) {
            mOverlayColor = color
            invalidate()
        }
    }

    fun setTopLeftRadius(factor: Float) {
        require(factor > -1) { "TopRadius factor must be greater than -1." }
        if (mTopLeftRadius != factor) {
            mTopLeftRadius = factor
            invalidate()
        }
    }

    fun setTopRightRadius(factor: Float) {
        require(factor > -1) { "BottomRadius factor must be greater than -1." }
        if (mTopRightRadius != factor) {
            mTopRightRadius = factor
            invalidate()
        }
    }

    fun setBottomLeftRadius(factor: Float) {
        require(factor > -1) { "LeftRadius factor must be greater than -1." }
        if (mBottomLeftRadius != factor) {
            mBottomLeftRadius = factor
            invalidate()
        }
    }

    fun setBottomRightRadius(factor: Float) {
        require(factor > -1) { "RightRadius factor must be greater than -1." }
        if (mBottomRightRadius != factor) {
            mBottomRightRadius = factor
            invalidate()
        }
    }

    private fun releaseBitmap() {
        if (mBitmapToBlur != null) {
            mBitmapToBlur!!.recycle()
            mBitmapToBlur = null
        }
        if (mBlurredBitmap != null) {
            mBlurredBitmap!!.recycle()
            mBlurredBitmap = null
        }
    }

    protected fun release() {
        releaseBitmap()
        mBlurImpl?.release()
    }

    protected fun prepare(): Boolean {
        if (mBlurRadius == 0f) {
            release()
            return false
        }
        var downsampleFactor = mDownsampleFactor
        var radius = mBlurRadius / downsampleFactor
        if (radius > 25) {
            downsampleFactor = downsampleFactor * radius / 25
            radius = 25f
        }
        val width = width
        val height = height
        val scaledWidth = Math.max(1, (width / downsampleFactor).toInt())
        val scaledHeight = Math.max(1, (height / downsampleFactor).toInt())
        var dirty = mDirty
        if (mBlurringCanvas == null || mBlurredBitmap == null || mBlurredBitmap!!.width != scaledWidth || mBlurredBitmap!!.height != scaledHeight) {
            dirty = true
            releaseBitmap()
            var r = false
            try {
                mBitmapToBlur =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                if (mBitmapToBlur == null) {
                    return false
                }
                mBlurringCanvas = Canvas(mBitmapToBlur!!)
                mBlurredBitmap =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                if (mBlurredBitmap == null) {
                    return false
                }
                r = true
            } catch (e: OutOfMemoryError) {
                // Bitmap.createBitmap() may cause OOM error
                // Simply ignore and fallback
            } finally {
                if (!r) {
                    release()
                    return false
                }
            }
        }
        if (dirty) {
            mDirty = if (mBlurImpl?.prepare(context, mBitmapToBlur, radius) == true) {
                false
            } else {
                return false
            }
        }
        return true
    }

    protected fun blur(bitmapToBlur: Bitmap?, blurredBitmap: Bitmap?) {
        mBlurImpl?.blur(bitmapToBlur, blurredBitmap)
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        drawView()
        true
    }
    private fun drawView() {
            val locations = IntArray(2)
            var oldBmp = mBlurredBitmap
            var decor = mDecorView
            if (decor != null && isShown && prepare()) {
                val redrawBitmap = mBlurredBitmap != oldBmp
                oldBmp = null
                decor.getLocationOnScreen(locations)
                var x = -locations[0]
                var y = -locations[1]
                getLocationOnScreen(locations)
                x += locations[0]
                y += locations[1]

                // just erase transparent
                mBitmapToBlur!!.eraseColor(mOverlayColor and 0xffffff)
                val rc = mBlurringCanvas!!.save()
                mIsRendering = true
                RENDERING_COUNT++
                try {
                    mBlurringCanvas!!.scale(
                        1f * mBitmapToBlur!!.width / width,
                        1f * mBitmapToBlur!!.height / height
                    )
                    mBlurringCanvas!!.translate(-x.toFloat(), -y.toFloat())
                    if (decor.background != null) {
                        decor.background.draw(mBlurringCanvas!!)
                    }
                    decor.draw(mBlurringCanvas)
                } catch (e: StopException) {
                } catch (e: Exception) {
                } finally {
                    decor=null
                    mIsRendering = false
                    RENDERING_COUNT--
                    mBlurringCanvas!!.restoreToCount(rc)
                }
                try {
                    blur(mBitmapToBlur, mBlurredBitmap)
                } catch (e: Exception) {
                }
                if (redrawBitmap || mDifferentRoot) {
                    invalidate()
                }
            }
    }
    private val activityDecorView: View?
        get() {
            var ctx = context
            var i = 0
            while (i < 4 && ctx != null && ctx !is Activity && ctx is ContextWrapper) {
                ctx = ctx.baseContext
                i++
            }
            return if (ctx is Activity) {
                ctx.window.decorView
            } else {
                null
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mDecorView = activityDecorView
        if (mDecorView != null) {
            mDecorView!!.viewTreeObserver.addOnPreDrawListener(preDrawListener)
            mDifferentRoot = mDecorView!!.rootView !== rootView
            if (mDifferentRoot) {
                mDecorView!!.postInvalidate()
            }
        } else {
            mDifferentRoot = false
        }
    }

    override fun onDetachedFromWindow() {
        if (mDecorView != null) {
            mDecorView!!.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
        }
        release()
        super.onDetachedFromWindow()
    }

    override fun draw(canvas: Canvas) {
        if (mIsRendering) {
            // Quit here, don't draw views above me
            throw STOP_EXCEPTION
        } else if (RENDERING_COUNT > 0) {
            // Doesn't support blurview overlap on another blurview
        } else {
            super.draw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBlurredBitmap(
            canvas,
            mBlurredBitmap,
            mOverlayColor,
            mTopLeftRadius,
            mTopRightRadius,
            mBottomLeftRadius,
            mBottomRightRadius
        )
    }

    /**
     * Custom draw the blurred bitmap and color to define your own shape
     * @param canvas
     * @param blurredBitmap
     * @param overlayColor
     * @param mTopLeftRadius
     * @param mTopRightRadius
     * @param mBottomLeftRadius
     * @param mBottomRightRadius
     */
    protected open fun drawBlurredBitmap(
        canvas: Canvas,
        blurredBitmap: Bitmap?,
        overlayColor: Int,
        mTopLeftRadius: Float,
        mTopRightRadius: Float,
        mBottomLeftRadius: Float,
        mBottomRightRadius: Float
    ) {
        if (blurredBitmap != null) {
            mRectSrc.right = blurredBitmap.width
            mRectSrc.bottom = blurredBitmap.height
            mRectDst.right = width
            mRectDst.bottom = height
            canvas.drawBitmap(blurredBitmap, mRectSrc, mRectDst, null)
        }
        mPaint?.color = overlayColor
        if (mPaint != null) {
            canvas.drawRect(mRectDst, mPaint!!)
        }
    }

    private class StopException : RuntimeException()
    companion object {
        private var RENDERING_COUNT = 0
        private var BLUR_IMPL = 0
        private val STOP_EXCEPTION = StopException()
    }

    init {
        mBlurImpl = blurImpl // provide your own by override getBlurImpl()
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomBlurView)
        mBlurRadius = a.getDimension(
            R.styleable.CustomBlurView_realtimeBlurRadius,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10f,
                context.resources.displayMetrics
            )
        )
        mDownsampleFactor = a.getFloat(R.styleable.CustomBlurView_realtimeDownsampleFactor, 4f)
        mOverlayColor = a.getColor(R.styleable.CustomBlurView_realtimeOverlayColor, -0x55000001)
        mTopLeftRadius = a.getDimension(
            R.styleable.CustomBlurView_topLeftRadius, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 7f, context.resources.displayMetrics
            )
        )
        mTopRightRadius = a.getDimension(
            R.styleable.CustomBlurView_topRightRadius, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 7f, context.resources.displayMetrics
            )
        )
        mBottomLeftRadius = a.getDimension(
            R.styleable.CustomBlurView_bottomLeftRadius, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 7f, context.resources.displayMetrics
            )
        )
        mBottomRightRadius = a.getDimension(
            R.styleable.CustomBlurView_bottomRightRadius, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 7f, context.resources.displayMetrics
            )
        )
        a.recycle()
        mPaint = Paint()
    }
}