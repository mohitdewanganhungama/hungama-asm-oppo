package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class ApiPerformanceEvent(var hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.APIPERFORMANCE_ENAME
    }

    override fun getType(): EventType {
        return EventType.API_PERFORMANCE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }

}