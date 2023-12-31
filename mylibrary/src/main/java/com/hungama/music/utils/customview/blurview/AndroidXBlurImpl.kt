package com.hungama.music.utils.customview.blurview

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.renderscript.*

class AndroidXBlurImpl : BlurImpl {
    private var mRenderScript: RenderScript? = null
    private var mBlurScript: ScriptIntrinsicBlur? = null
    private var mBlurInput: Allocation? = null
    private var mBlurOutput: Allocation? = null
    override fun prepare(context: Context?, buffer: Bitmap?, radius: Float): Boolean {
        if (mRenderScript == null) {
            try {
                mRenderScript = RenderScript.create(context)
                mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript))
            } catch (e: RSRuntimeException) {
                return if (isDebug(context)) {
                    throw e
                } else {
                    // In release mode, just ignore
                    release()
                    false
                }
            }
        }
        mBlurScript!!.setRadius(radius)
        mBlurInput = Allocation.createFromBitmap(
            mRenderScript, buffer,
            Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT
        )
        mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput?.getType())
        return true
    }

    override fun release() {
        if (mBlurInput != null) {
            mBlurInput!!.destroy()
            mBlurInput = null
        }
        if (mBlurOutput != null) {
            mBlurOutput!!.destroy()
            mBlurOutput = null
        }
        if (mBlurScript != null) {
            mBlurScript!!.destroy()
            mBlurScript = null
        }
        if (mRenderScript != null) {
            mRenderScript!!.destroy()
            mRenderScript = null
        }
    }

    override fun blur(input: Bitmap?, output: Bitmap?) {
        mBlurInput!!.copyFrom(input)
        mBlurScript!!.setInput(mBlurInput)
        mBlurScript!!.forEach(mBlurOutput)
        mBlurOutput!!.copyTo(output)
    }

    companion object {
        // android:debuggable="true" in AndroidManifest.xml (auto set by build tool)
        var DEBUG: Boolean? = null
        fun isDebug(ctx: Context?): Boolean {
            if (DEBUG == null && ctx != null) {
                DEBUG = ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            }
            return DEBUG === java.lang.Boolean.TRUE
        }
    }
}