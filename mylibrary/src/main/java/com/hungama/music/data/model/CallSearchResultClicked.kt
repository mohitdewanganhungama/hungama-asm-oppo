package com.hungama.music.data.model

import com.hungama.music.data.model.BodyRowsItemsItem

interface CallSearchResultClicked {
    fun contentClicked(searchdata: BodyRowsItemsItem, itemPosition:Int)
}