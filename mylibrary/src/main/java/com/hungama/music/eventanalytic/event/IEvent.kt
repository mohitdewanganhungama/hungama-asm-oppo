package com.hungama.music.eventanalytic.event

interface IEvent {
   fun getName():String
    fun getType(): EventType
    fun getProperty():HashMap<String,String>

}