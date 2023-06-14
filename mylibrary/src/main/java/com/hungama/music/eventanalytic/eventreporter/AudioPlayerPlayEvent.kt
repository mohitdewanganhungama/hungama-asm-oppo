package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AudioPlayerPlayEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUDIOPLAYERPLAY_ENAME
    }

    override fun getType(): EventType {
        return EventType.AUDIO_PLAYER_PLAY
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}