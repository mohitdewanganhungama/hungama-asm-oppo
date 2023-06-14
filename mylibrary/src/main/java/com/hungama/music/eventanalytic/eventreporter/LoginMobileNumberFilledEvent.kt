package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginMobileNumberFilledEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGINMOBILENUMBERFILLED_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN_MOBILE_NUMBER_FILLED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}