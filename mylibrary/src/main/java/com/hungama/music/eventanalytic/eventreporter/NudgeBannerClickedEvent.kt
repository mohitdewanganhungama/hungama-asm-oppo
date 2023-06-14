package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class NudgeBannerClickedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.NudgeBannerClick
    }

    override fun getType(): EventType {
       return EventType.NudgeBannerClick
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}