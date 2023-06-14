package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginMobileClickedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGINMOBILECLICKED_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN_MOBILE_CLICKED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}