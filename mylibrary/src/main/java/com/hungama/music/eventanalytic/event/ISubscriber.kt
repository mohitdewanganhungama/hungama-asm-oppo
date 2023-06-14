package com.hungama.music.eventanalytic.event

import com.hungama.music.eventanalytic.event.IEvent

interface ISubscriber{
        fun sendEvent(iEvent: IEvent)
        fun sendUserAttribute(iEvent: IEvent)
    }