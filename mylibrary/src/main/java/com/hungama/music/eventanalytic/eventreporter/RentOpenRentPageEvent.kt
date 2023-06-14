package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RentOpenRentPageEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.RENTOPENRENTPAGE_ENAME
    }

    override fun getType(): EventType {
        return EventType.RENT_OPEN_RENT_PAGE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }

}