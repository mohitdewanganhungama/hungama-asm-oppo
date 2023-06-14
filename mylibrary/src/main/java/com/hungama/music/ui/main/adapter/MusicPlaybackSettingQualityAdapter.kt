package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowSelectMusicPlaybackQualityBinding
import com.hungama.music.data.model.PlanNames
import com.hungama.music.data.model.MusicPlaybackSettingStreamQualityModel
import com.hungama.music.data.model.Quality
import com.hungama.music.data.model.QualityAction
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.getStoredSelectedQualityId
import com.hungama.music.utils.CommonUtils.getUserSubscriptionProfileConfigData
import com.hungama.music.utils.CommonUtils.isUserHasGoldSubscription
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.setStoredSelectedQualityId
import com.hungama.music.utils.Constant
import com.hungama.music.utils.hide
import com.hungama.music.utils.show
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class MusicPlaybackSettingQualityAdapter(
    val context: Context,
    var arrayList: List<MusicPlaybackSettingStreamQualityModel>?,
    val onitemclick: OnItemClick?,
    val qualityAction: QualityAction
) :
    RecyclerView.Adapter<MusicPlaybackSettingQualityAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1


    fun addData(list: List<MusicPlaybackSettingStreamQualityModel>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowSelectMusicPlaybackQualityBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_select_music_playback_quality,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowSelectMusicPlaybackQualityBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList?.size!!
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {

        val list = arrayList?.get(pos)

        setLog("PrintingList","List${list?.isSelected}")
        val profileConfig = getUserSubscriptionProfileConfigData(context)
        if (!TextUtils.isEmpty(list?.title)){
            if (list?.id == Quality.DOLBY.id){
                holder.binding.rlDolby.show()
                holder.binding.ivDolby.show()
                holder.binding.tvTitle.hide()
            }else{
                holder.binding.tvTitle.text = list?.title
                holder.binding.tvTitle.show()
                holder.binding.ivDolby.hide()
                holder.binding.rlDolby.hide()
            }
        }else{
            holder.binding.tvTitle.visibility = View.GONE
        }
        if (((profileConfig == null && list?.id == Quality.HD.id) || (profileConfig?.hd_quality == 0 && list?.id == Quality.HD.id)
                    || (profileConfig == null && list?.id == Quality.DOLBY.id) || (profileConfig?.hd_quality == 0 && list?.id == Quality.DOLBY.id)) && (list.id == Quality.DOLBY.id || list.id == Quality.HD.id)){
            CommonUtils.setAppButton2(context, holder.binding.llUpgradePlan)
            holder.binding.llUpgradePlan.show()
        }else{
            holder.binding.llUpgradePlan.hide()
        }

        setLog("playbackQuality", "MusicPlaybackSettingQualityAdapter-list?.isSelected-${list?.isSelected}-Quality-${list?.title} - key-${list?.urlKey} - isGoldUser-${list?.isGoldUser}")
        if (list?.isSelected!!){
            holder.binding.ivTick.visibility = View.VISIBLE
        }else{
            holder.binding.ivTick.visibility = View.INVISIBLE
        }


        if (list.id == Quality.DOLBY.id || list.id == Quality.HD.id){
            setLog("playbackQuality", "MusicPlaybackSettingQualityAdapter-Quality-${list.title} - key-${list.urlKey} - isGoldUser-${list.isGoldUser}")
            if (list.isDolbyOrHDEnable){
                holder.binding.llMain.alpha = 1F
            }else{
                holder.binding.tvTitle.alpha = 0.4F
                holder.binding.rlDolby.alpha = 0.4F
            }
        }

        if (!arrayList.isNullOrEmpty() && pos == arrayList?.size!! - 1){
            holder.binding.devider.hide()
        }

        holder.binding.llMain.setOnClickListener {
            if ((profileConfig == null && list.id == Quality.HD.id) || (profileConfig?.hd_quality == 0 && list.id == Quality.HD.id) || (profileConfig == null && list.id == Quality.DOLBY.id) || (profileConfig?.hd_quality == 0 && list.id == Quality.DOLBY.id) ){
                Constant.screen_name ="Quality Setting"
                CommonUtils.openSubscriptionDialogPopup(context, PlanNames.SVOD.name, "", true, null, "", null,Constant.drawer_streaming_quality)
                if (onitemclick != null) {
                    onitemclick.onUserClick(pos, true)
                }
            }else{
                //setLog("playbackQuality", "MusicPlaybackSettingQualityAdapter-list.isDolbyOrHDEnable-${list.isDolbyOrHDEnable}-Quality-${list.title} - key-${list.urlKey} - isGoldUser-${list.isGoldUser}")
                if (list.isDolbyOrHDEnable || (list.id != Quality.HD.id && list.id != Quality.DOLBY.id)){
                    for (i in 0 until arrayList?.size!!) {
                        arrayList?.get(i)?.isSelected = false
                    }
                    setStoredSelectedQualityId(qualityAction, list.id, list.title)
                    list.isSelected = true
                    if (onitemclick != null) {
                        onitemclick.onUserClick(pos, false)
                    }
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }//onBind

    override fun getItemViewType(position: Int): Int {
        return position

    }

    interface OnItemClick {
        fun onUserClick(position: Int, onlyDismissPopUp:Boolean)
    }
}