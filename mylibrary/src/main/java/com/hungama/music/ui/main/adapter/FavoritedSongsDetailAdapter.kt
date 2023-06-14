package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.hungama.fetch2.Status
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.fontmanger.FontDrawable
import java.io.File
import java.io.FileNotFoundException

class FavoritedSongsDetailAdapter(
    val ctx: Context,
    val list: List<BookmarkDataModel.Data.Body.Row>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class IType1000ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivDownload:ImageView = itemView.findViewById(R.id.ivDownload)
        val ivExplicit:ImageView = itemView.findViewById(R.id.ivE)
        val vNowPlaying:View = itemView.findViewById(R.id.vNowPlaying)
        val ivEqualizer:ImageView = itemView.findViewById(R.id.ivEqualizer)
        val ivEqualizerAnim: LottieAnimationView = itemView.findViewById(R.id.ivEqualizerAnim)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty()){
                val list = list[position].data

                if (list.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.releasedate != null) {
                    tvSubTitle.text = list.subtitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
                ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivMore.setImageDrawable(ivMoreDrawable)

                val drawable = FontDrawable(ctx, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)

                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(list.id!!)
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(list.id!!)
                if (downloadQueue != null){

                    downloadIconStates(downloadQueue.downloadStatus, ivDownload)

                }

                if (downloadedAudio != null){
                    if (downloadedAudio.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(downloadedAudio.downloadedFilePath)){
                        try {
                            val fileName = downloadedAudio.downloadedFilePath
                            val file = File(fileName)
                            if (file.exists()){
                                downloadIconStates(downloadedAudio.downloadStatus, ivDownload)
                            }else{
                                AppDatabase.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(
                                    downloadedAudio.contentId!!
                                )
                                downloadIconStates(0, ivDownload)
                            }
                        }catch (e:Exception){

                        }catch (fileNotFound: FileNotFoundException){
                            downloadIconStates(0, ivDownload)
                        }


                    }
                }

                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        list.image!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                CommonUtils.setExplicitContent(ctx, llMain, list.misc.explicit, ivExplicit)

                if (list.isCurrentPlaying){
                    vNowPlaying.show()
                    //ivEqualizer?.show()
                    ivEqualizerAnim.show()
                    ivEqualizerAnim.playAnimation()
                }else{
                    vNowPlaying.hide()
                    ivEqualizer.hide()
                    ivEqualizerAnim.hide()
                    ivEqualizerAnim.cancelAnimation()
                    ivEqualizerAnim.progress = 0f
                }

                llMain.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)){
                        onChildItemClick?.onUserClick(position,
                            isMenuClick = false,
                            isDownloadClick = false
                        )
                    }
                }

                ivMore.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)){
                        onChildItemClick?.onUserClick(position, isMenuClick = true, isDownloadClick = false)
                    }
                }

                ivDownload.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)){
                        onChildItemClick?.onUserClick(position, isMenuClick = false, isDownloadClick = true)

                        if(CommonUtils.isUserHasGoldSubscription()){
                            val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                            ivDownload.setImageDrawable(drawable)
                            setLog(TAG, "bind: download img change")
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_favorited_songs_detail, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType1000ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isMenuClick:Boolean, isDownloadClick:Boolean)
    }

    private fun downloadIconStates(status: Int, ivDownload: ImageView){
        when (status){
            Status.NONE.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
            Status.QUEUED.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
            Status.DOWNLOADING.value ->{
                val drawable = FontDrawable(ctx, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
            Status.COMPLETED.value ->{
                val drawable = FontDrawable(ctx, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
        }
    }
}