package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SubscriptionFailedPageEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SUBSCRIPTION_FAILED
    }

    override fun getType(): EventType {
        return EventType.SUBSCRIPTION_FAILED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }

}