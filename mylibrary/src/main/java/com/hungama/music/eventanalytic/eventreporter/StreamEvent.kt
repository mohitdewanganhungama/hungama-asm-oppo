package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class StreamEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.STREAM_ENAME
    }

    override fun getType(): EventType {
        return EventType.STREAM
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}