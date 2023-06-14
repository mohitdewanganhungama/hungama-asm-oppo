package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VoiceSearchClicked(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VOICE_SEARCH_CLICKED
    }

    override fun getType(): EventType {
        return EventType.SEARCH_RESULT_FILTER
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}