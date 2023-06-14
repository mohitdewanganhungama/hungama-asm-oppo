package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class GameStartEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.GAME_START
    }

    override fun getType(): EventType {
        return EventType.GAME_START
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}