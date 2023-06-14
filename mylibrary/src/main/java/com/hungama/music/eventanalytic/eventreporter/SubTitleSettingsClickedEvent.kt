package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SubTitleSettingsClickedEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SUBTITLESETTINGSCLICKED_ENAME
    }

    override fun getType(): EventType {
        return EventType.SUBTITLE_SETTING_CLICKED
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}