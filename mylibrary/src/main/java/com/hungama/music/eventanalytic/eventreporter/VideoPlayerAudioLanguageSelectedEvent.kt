package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class VideoPlayerAudioLanguageSelectedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.VIDEOPLAYERAUDIOLANGUAGESELECTED_ENAME
    }

    override fun getType(): EventType {
        return EventType.VIDEO_PLAYER_AUDIO_LANGUAGE_SELECTED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}