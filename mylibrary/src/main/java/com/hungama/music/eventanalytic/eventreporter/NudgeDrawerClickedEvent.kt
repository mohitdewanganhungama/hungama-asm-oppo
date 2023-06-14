package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class NudgeDrawerClickedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.NudgeDrawerClick
    }

    override fun getType(): EventType {
       return EventType.NudgeDrawerClick
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}