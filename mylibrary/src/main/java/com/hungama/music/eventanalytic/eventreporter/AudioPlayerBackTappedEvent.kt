package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AudioPlayerBackTappedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUDIOPLAYERBACKTAPPED_ENAME
    }

    override fun getType(): EventType {
        return EventType.AUDIO_PLAYER_BACK_TAPPED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}