package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils
import com.hungama.music.utils.hide
import com.hungama.music.utils.show

class SavedVideoAdapter(
    var context: Context,
    var musicplayList: ArrayList<BookmarkDataModel.Data.Body.Row>,
    var itemClick: onItemClickListener
):RecyclerView.Adapter<SavedVideoAdapter.ViewHolder>() {


    internal fun refreshData(arrayList:ArrayList<BookmarkDataModel.Data.Body.Row>){
        musicplayList = arrayList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var Title = itemView.findViewById(R.id.tvTitle)as TextView
        var subTitle = itemView.findViewById(R.id.tvSubTitle)as TextView
        var downloadSize = itemView.findViewById(R.id.tvMusicVideoDownloadSize)as TextView
        var image = itemView.findViewById(R.id.ivMusicVideoDownload)as ImageView
        var cvMain = itemView.findViewById(R.id.cvMain)as ConstraintLayout
        var rlDownloadeStates = itemView.findViewById(R.id.rlDownloadeStates) as RelativeLayout
        var download = itemView.findViewById(R.id.ivMovieDownloading) as ImageView
        var videoDuration = itemView.findViewById(R.id.tvDuration)as TextView
        var rlMoreInfo = itemView.findViewById(R.id.rlMoreInfo)as RelativeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_music_video_download,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setLog("TAG", "musicvideoList: ${musicplayList?.size}")

        var musicdata = holder?.adapterPosition?.let { musicplayList.get(it) }
        holder.rlDownloadeStates.visibility = View.GONE
        holder.download.visibility = View.GONE
        holder.downloadSize.visibility = View.GONE
        holder.Title.text = musicdata?.data?.title
        holder.subTitle.text = musicdata?.data?.subtitle
        if (musicdata != null) {
            if (!TextUtils.isEmpty(musicdata?.data?.duration.toString()) && musicdata.data.duration > 0){
                holder.videoDuration.show()
                holder.videoDuration.text = DateUtils.formatElapsedTime(musicdata?.data?.duration!!.toLong())
            }else{
                holder.videoDuration.hide()
            }
        }
        ImageLoader.loadImage(context,holder.image,musicdata?.data?.image!!,R.drawable.bg_gradient_placeholder)

        holder.cvMain.setOnClickListener {
            if (itemClick != null) {
                holder?.adapterPosition?.let { it1 -> itemClick.OnTvShowItemClick(it1) }
            }
        }
        holder.rlMoreInfo.setOnClickListener{
            holder?.adapterPosition?.let { it1 -> itemClick.OnLibraryItemThreeDotClick(it1) }
        }
    }

    override fun getItemCount(): Int {
        return musicplayList.size
    }
    interface onItemClickListener{
        fun OnTvShowItemClick(adapterPosition: Int)
        fun OnLibraryItemThreeDotClick(adapterPosition: Int)
    }
}