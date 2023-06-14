package com.hungama.music.utils.customview.stories

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import com.hungama.music.utils.PausableScaleAnimation

class PausableProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var frontProgressView: View? = null
    private var maxProgressView: View? = null
    private var animation: PausableScaleAnimation? = null
    private var duration = Constant.DEFAULT_PROGRESS_DURATION.toLong()
    private var callback: Callback? = null
    private var isStarted = false

    init {
        LayoutInflater.from(context).inflate(R.layout.pausable_progress, this)
        frontProgressView = findViewById(R.id.front_progress)
        maxProgressView = findViewById(R.id.max_progress)
    }

    fun setDuration(duration: Long, isNewDuration:Boolean = false) {
        this.duration = duration
        CommonUtils.setLog("StoryView", "setDuration-"+(this.duration).toString())
        if (animation != null){
            animation = null
            startProgress(isNewDuration)
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setMax() {
        finishProgress(true)
    }

    fun setMin() {
        finishProgress(false)
    }

    fun setMinWithoutCallback() {
        maxProgressView?.setBackgroundResource(R.color.progress_secondary)
        maxProgressView?.visibility = View.VISIBLE
        CommonUtils.setLog("StoryView", "setMinWithoutCallback-animation-$animation")
        if (animation != null) {
            CommonUtils.setLog("StoryView", "setMinWithoutCallback-"+(duration).toString())
            animation?.setAnimationListener(null)
            animation?.cancel()
        }
    }

    fun setMaxWithoutCallback() {
        maxProgressView?.setBackgroundResource(R.color.progress_max_active)
        maxProgressView?.visibility = View.VISIBLE
        if (animation != null) {
            CommonUtils.setLog("StoryView", "setMaxWithoutCallback-"+(duration).toString())
            animation?.setAnimationListener(null)
            animation?.cancel()
        }
    }

    private fun finishProgress(isMax: Boolean) {
        if (isMax) maxProgressView?.setBackgroundResource(R.color.progress_max_active)
        maxProgressView?.visibility = if (isMax) View.VISIBLE else View.GONE
        if (animation != null) {
            animation?.setAnimationListener(null)
            animation?.cancel()
            if (callback != null) {
                CommonUtils.setLog("StoryView", "finishProgress-"+(duration).toString())
                callback?.onFinishProgress()
            }
        }
    }

    fun startProgress(isNewDuration:Boolean = false) {
        if (isNewDuration){
            CommonUtils.setLog("StoryView", "startProgress(1)-"+(duration).toString())
            clear()
            isStarted = false
        }
        maxProgressView?.visibility = View.GONE
        CommonUtils.setLog("StoryView", "startProgress(2)-"+(duration).toString())

        if (duration <= 0) duration = Constant.DEFAULT_PROGRESS_DURATION

        CommonUtils.setLog("StoryView", "startProgress(3)-"+(duration).toString())
        animation =
            PausableScaleAnimation(
                0f,
                1f,
                1f,
                1f,
                Animation.ABSOLUTE,
                0f,
                Animation.RELATIVE_TO_SELF,
                0f
            )
        animation?.duration = duration
        animation?.interpolator = LinearInterpolator()
        animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (isStarted) {
                    //CommonUtils.setLog("StoryView", "onAnimationStart-isStarted-"+(animation.duration).toString())
                    return
                }
                isStarted = true
                frontProgressView?.visibility = View.VISIBLE
                CommonUtils.setLog("StoryView", "onAnimationStart-"+(animation.duration).toString())
                if (callback != null) callback?.onStartProgress()
            }

            override fun onAnimationEnd(animation: Animation) {
                isStarted = false
                CommonUtils.setLog("StoryView", "onAnimationEnd-"+(animation.duration).toString())
                if (callback != null) callback?.onFinishProgress()
            }

            override fun onAnimationRepeat(animation: Animation) {
                //NO-OP
            }
        })
        animation?.fillAfter = true
        frontProgressView?.startAnimation(animation)
    }

    fun pauseProgress() {
        if (animation != null) {
            animation?.pause()
        }
    }

    fun resumeProgress() {
        if (animation != null) {
            animation?.resume()
        }
    }

    fun clear() {
        if (animation != null) {
            animation?.setAnimationListener(null)
            animation?.cancel()
            animation = null
        }
    }

    interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }
}