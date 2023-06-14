package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class ProgressiveSurveyTappedEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.PROGRESSIVE_SURVEY_TAPPED_ENAME
    }

    override fun getType(): EventType {
        return EventType.PROGRESSIVE_SURVEY_TAPPED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}