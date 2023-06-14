package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginEmailClickedEvent(var hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGINEMAILCLICKED_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN_EMAIL_CLICKED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}