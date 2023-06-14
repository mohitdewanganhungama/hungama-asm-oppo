package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SubscriptionPlanPageOpenEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SUBSCRIPTION_PLAN_PAGE_OPEN
    }

    override fun getType(): EventType {
        return EventType.SUBSCRIPTION_PLAN_PAGE_OPEN
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }

}