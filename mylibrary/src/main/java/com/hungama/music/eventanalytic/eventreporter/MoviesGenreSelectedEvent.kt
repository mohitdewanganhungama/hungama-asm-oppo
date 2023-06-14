package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class MoviesGenreSelectedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.MOVIESGENRESELECTED_ENAME
    }

    override fun getType(): EventType {
        return EventType.MOVIES_GENRE_SELECTED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}