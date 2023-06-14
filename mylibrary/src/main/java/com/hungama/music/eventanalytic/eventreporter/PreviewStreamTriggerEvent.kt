package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class PreviewStreamTriggerEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.PREVIEW_STREAM_TRIGGER_ENAME
    }

    override fun getType(): EventType {
        return EventType.STREAM_TRIGGER
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}