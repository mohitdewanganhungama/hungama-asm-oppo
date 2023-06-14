package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginOTPPageLoadSuccessEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGINOTPPAGELOADSUCCESS_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN_OTP_PAGE_LOAD_SUCCESS
    }

    override fun getProperty(): HashMap<String, String> {
       return hashMap
    }
}