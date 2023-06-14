package com.hungama.music.data.model

import com.hungama.music.data.model.RowsItem

interface OnParentItemClickListener {
    fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int)
}