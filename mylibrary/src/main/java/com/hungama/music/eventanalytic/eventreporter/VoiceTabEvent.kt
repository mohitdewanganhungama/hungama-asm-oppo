package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VoiceTabEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VOICETAP_ENAME
    }

    override fun getType(): EventType {
        return EventType.VOICE_TAP
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}