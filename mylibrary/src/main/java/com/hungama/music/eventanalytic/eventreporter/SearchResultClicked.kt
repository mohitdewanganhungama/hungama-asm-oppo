package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SearchResultClicked(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SEEARCH_RESULT_CLICKED
    }

    override fun getType(): EventType {
       return EventType.CATEGORY_SEARCH
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}