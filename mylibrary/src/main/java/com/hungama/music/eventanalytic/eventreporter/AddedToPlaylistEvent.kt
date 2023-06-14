package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AddedToPlaylistEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.ADDEDTOPLAYLIST_ENAME
    }

    override fun getType(): EventType {
        return EventType.ADDED_TO_PLAYLIST
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}