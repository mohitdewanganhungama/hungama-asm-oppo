package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class ThreeDotsClickedEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.THREEDOTSCLICKED_ENAME
    }

    override fun getType(): EventType {
        return EventType.THREE_DOTS_CLIKED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}