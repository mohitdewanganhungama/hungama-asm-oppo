package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SearchAutoSuggestDisplay(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SEARCH_AUTOSUGGEST_DISPLAY
    }

    override fun getType(): EventType {
       return EventType.CATEGORY_SEARCH
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}