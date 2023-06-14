package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VideoPlayerSkipBackwardEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VIDEOPLAYERSKIPBACKWARD_ENAME
    }

    override fun getType(): EventType {
        return EventType.VIDEO_PLAYER_SKIP_BACKWARD
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}