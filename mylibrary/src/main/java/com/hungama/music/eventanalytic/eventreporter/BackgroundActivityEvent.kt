package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class BackgroundActivityEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.BACKGROUND_ACTIVITY
    }

    override fun getType(): EventType {
        return EventType.BACKGROUND_ACTIVITY
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}