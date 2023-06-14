package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RentPaymentOptionEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.RENTPAYMENTOPTION_ENAME
    }

    override fun getType(): EventType {
        return EventType.RENT_PAYMENT_OPTION
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}