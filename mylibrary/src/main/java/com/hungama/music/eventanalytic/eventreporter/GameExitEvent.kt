package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class GameExitEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.GAME_EXIT
    }

    override fun getType(): EventType {
        return EventType.GAME_EXIT
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}