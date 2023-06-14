package com.hungama.music.eventanalytic.util.callbacks.inapp

import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.InAppSelfHandledModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.showToast
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.InAppMessageListener
import com.moengage.inapp.model.MoEInAppCampaign


/**
 * @author
 * Date: 2021-02-21
 */
class InAppCallback : InAppMessageListener() {

  companion object{
    var mInAppCampaignList= HashMap<String,InAppSelfHandledModel>()
  }



  override fun onNavigation(inAppCampaign: MoEInAppCampaign): Boolean {
    setLog("setMoengageData", "Navigation Action: %s"+ inAppCampaign)
    // return true if the application is handling the navigation else false.
    return true
  }
  override fun onCustomAction(inAppCampaign: MoEInAppCampaign) {
    super.onCustomAction(inAppCampaign)
    setLog("setMoengageData", "Custom Action: %s"+ inAppCampaign)
    MoEInAppHelper.getInstance().selfHandledClicked(HungamaMusicApp.getInstance(), inAppCampaign)
  }

  override fun onSelfHandledAvailable(inAppCampaign: MoEInAppCampaign) {
    super.onSelfHandledAvailable(inAppCampaign)
    setLog("SetMoengageDataMain", " "  + Gson().toJson(inAppCampaign.selfHandledCampaign!!.payload))

    try {
      if(inAppCampaign.selfHandledCampaign?.payload!=null&&!TextUtils.isEmpty(inAppCampaign.selfHandledCampaign?.payload)){
        val inAppSelfHandledModel = Gson().fromJson<InAppSelfHandledModel>(
          inAppCampaign.selfHandledCampaign?.payload,
          InAppSelfHandledModel::class.java
        ) as InAppSelfHandledModel

        inAppSelfHandledModel?.campaignId=inAppCampaign.campaignId
        inAppSelfHandledModel?.inAppCampaign=inAppCampaign
        if(mInAppCampaignList.size>0){
          var check=true
          mInAppCampaignList?.values?.forEachIndexed { index, moEInAppCampaign ->
            try {
              if (inAppSelfHandledModel.bottom_nav_position == moEInAppCampaign?.bottom_nav_position && inAppSelfHandledModel.top_nav_position == moEInAppCampaign?.top_nav_position) {
                check=false
                setLog("setMoengageData", "onSelfHandledAvailable mInAppCampaignList status"+check)
              }
            }catch (e :Exception){

            }
          }
          if(check){
            mInAppCampaignList.put(inAppSelfHandledModel.campaign_id, inAppSelfHandledModel)
          }
        }
        else {
          mInAppCampaignList.put(inAppSelfHandledModel.campaign_id, inAppSelfHandledModel)
        }

        setLog("setMoengageData", "onSelfHandledAvailable mInAppCampaignList size:${mInAppCampaignList?.size}")
      }

    }catch (exp:Exception){
      exp.printStackTrace()
    }

  }
}
