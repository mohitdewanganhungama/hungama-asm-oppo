package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AutoCompleteResultsEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUTOCOMPLETERESULT_ENAME
    }

    override fun getType(): EventType {
        return EventType.AUTOCOMPLETE_RESULTS
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}