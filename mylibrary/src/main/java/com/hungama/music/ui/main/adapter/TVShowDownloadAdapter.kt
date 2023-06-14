package com.hungama.music.ui.main.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.TVShowDownloadModel

class TVShowDownloadAdapter(var context: Context,var itemClick: onItemClickListener):RecyclerView.Adapter<TVShowDownloadAdapter.ViewHolder>() {

    var tvShowLit = emptyList<TVShowDownloadModel>()

    internal fun setTvShow(showlistdata:List<TVShowDownloadModel>){
       tvShowLit = showlistdata
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),View.OnClickListener{
        var tvShowtitle = itemView.findViewById(R.id.tvTVShowTitle)as TextView
        var tvShowsub = itemView.findViewById(R.id.tvTVShowSubTitle)as TextView
        var subscriber = itemView.findViewById(R.id.tvTVShowDownload)as TextView
        var tvShowlanguage = itemView.findViewById(R.id.tvTVShowLanguage)as TextView
        var tvShowaction = itemView.findViewById(R.id.tvTVShowAction)as TextView
        var tvShowDownloadSize = itemView.findViewById(R.id.tvTvShowSize)as TextView
        var tvShowimage = itemView.findViewById(R.id.ivTVShowImage)as ImageView
        var tvShowDownloading = itemView.findViewById(R.id.ivTVShowDownloading)as ImageView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            itemClick.OnTvShowItemClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_tvshow_download,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var datalist = tvShowLit[position]
        Log.v("MyDtaLies","List"+datalist.showtitle+" "+datalist.showsubtitle)
        holder.tvShowtitle.text = datalist.showtitle
        holder.tvShowsub.text = datalist.showsubtitle
        holder.subscriber.text = datalist.showsubscriber
        holder.tvShowlanguage.text = datalist.showLanguage
        holder.tvShowaction.text = datalist.showType
        holder.tvShowimage.setImageResource(datalist.showImage)
        holder.tvShowDownloading.setImageResource(datalist.showdownload)
        holder.tvShowDownloadSize.text = datalist.downloadsize
    }

    override fun getItemCount(): Int {
        return tvShowLit.size
    }
    interface onItemClickListener{
        fun OnTvShowItemClick()
    }
}