package com.hungama.music.utils.customview.stories

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.hungama.music.R
import com.hungama.music.ui.main.view.fragment.StoryDisplayFragment
import com.hungama.music.utils.CommonUtils
import java.util.ArrayList

class StoriesProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val progressBars: MutableList<PausableProgressBar> = ArrayList()
    private var storiesListener: StoriesListener? = null
    private var storiesCount = -1
    private var current = -1
    private var isSkipStart = false
    private var isReverseStart = false
    private var position = -1
    private var isComplete = false

    init {
        orientation = HORIZONTAL
        val typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.StoriesProgressView
        )
        storiesCount = typedArray.getInt(R.styleable.StoriesProgressView_progressCount, 0)
        typedArray.recycle()
        bindViews()
    }

    private fun bindViews() {
        progressBars.clear()
        removeAllViews()
        for (i in 0 until storiesCount) {
            val p = createProgressBar()
            p.tag = "p($position) c($i)" // debug
            progressBars.add(p)
            addView(p)
            if (i + 1 < storiesCount) {
                addView(createSpace())
            }
        }
        CommonUtils.setLog("StoryView", "bindViews position :${position} progressBars size:${progressBars?.size} storiesCount:${storiesCount}")
    }

    private fun createProgressBar(): PausableProgressBar {
        return PausableProgressBar(context).apply {
            layoutParams =
                PROGRESS_BAR_LAYOUT_PARAM
        }
    }

    private fun createSpace(): View {
        return View(context).apply {
            layoutParams =
                SPACE_LAYOUT_PARAM
        }
    }

    private fun callback(index: Int): PausableProgressBar.Callback {
        return object : PausableProgressBar.Callback {
            override fun onStartProgress() {
                current = index
                CommonUtils.setLog("StoryView", "onStartProgress-isReverseStart- position :${position} ${isReverseStart}-current-${current} progressBars size:${progressBars?.size}")
            }

            override fun onFinishProgress() {
                CommonUtils.setLog("StoryView", "onFinishProgress- position :${position}isReverseStart-${isReverseStart}-current-${current} progressBars size:${progressBars?.size}")
                if (isReverseStart) {
                    if (storiesListener != null) storiesListener?.onPrev()
                    if (0 <= current - 1) {
                        val p = progressBars[current - 1]
                        CommonUtils.setLog("StoryView", "onFinishProgress22- position :${position}  current-${current} progressBars size:${progressBars?.size}")
                        p.setMinWithoutCallback()
                        progressBars[--current].startProgress()
                    } else {
                        CommonUtils.setLog("StoryView", "onFinishProgress23- position :${position} current-${current} progressBars size:${progressBars?.size}")
                        progressBars[current].startProgress()
                    }
                    isReverseStart = false
                    return
                }
                CommonUtils.setLog("StoryView", "onFinishProgress------ position :${position} current-${current} progressBars size:${progressBars?.size} name:${StoryDisplayFragment.stories?.name?.get(0)?.en}")
                val next = current + 1
                if (next <= progressBars.size - 1) {
                    CommonUtils.setLog("StoryView", "onFinishProgress22-onNext current-${current} progressBars size:${progressBars?.size}")
                    if (storiesListener != null) storiesListener?.onNext()
                    progressBars[next].startProgress()
                    ++current
                } else {
                    isComplete = true
                    CommonUtils.setLog("StoryView", "onFinishProgress22-onComplete current-${current} progressBars size:${progressBars?.size}")
                    if (storiesListener != null) storiesListener?.onComplete()
                }
                isSkipStart = false
            }
        }
    }

    fun setStoriesCountDebug(storiesCount: Int, position: Int) {
        this.storiesCount = storiesCount
        this.position = position
        bindViews()

        CommonUtils.setLog("StoryView", "setStoriesCountDebug bindViews position:${position} storiesCount:${storiesCount}")
    }

    fun setStoriesListener(storiesListener: StoriesListener?) {
        this.storiesListener = storiesListener
    }

    fun skip() {
        CommonUtils.setLog("StoryView", "skip")
        if (isSkipStart || isReverseStart) return
        if (isComplete) return
        if (current < 0) return
        val p = progressBars[current]
        isSkipStart = true
        p.setMax()
    }

    fun reverse() {
        CommonUtils.setLog("StoryView", "reverse-isSkipStart-$isSkipStart-isReverseStart-$isReverseStart-isComplete-$isComplete-current-$current")
        if (isSkipStart || isReverseStart) return
        if (isComplete) return
        if (current < 0) return
        val p = progressBars[current]
        isReverseStart = true
        p.setMin()
    }

    fun setAllStoryDuration(duration: Long) {
        CommonUtils.setLog("StoryView", "setAllStoryDuration()--$duration")

        for (i in progressBars.indices) {
            CommonUtils.setLog("StoryView", "setAllStoryDuration() i--${i} size:${progressBars.indices}")
            //progressBars[i].setDuration(duration)
            progressBars[i].setCallback(callback(i))
        }
    }

    fun startStories() {
        CommonUtils.setLog("StoryView", "startStories()")
        if (progressBars.size > 0) {
            progressBars[0].startProgress()
        }
    }

    fun startStories(from: Int) {
        CommonUtils.setLog("StoryView", "startStories(from: Int)-clear()")
        for (i in progressBars.indices) {
            progressBars[i].clear()
        }
        CommonUtils.setLog("StoryView", "startStories(from: Int)-setMaxWithoutCallback()")
        for (i in 0 until from) {
            if (progressBars.size > i) {
                progressBars[i].setMaxWithoutCallback()
            }
        }
        CommonUtils.setLog("StoryView", "startStories(from: Int)-startProgress()")
        if (progressBars.size > from) {
            //progressBars[from].startProgress()
        }
    }

    fun destroy() {
        for (p in progressBars) {
            CommonUtils.setLog("StoryView", "destroy()")
            p.clear()
        }
    }

    fun abandon() {
        if (!progressBars.isNullOrEmpty() && progressBars.size > current && current >= 0) {
            CommonUtils.setLog("StoryView", "abandon()")
            progressBars[current].setMinWithoutCallback()
        }
    }

    fun pause() {
        CommonUtils.setLog("StoryView", "pause(0)-current-$current")
        if (current < 0) return

        if (current >= 0 && !progressBars.isNullOrEmpty() && progressBars.size > current) {
            CommonUtils.setLog("StoryView", "pause(1)")
            progressBars[current].pauseProgress()
        }
    }

    fun resume() {
        CommonUtils.setLog("StoryView", "resume(0)-current-$current")
        if (current < 0 && !progressBars.isNullOrEmpty() && progressBars.size > 0) {
            CommonUtils.setLog("StoryView", "resume(1)")
            progressBars[0].startProgress()
            return
        }
        if (current >= 0 && !progressBars.isNullOrEmpty() && progressBars.size > current) {
            CommonUtils.setLog("StoryView", "resume(2)")
            progressBars[current].resumeProgress()
        }

    }

    fun getProgressWithIndex(index: Int): PausableProgressBar? {
        try {
            if (!progressBars.isNullOrEmpty() && progressBars.size > index){
                return progressBars[index]
            }else {
                return null
            }
        }catch (e:Exception){
            return null
        }
    }

    companion object {
        private val PROGRESS_BAR_LAYOUT_PARAM = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1F)
        private val SPACE_LAYOUT_PARAM = LayoutParams(5, LayoutParams.WRAP_CONTENT)
    }

    interface StoriesListener {
        fun onNext()
        fun onPrev()
        fun onComplete()
    }
}