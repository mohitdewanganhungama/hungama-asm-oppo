package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent
import java.util.*

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/24/2021.
 * Purpose:
 */
class AppLaunchEvent(val hashMap: HashMap<String, String>) : IEvent {
    override fun getName(): String {
        return EventConstant.APPLAUNCH_ENAME
    }

    override fun getType(): EventType {
        return EventType.APP_LAUNCH
    }

    override fun getProperty(): HashMap<String, String> {
        return  hashMap
    }
}