package com.hungama.music.data.model

import android.os.Handler
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.hungama.music.utils.CommonUtils.setLog

abstract class PageChangeListener : ViewPager.OnPageChangeListener {

    private var pageBeforeDragging = 0
    private var currentPage = 0
    private var lastTime = DEBOUNCE_TIMES + 1L

    override fun onPageScrollStateChanged(state: Int) {
        when (state) {
            ViewPager.SCROLL_STATE_IDLE -> {
                setLog("onPageScrollState"," SCROLL_STATE_IDLE")
                val now = System.currentTimeMillis()
                if (now - lastTime < DEBOUNCE_TIMES) {
                    return
                }
                lastTime = now
                Handler().postDelayed({
                    if (pageBeforeDragging == currentPage) {
                        onPageScrollCanceled()
                    }
                }, 300L)
            }
            ViewPager.SCROLL_STATE_DRAGGING -> {
                setLog("onPageScrollState"," SCROLL_STATE_DRAGGING")
                pageBeforeDragging = currentPage
            }
            ViewPager.SCROLL_STATE_SETTLING -> {
                setLog("onPageScrollState"," SCROLL_STATE_SETTLING")
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        setLog("onPageScrollState","onPageSelected(): position($position)")
        currentPage = position
    }

    abstract fun onPageScrollCanceled()

    companion object {
        private const val DEBOUNCE_TIMES = 500L
    }
}
