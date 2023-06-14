package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class StoryPageViewedEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.STORY_PAGE_VIEWED_ENAME
    }

    override fun getType(): EventType {
        return EventType.STORY_PAGE_VIEWED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}