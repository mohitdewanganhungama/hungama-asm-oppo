package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class StreamStartEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.STREAM_START_ENAME
    }

    override fun getType(): EventType {
        return EventType.STREAM_START
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}