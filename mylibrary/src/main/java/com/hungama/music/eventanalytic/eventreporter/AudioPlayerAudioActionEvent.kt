package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AudioPlayerAudioActionEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUDIOPLAYERAUDIOACTION_ENAME
    }

    override fun getType(): EventType {
        return EventType.AUDIO_PLAYER_AUDIO_ACTION
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}