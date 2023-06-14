package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AF_Subscribe(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AF_SUBSCRIBE_ENAME
    }

    override fun getType(): EventType {
        return EventType.PAGE_VIEW
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }

}