package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AutosuggestDisplayed(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUTOSUGGEST_DISPLAYED
    }

    override fun getType(): EventType {
        return EventType.SEARCH_RESULT_FILTER
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}