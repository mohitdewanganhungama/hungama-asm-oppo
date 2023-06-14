package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AudioPlayerSkipBackwardEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUDIOPLAYERSKIPBACKWARD_ENAME
    }

    override fun getType(): EventType {
        return EventType.AUDIO_PLAYER_SKIP_BACKWARD
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}