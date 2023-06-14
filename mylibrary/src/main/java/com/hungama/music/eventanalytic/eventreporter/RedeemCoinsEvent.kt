package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.EventConstant.Companion.COINS_REDEEM
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class RedeemCoinsEvent(val hashMap: HashMap<String, String>) : IEvent {
    override fun getName(): String {
        return EventConstant.COINS_REDEEM
    }

    override fun getType(): EventType {
        return EventType.COINS_REDEEM
    }

    override fun getProperty(): HashMap<String, String> {
        return  hashMap
    }
}