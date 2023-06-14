package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginSuccessEvent(val hashMap : HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGINSUCCESS_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN_SUCCESS
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}