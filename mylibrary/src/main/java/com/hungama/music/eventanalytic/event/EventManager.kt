package com.hungama.music.eventanalytic.event

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.home.eventsubscriber.AmplitudeSubscriber
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/24/2021.
 * Purpose:
 */
class EventManager {
    var lastEvent = ""
    var subList=ArrayList<ISubscriber>()
    var multipalEventList=ArrayList<String>()
    init {
        instance = this
        multipalEventList.add(EventConstant.VIDEOPLAYERSKIPFORWARD_ENAME)
        multipalEventList.add(EventConstant.SEARCHRESULTFILTER_ENAME)
        multipalEventList.add(EventConstant.VIDEOPLAYERSKIPBACKWARD_ENAME)
        multipalEventList.add(EventConstant.THREEDOTSCLICKED_ENAME)
        multipalEventList.add(EventConstant.VIDEOPLAYERLOCKTAPPED_ENAME)
        multipalEventList.add(EventConstant.VIDEOPLAYERACTIONS_ENAME)
        multipalEventList.add(EventConstant.VIDEOPLAYERBRIGHTNESSACTION_ENAME)
        multipalEventList.add(EventConstant.VIDEOPLAYERAUDIOACTION_ENAME)
    }

    companion object {
        private var instance: EventManager? = null
        fun getInstance(): EventManager {
            if (instance == null) {
                instance = EventManager()
            }
            return instance as EventManager
        }
    }

    fun registerSubscriber(iSubscriber: ISubscriber) {
        subList.add(iSubscriber)
    }
    fun sendEvent(iEvent: IEvent){
        CoroutineScope(Dispatchers.IO).launch{
            setLog("EventManager", "EventManager 11 sendEvent lastEvent:${lastEvent} current:${iEvent?.getName()}")

            if(!lastEvent?.equals(iEvent?.getName(),true)!! || multipalEventList?.contains(iEvent?.getName()) == true){
                subList.forEach {
                    it.sendEvent(iEvent)
                    setLog("EventManagerName",iEvent.getName() + " " + it)
                }
//                lastEvent=iEvent.getName()
                BaseActivity.eventManagerStreamName = iEvent.getName()
                setLog("EventManager", "EventManager 22  last sent Event current:${iEvent?.getName()}")

            }
        }


    }

    fun sendUserAttribute(iEvent: IEvent){
        CoroutineScope(Dispatchers.IO).launch{
            subList.forEach {
                it?.sendUserAttribute(iEvent)
            }
        }

    }


    fun findAmplitudeSubscriber():AmplitudeSubscriber{
        setLog("GM-SDK-APP","findAmplitudeSubscriber subList size:${subList?.size}")
        subList.let {
            return subList?.get(0) as AmplitudeSubscriber
        }
    }

}