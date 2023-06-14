package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class MoviesLanguageSelectedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.MOVIESLANGUAGESELECTED_ENAME
    }

    override fun getType(): EventType {
       return EventType.MOVIES_LANGUAGE_SELECTED
    }

    override fun getProperty(): HashMap<String, String> {
    return hashMap
    }
}