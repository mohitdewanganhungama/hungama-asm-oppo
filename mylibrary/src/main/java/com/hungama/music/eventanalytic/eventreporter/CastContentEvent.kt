package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class CastContentEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.CASTCONTENT_ENAME
    }

    override fun getType(): EventType {
        return EventType.CAST_CONTENT
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}