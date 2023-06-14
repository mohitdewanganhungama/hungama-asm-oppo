package com.hungama.music.data.model

interface OnUserSubscriptionUpdate {
    public fun onUserSubscriptionUpdateCall(status:Int, contentId:String)
}