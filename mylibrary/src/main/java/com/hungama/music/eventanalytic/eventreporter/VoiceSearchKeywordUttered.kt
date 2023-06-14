package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VoiceSearchKeywordUttered(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VOICE_SEARCH_KEYWORD_UTTERED
    }

    override fun getType(): EventType {
       return EventType.CATEGORY_SEARCH
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}