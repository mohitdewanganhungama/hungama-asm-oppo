package com.hungama.music.home.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/24/2021.
 * Purpose:
 */
class LoginEvent(val hashMap: HashMap<String, String>) : IEvent {
    override fun getName(): String {
        return EventConstant.LOGIN_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}