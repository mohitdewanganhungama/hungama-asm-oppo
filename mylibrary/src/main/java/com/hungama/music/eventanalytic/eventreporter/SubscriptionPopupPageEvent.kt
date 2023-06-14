package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SubscriptionPopupPageEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SUBSCRIPTION_POPUP
    }

    override fun getType(): EventType {
        return EventType.SUBSCRIPTION_POPUP
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }

}