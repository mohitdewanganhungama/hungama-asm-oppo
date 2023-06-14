package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VideoPlayerBrightnessActionEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VIDEOPLAYERBRIGHTNESSACTION_ENAME
    }

    override fun getType(): EventType {
        return EventType.VIDEO_PLAYER_BRIGHTNESS_ACTION
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}