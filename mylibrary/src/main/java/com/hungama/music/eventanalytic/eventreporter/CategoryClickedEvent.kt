package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class CategoryClickedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.CATEGORY_ENAME
    }

    override fun getType(): EventType {
       return EventType.CATEGORY_CLICKED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}