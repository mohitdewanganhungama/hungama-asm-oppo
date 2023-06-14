package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RegistrationSuccessEvent(val hashMap : HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.REGISTRATION_SUCCESS_ENAME
    }

    override fun getType(): EventType {
        return EventType.REGISTER_SUCCESS
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}