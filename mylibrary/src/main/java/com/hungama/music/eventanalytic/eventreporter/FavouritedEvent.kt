package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class FavouritedEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.FAVOURITED_ENAME
    }

    override fun getType(): EventType {
        return EventType.FAVOURITED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}