package com.hungama.music.data.model
interface BannerItemClick {
    fun bannerItemClick(
        isClicked: Boolean,
        pos: Int,
        bodyData: BodyRowsItemsItem?
    )
    fun onCheckSatusplaylist(
        isClicked: Boolean,
        pos: Int,
        bodyData: BodyRowsItemsItem?
    )
}