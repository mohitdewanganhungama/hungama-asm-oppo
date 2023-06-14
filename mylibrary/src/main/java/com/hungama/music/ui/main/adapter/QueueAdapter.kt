package com.hungama.music.ui.main.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.R
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.Track_State
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.databinding.RowNowPlayingBinding
import com.hungama.music.databinding.RowQueueBinding
import com.hungama.music.databinding.RowQueueHistoryBinding
import com.hungama.music.utils.Constant.TRACKS_LIST
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils
import com.hungama.music.utils.hide
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class QueueAdapter(
    val context: Context,
    var arrayList: ArrayList<Track>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2
    private val ROW_TYPE_3 = 3
    private var isHeaderHistoryVisiable=false

    fun addData(list: ArrayList<Track>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==ROW_TYPE_1){
            val binding: RowQueueHistoryBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_queue_history,
                parent,
                false
            )
            return HistoryItemViewHolder(binding)
        }else if(viewType==ROW_TYPE_2){
            val binding: RowNowPlayingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_now_playing,
                parent,
                false
            )
            return NowPlayingViewHolder(binding)
        }else if(viewType==ROW_TYPE_3){
            val binding: RowQueueBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_queue,
                parent,
                false
            )
            return ItemViewHolder(binding)
        }else{
            val binding: RowQueueBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_queue,
                parent,
                false
            )
            return ItemViewHolder(binding)
        }


    }


    class NowPlayingViewHolder(val binding: RowNowPlayingBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ItemViewHolder(val binding: RowQueueBinding) :
        RecyclerView.ViewHolder(binding.root)

    class HistoryItemViewHolder(val binding: RowQueueHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
       return arrayList?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //CommonUtils.setLog("onBindViewHolder", "title-${arrayList?.get(position)?.title}-arrayList?.get(position)?.state-${arrayList?.get(position)?.state}")
        if(getItemViewType(position)==ROW_TYPE_1 && holder is HistoryItemViewHolder){
            if (arrayList?.get(holder.adapterPosition)?.contentType == ContentTypes.Audio_Ad.value){
                (holder as HistoryItemViewHolder).itemView.visibility = View.GONE
                (holder as HistoryItemViewHolder).binding.llMain.layoutParams.height = 0
                Utils.setMarginsTop((holder as HistoryItemViewHolder).binding.llMain, 0)
                (holder as HistoryItemViewHolder).binding.llMain.requestLayout()
            }else{
                if (arrayList?.get(holder.adapterPosition)?.title != null) {

                    (holder as HistoryItemViewHolder).binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.title
                    (holder as HistoryItemViewHolder).binding.tvTitle.visibility = View.VISIBLE
                } else {
                    (holder as HistoryItemViewHolder).binding.tvTitle.visibility = View.GONE
                }

                if (arrayList?.get(holder.adapterPosition)?.subTitle != null) {
                    (holder as HistoryItemViewHolder)?.binding?.tvSubTitle?.text =
                        arrayList?.get(holder.adapterPosition)?.subTitle
                    (holder as HistoryItemViewHolder)?.binding?.tvSubTitle?.visibility = View.VISIBLE
                } else {
                    (holder as HistoryItemViewHolder)?.binding?.tvSubTitle?.visibility = View.GONE
                }

                if (arrayList?.get(holder.adapterPosition)?.image != null) {
                    (holder as HistoryItemViewHolder)?.binding?.ivSong?.let {
                        ImageLoader.loadImage(
                            context,
                            it,
                            arrayList?.get(holder.adapterPosition)?.image!!,
                            R.drawable.bg_gradient_placeholder
                        )
                    }
                }

                (holder as HistoryItemViewHolder)?.binding?.ivDelete?.setOnClickListener {
                    if (onitemclick != null) {
                        onitemclick.onItemDeleteClick(holder.adapterPosition)
                    }
                }

                (holder as HistoryItemViewHolder)?.binding?.llSong?.setOnClickListener {
                    if (onitemclick != null) {
                        onitemclick.onItemPlayClick(holder.adapterPosition)
                    }
                }

                if(position == 0){
                    //isHeaderHistoryVisiable=true
                    (holder as HistoryItemViewHolder)?.binding?.tvHeaderTitle?.visibility=View.VISIBLE
                }else{
                    (holder as HistoryItemViewHolder)?.binding?.tvHeaderTitle?.visibility=View.GONE
                }

                (holder as HistoryItemViewHolder)?.binding?.ivStatus?.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onitemclick?.onStartDrag(holder)
                    }
                    return@setOnTouchListener true
                }
            }
        }else if(getItemViewType(position)==ROW_TYPE_2 && holder is NowPlayingViewHolder){

            if (arrayList?.get(holder.adapterPosition)?.title != null) {

                (holder as NowPlayingViewHolder).binding.tvNowTitle.text = arrayList?.get(holder.adapterPosition)?.title
                (holder as NowPlayingViewHolder).binding.tvNowTitle.visibility = View.VISIBLE
            } else {
                (holder as NowPlayingViewHolder).binding.tvNowTitle.visibility = View.GONE
            }

            if (arrayList?.get(holder.adapterPosition)?.subTitle != null) {
                (holder as NowPlayingViewHolder)?.binding?.tvNowSubTitle?.text =
                    arrayList?.get(holder.adapterPosition)?.subTitle
                (holder as NowPlayingViewHolder)?.binding?.tvNowSubTitle?.visibility = View.VISIBLE
            } else {
                (holder as NowPlayingViewHolder)?.binding?.tvNowSubTitle?.visibility = View.GONE
            }

            if (arrayList?.get(holder.adapterPosition)?.image != null) {
                (holder as NowPlayingViewHolder)?.binding?.ivNowPlay?.let {
                    ImageLoader.loadImage(
                        context,
                        it,
                        arrayList?.get(holder.adapterPosition)?.image!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }
            }

            //BaseActivity.queueNowPlayIndex = position
        }else if(getItemViewType(position)==ROW_TYPE_3 && holder is ItemViewHolder){
            if (arrayList?.get(holder.adapterPosition)?.contentType == ContentTypes.Audio_Ad.value){
                (holder as ItemViewHolder).itemView.visibility = View.GONE
                (holder as ItemViewHolder).binding.llMain.layoutParams.height = 0
                Utils.setMarginsTop((holder as ItemViewHolder).binding.llMain, 0)
                (holder as ItemViewHolder).binding.llMain.requestLayout()
            }else{
                if (arrayList?.get(holder.adapterPosition)?.title != null) {

                    (holder as ItemViewHolder).binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.title
                    (holder as ItemViewHolder).binding.tvTitle.visibility = View.VISIBLE
                } else {
                    (holder as ItemViewHolder).binding.tvTitle.visibility = View.GONE
                }

                if (arrayList?.get(holder.adapterPosition)?.subTitle != null) {
                    (holder as ItemViewHolder)?.binding?.tvSubTitle?.text =
                        arrayList?.get(holder.adapterPosition)?.subTitle
                    (holder as ItemViewHolder)?.binding?.tvSubTitle?.visibility = View.VISIBLE
                } else {
                    (holder as ItemViewHolder)?.binding?.tvSubTitle?.visibility = View.GONE
                }

                if (arrayList?.get(holder.adapterPosition)?.image != null) {
                    (holder as ItemViewHolder)?.binding?.ivSong?.let {
                        ImageLoader.loadImage(
                            context,
                            it,
                            arrayList?.get(holder.adapterPosition)?.image!!,
                            R.drawable.bg_gradient_placeholder
                        )
                    }
                }

                (holder as ItemViewHolder)?.binding?.ivDelete?.setOnClickListener {
                    if (onitemclick != null) {
                        onitemclick.onItemDeleteClick(holder.adapterPosition)
                    }
                }

                (holder as ItemViewHolder)?.binding?.llSong?.setOnClickListener {
                    if (onitemclick != null) {
                        onitemclick.onItemPlayClick(holder.adapterPosition)
                    }
                }

                (holder as ItemViewHolder)?.binding?.ivStatus?.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onitemclick?.onStartDrag(holder)
                    }
                    return@setOnTouchListener true
                }
            }
        }


    }


    override fun getItemViewType(position: Int): Int {
        if(arrayList.get(position).state.equals(Track_State.PLAYED)){
            return ROW_TYPE_1
        }else if(arrayList.get(position).state.equals(Track_State.PLAYING) || arrayList.get(position).state.equals(Track_State.PAUSED)){
            return ROW_TYPE_2
        }else if(arrayList.get(position).state.equals(Track_State.IN_QUEUE)){
            return ROW_TYPE_3
        }else{
            return ROW_TYPE_3
        }


    }

    interface OnItemClick {
        fun onItemDeleteClick(position: Int)
        fun onItemPlayClick(position: Int)
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    /**
     * Function called to swap dragged items
     */
    fun swapItems(fromPosition: Int, toPosition: Int) {
        setLog("TAG", "fromPosition:"+fromPosition)
        setLog("TAG", "toPosition:"+toPosition)
        /*if (fromPosition < toPosition) {
            for (i in fromPosition..toPosition - 1) {
                arrayList?.set(i, arrayList?.set(i+1, arrayList?.get(i)));
                setLog("TAG", "fromPosition => i:"+i)
            }
        } else {
            for (i in fromPosition..toPosition + 1) {
                arrayList?.set(i, arrayList?.set(i-1, arrayList?.get(i)));
                setLog("TAG", "fromPosition => i:"+i)
            }
        }

        notifyItemMoved(fromPosition, toPosition)*/

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(arrayList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(arrayList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)

        /*arrayList?.forEachIndexed { index, track ->
                if(track.state==Track_State.PLAYING){
                    BaseActivity.updateNowPlayingCurrentIndex(index)
                    setLog("TAG", "nowPlayingPosition:"+BaseActivity.nowPlayingCurrentIndex())
                }
        }
        BaseActivity?.setTrackListData(arrayList)*/
            /*val intent = Intent(context, AudioPlayerService::class.java)
            intent.apply {
                action = AudioPlayerService.PlaybackControls.RE_ORDER.name
                putExtra(TRACKS_LIST, ArrayList(arrayList))
            }
            context?.let {
                Util.startForegroundService(it, intent)
            }*/
    }

    fun onItemSelected(viewHolder: ItemViewHolder) {
        viewHolder?.itemView?.translationZ = 10F
    }

    @OptIn(UnstableApi::class) fun onItemClear(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is ItemViewHolder || viewHolder is HistoryItemViewHolder) {
            viewHolder?.itemView?.translationZ = 0F

            arrayList?.forEachIndexed { index, track ->
                if(track.state==Track_State.PLAYING){
                    BaseActivity.updateNowPlayingCurrentIndex(index)
                    setLog("TAG", "nowPlayingPosition:"+BaseActivity.nowPlayingCurrentIndex())
                }
            }
            //BaseActivity?.setTrackListData(arrayList)

            val intent = Intent(context, AudioPlayerService::class.java)
            intent.apply {
                action = AudioPlayerService.PlaybackControls.RE_ORDER.name
                putExtra(TRACKS_LIST, ArrayList(arrayList))
            }
            context?.let {
                Util.startForegroundService(it, intent)
            }
        }
    }


}