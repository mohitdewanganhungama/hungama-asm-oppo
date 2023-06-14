package com.hungama.music.home.eventreporter

import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/24/2021.
 * Purpose:
 */
class UserAttributeEvent(val hashMap: HashMap<String, String>) : IEvent {
    override fun getName(): String {
        return "userattribute"
    }

    override fun getType(): EventType {
        return EventType.USERATTRIBUTE
    }

    override fun getProperty(): HashMap<String, String> {
        return  hashMap
    }
}