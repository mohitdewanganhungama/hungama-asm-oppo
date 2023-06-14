package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AudioPlayerPauseEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUDIOPLAYERPAUSE_ENAME
    }

    override fun getType(): EventType {
        return EventType.AUDIO_PLAYER_PAUSE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}