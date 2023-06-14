package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VideoPlayerSubtitleSelectedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VIDEOPLAYERSUBTITLESELECTED_ENAME
    }

    override fun getType(): EventType {
        return EventType.VIDEO_PLAYER_SUBTITLE_SELECTED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}