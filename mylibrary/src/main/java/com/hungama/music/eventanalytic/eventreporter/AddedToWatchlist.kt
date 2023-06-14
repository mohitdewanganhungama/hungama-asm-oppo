package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AddedToWatchlist(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.ADDEDTOWATCHLIST_ENAME
    }

    override fun getType(): EventType {
        return EventType.ADDED_TO_WATCHLIST
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}