package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LiveConcertReminderEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.REMIND_CONCERT_ENAME
    }

    override fun getType(): EventType {
        return EventType.REMIND_CONCERT
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}