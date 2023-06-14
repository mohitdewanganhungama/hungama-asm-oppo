package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SearchBarClick(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SEARCH_BAR_CLICK
    }

    override fun getType(): EventType {
       return EventType.CATEGORY_SEARCH
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}