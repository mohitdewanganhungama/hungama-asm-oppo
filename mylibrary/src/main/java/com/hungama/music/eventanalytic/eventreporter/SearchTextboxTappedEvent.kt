package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SearchTextboxTappedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SEARCHTEXTBOXTAPPED_ENAME
    }

    override fun getType(): EventType {
        return EventType.SEARCH_TEXTBOX_TAPPED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}