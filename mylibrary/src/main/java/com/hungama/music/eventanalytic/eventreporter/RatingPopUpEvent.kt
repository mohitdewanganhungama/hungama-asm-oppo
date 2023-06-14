package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RatingPopUpEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.RATINGPOPUP_ENAME
    }

    override fun getType(): EventType {
        return EventType.RATING_POP_UP
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}