package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class WebViewPerformanceEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.WEBVIEWPERFORMANCE_ENAME
    }

    override fun getType(): EventType {
        return EventType.WEB_VIEW_PERFORMANCE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}