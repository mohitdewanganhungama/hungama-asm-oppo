package com.hungama.music.ui.main.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.R
import com.hungama.music.data.model.OrignalSeason.OrignalData.OrignalMisc.OrignalMiscTrack
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.TvShowDetailsFragment
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.SharedPrefHelper
import java.util.concurrent.TimeUnit

class OrignalEpisodeListAdapter(val ctx:Context,
                                val mediaObjects: ArrayList<OrignalSeason.OrignalData.OrignalMisc.OrignalMiscTrack>,
                                val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnUserSubscriptionUpdate {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        return EpisodeViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_itype_orignal, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        (viewHolder as EpisodeViewHolder).onBind(mediaObjects[i], requestManager)
        /*viewHolder?.itemView.setOnTouchListener { v, event ->
            CommonUtils.setLog("original", "Original-47-onTouch-6")
            //v.parent.requestDisallowInterceptTouchEvent(false)
            return@setOnTouchListener false
        }*/
        viewHolder?.itemView?.setOnClickListener {

            val dpm = DownloadPlayCheckModel()

            /*if (CommonUtils.userCanDownloadContent(
                    ctx,
                    null,
                    dpm,
                    this@OrignalEpisodeListAdapter
                )
            ){
                if (!CommonUtils.checkUserCensorRating(
                        ctx,
                        attributeCensorRating
                    )
                ) {
                    val intent =
                        Intent(ctx, VideoPlayerActivity::class.java)
                    val serviceBundle = Bundle()

                    serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                    serviceBundle.putString(
                        Constant.SELECTED_CONTENT_ID,
                        mediaObjects[i].data?.id
                    )
                    serviceBundle.putInt(
                        Constant.CONTENT_TYPE,
                        Constant.CONTENT_MOVIE
                    )
                    serviceBundle.putInt(
                        Constant.TYPE_ID,
                        mediaObjects[i].data?.type!!
                    )
                    intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                    intent.putExtra(
                        "thumbnailImg",
                        mediaObjects[i].data?.image
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //startActivity(intent)
                    serviceBundle.putLong(
                        Constant.VIDEO_START_POSITION,
                        TimeUnit.SECONDS.toMillis(
                            HungamaMusicApp.getInstance().getContentDuration(
                                mediaObjects[i].data?.id!!
                            )!!
                        )
                    )
//            (ctx).setLocalBroadcastEventCall(
//                ctx!!,
//                Constant.VIDEO_PLAYER_EVENT
//            )
                    ctx.startActivity(intent)
//            ctx.startActivityForResult(
//                intent,
//                Constant.VIDEO_ACTIVITY_RESULT_CODE
//            )
                }
            }*/


            dpm.contentId =  mediaObjects[i].data?.id?.toString()!!
            dpm.contentTitle =  mediaObjects[i].data?.name?.toString()!!
            dpm.planName =mediaObjects[i].data?.movierights?.toString()!!
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            var attributeCensorRating = ""
            if (!mediaObjects[i].data.attributeCensorRating.isNullOrEmpty()){
                attributeCensorRating = mediaObjects[i].data.attributeCensorRating.get(0).toString()
            }
            Constant.screen_name ="Original Episode List"

            if (CommonUtils.userCanDownloadContent(
                    ctx,
                    null,
                    dpm,
                    this@OrignalEpisodeListAdapter,Constant.drawer_svod_tvshow_episode
                )
            ) {
                if (!CommonUtils.checkUserCensorRating(
                        ctx,
                        attributeCensorRating
                    )
                ) {

                    val intent =
                        Intent(ctx, VideoPlayerActivity::class.java)
                    val serviceBundle = Bundle()
                    serviceBundle.putParcelableArrayList(Constant.ORIGINAL_SEASON_LIST, mediaObjects)
                    serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                    serviceBundle.putString(Constant.SELECTED_CONTENT_ID, mediaObjects[i].data.id)
                    serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_TV_SHOW)
                    serviceBundle.putBoolean(Constant.IS_ORIGINAL, true)
                    serviceBundle.putInt(Constant.TYPE_ID, mediaObjects[i].data.type)
                    intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                    intent.putExtra("thumbnailImg", mediaObjects[i].data.image)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    serviceBundle.putLong(
                        Constant.VIDEO_START_POSITION,
                        TimeUnit.SECONDS.toMillis(HungamaMusicApp.getInstance().getContentDuration(mediaObjects[i].data.id)!!)
                    )

                    if (ctx != null && ctx is MainActivity){
                        val status = (ctx as MainActivity).getAudioPlayerPlayingStatus()
                        if (status == Constant.pause){
                            SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                        }else{
                            SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                        }
                        (ctx as MainActivity).pausePlayer()
                    }
                    ctx.startActivity(intent)
                    /*startActivityForResult(
                        intent,
                        Constant.VIDEO_ACTIVITY_RESULT_CODE
                    )*/


                }
            }

        }
        if (mediaObjects?.get(viewHolder.adapterPosition)?.data?.movierights != null&&mediaObjects?.get(viewHolder.adapterPosition)?.data?.movierights?.size!!>0) {
            Utils.setMovieRightTextForBucket(viewHolder.ivSubscription,mediaObjects?.get(viewHolder.adapterPosition)?.data?.movierights!!, ctx, mediaObjects?.get(viewHolder.adapterPosition)?.data?.id.toString(),true)
            setLog("TAG", "onBindViewHolder: "+mediaObjects?.get(viewHolder.adapterPosition)?.data?.id.toString())
        } else {
            viewHolder.ivSubscription.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mediaObjects.size
    }

    internal inner class EpisodeViewHolder(private val parent: View) : RecyclerView.ViewHolder(
        parent
    ) {
        /**
         * below view have public modifier because
         * we have access PlayerViewHolder inside the ExoPlayerRecyclerView
         */
        var ivUserImage: ImageView
        var requestManager: RequestManager? = null
        private val tvTitle: TextView
        private val tvSubTitle: TextView
        var ivSubscription : ImageView
        fun onBind(mediaObject: OrignalMiscTrack, requestManager: RequestManager?) {
            this.requestManager = requestManager
            parent.tag = this
            tvTitle.text = mediaObject.data.name
            tvSubTitle.text = mediaObject.data.subTitle
            this.requestManager
                ?.load(mediaObject.data.image)
                ?.into(ivUserImage)

        }

        init {
            ivUserImage = itemView.findViewById(R.id.ivUserImage)
            tvTitle = itemView.findViewById(R.id.tvTitle)
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle)
            ivSubscription = itemView.findViewById(R.id.ivSubscription)
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }
}