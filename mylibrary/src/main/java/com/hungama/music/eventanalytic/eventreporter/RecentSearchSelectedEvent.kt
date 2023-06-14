package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RecentSearchSelectedEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.RECENT_SEARCH_SELECTED_ENAME
    }

    override fun getType(): EventType {
        return EventType.RECENT_SEARCH_SELECTED_ENAME
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}