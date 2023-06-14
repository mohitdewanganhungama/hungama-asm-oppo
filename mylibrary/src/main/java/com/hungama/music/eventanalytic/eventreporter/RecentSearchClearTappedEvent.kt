package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RecentSearchClearTappedEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.RECENT_SEARCH_CLEAR_TAPPED_ENAME
    }

    override fun getType(): EventType {
        return EventType.RECENT_SEARCH_CLEAR_TAPPED_ENAME
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}