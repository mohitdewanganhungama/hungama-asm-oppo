package com.hungama.music.data.model

interface PageViewOperator {
    fun backPageView(isError:Boolean)
    fun nextPageView(isError:Boolean)
}