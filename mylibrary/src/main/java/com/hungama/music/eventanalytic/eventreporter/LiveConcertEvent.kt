package com.hungama.music.home.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/24/2021.
 * Purpose:
 */
class LiveConcertEvent(val hashMap: HashMap<String, String>) : IEvent {
    override fun getName(): String {
        return EventConstant.LIVE_CONCERT_ENAME
    }

    override fun getType(): EventType {
        return EventType.LIVE_CONCERT
    }

    override fun getProperty(): HashMap<String, String> {
        return  hashMap
    }
}