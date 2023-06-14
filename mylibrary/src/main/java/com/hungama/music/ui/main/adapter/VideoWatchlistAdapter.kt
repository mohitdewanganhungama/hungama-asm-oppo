package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Color
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
import com.hungama.music.ui.main.view.fragment.VideoWatchlistFragment
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.hide

class VideoWatchlistAdapter(var context:Context, val contentId: Int,val onItemClick: OnItemClick?):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var musicVideoList = ArrayList<BookmarkDataModel.Data.Body.Row>()

    internal fun setMusicVideoList(musicList: ArrayList<BookmarkDataModel.Data.Body.Row>){
        musicVideoList = musicList
        notifyDataSetChanged()
    }

    class TVShowViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var tvShowtitle = itemView.findViewById(R.id.tvTVShowTitle)as TextView
        var tvShowsub = itemView.findViewById(R.id.tvTVShowSubTitle)as TextView
        var tvTVShowDownload = itemView.findViewById(R.id.tvTVShowDownload)as TextView
        var tvShowlanguage = itemView.findViewById(R.id.tvTVShowLanguage)as TextView
        var tvShowaction = itemView.findViewById(R.id.tvTVShowAction)as TextView
        var tvShowDownloadSize = itemView.findViewById(R.id.tvTvShowSize)as TextView
        var tvShowimage = itemView.findViewById(R.id.ivTVShowImage)as ImageView
        var tvShowDownloading = itemView.findViewById(R.id.ivTVShowDownloading)as ImageView
        var cvMain = itemView.findViewById(R.id.cvMain)as ConstraintLayout
        var cvBgView = itemView.findViewById(R.id.cvBgView) as ConstraintLayout
        var clSelection = itemView.findViewById(R.id.clSelection) as ConstraintLayout
        var ivSelection = itemView.findViewById(R.id.ivSelection) as ImageView
        var rlMoreInfo = itemView.findViewById(R.id.rlMoreInfo) as RelativeLayout
        var vDevider = itemView.findViewById(R.id.vDevider) as View
        var view2 = itemView.findViewById(R.id.view2) as View

    }
    inner class MovieViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById(R.id.tvMovieTitle) as TextView
        var desc = itemView.findViewById(R.id.tvMovieBody) as TextView
        var movielanguage = itemView.findViewById(R.id.tvMoviewLanguage) as TextView
        var moviaction = itemView.findViewById(R.id.tvMovieAction) as TextView
        var size = itemView.findViewById(R.id.downloadSize) as TextView
        var moviedate = itemView.findViewById(R.id.tvMoviedate) as TextView
        var movieimage = itemView.findViewById(R.id.ivMoviesDownloadImage) as ImageView
        var download = itemView.findViewById(R.id.ivMovieDownloading) as ImageView
        var rlMoreInfo = itemView.findViewById(R.id.rlMoreInfo) as RelativeLayout
        var cvMain = itemView.findViewById(R.id.cvMain) as ConstraintLayout
        var checkiamge = itemView.findViewById(R.id.checkiamge) as ImageView
        var rlDownloadeStates = itemView.findViewById(R.id.rlDownloadeStates) as RelativeLayout
        var cvBgView = itemView.findViewById(R.id.cvBgView) as ConstraintLayout
        var clSelection = itemView.findViewById(R.id.clSelection) as ConstraintLayout
        var ivSelection = itemView.findViewById(R.id.ivSelection) as ImageView




    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var Title = itemView.findViewById(R.id.tvTitle)as TextView
        var subTitle = itemView.findViewById(R.id.tvSubTitle)as TextView
        var downloadSize = itemView.findViewById(R.id.tvMusicVideoDownloadSize)as TextView
        var image = itemView.findViewById(R.id.ivMusicVideoDownload)as ImageView
        var cvMain = itemView.findViewById(R.id.cvMain)as ConstraintLayout
        var rlMoreInfo = itemView.findViewById(R.id.rlMoreInfo)as RelativeLayout
        var rlDownloadeStates = itemView.findViewById(R.id.rlDownloadeStates) as RelativeLayout
        var download = itemView.findViewById(R.id.ivMovieDownloading) as ImageView
        var cvBgView = itemView.findViewById(R.id.cvBgView) as ConstraintLayout
        var tvDuration = itemView.findViewById(R.id.tvDuration)as TextView
        var clSelection = itemView.findViewById(R.id.clSelection) as ConstraintLayout
        var ivSelection = itemView.findViewById(R.id.ivSelection) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(contentId== VideoWatchlistFragment.MOVIE_ID || contentId== VideoWatchlistFragment.SHORT_FILMS){
            var view = LayoutInflater.from(context).inflate(R.layout.row_movies_download,parent,false)
            return MovieViewHolder(view)
        }else if(contentId== VideoWatchlistFragment.TV_SHOW){
            var view = LayoutInflater.from(context).inflate(R.layout.row_tvshow_download,parent,false)
            return TVShowViewHolder(view)
        }else if(contentId== VideoWatchlistFragment.SHORT_VIDEO){
            var view = LayoutInflater.from(context).inflate(R.layout.row_music_video_download,parent,false)
            return ViewHolder(view)
        }else{
            var view = LayoutInflater.from(context).inflate(R.layout.row_music_video_download,parent,false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return musicVideoList.size
    }

    interface OnItemClick {
        fun onMusicItemVideoClick(childPosition: Int)
        fun onMusicItemThreeDotClick(childPosition: Int)
        fun onItemSelection(data: BookmarkDataModel.Data.Body.Row, childPosition: Int, isSelected:Int)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var datalist = musicVideoList[position]
        if(holder is MovieViewHolder){
            if (position % 2 == 0){
                holder.cvBgView.setBackgroundColor(0)
            }else{
                holder.cvBgView.setBackgroundColor(Color.parseColor("#33000000"))
            }
            //holder.checkiamge.visibility = View.GONE
            holder.download.visibility = View.GONE
            holder.rlDownloadeStates.visibility = View.GONE
            holder.size.visibility = View.GONE
            holder.moviedate.visibility = View.GONE
            holder.title.text = datalist?.data?.title
            holder.desc.text = datalist?.data?.subtitle
            holder.size.text = ""+datalist?.data?.duration
            holder.moviedate.text = datalist?.data?.releasedate
            holder.moviaction.hide()
            ImageLoader.loadImage(context,holder.movieimage,datalist?.data?.image!!,R.drawable.bg_gradient_placeholder)

            holder.download.setImageResource(R.drawable.image_movie_downloading_icon)
            holder.movielanguage.text = ""+datalist.data?.misc?.lang
            holder.movielanguage.hide()

            if (datalist?.data?.isSelected == 1){
                holder.clSelection?.visibility = View.VISIBLE
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else if (datalist?.data?.isSelected == 2){
                holder.clSelection?.visibility = View.VISIBLE
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else{
                holder.clSelection?.visibility = View.GONE
            }
            holder.clSelection?.setOnClickListener {
                holder.clSelection?.visibility = View.VISIBLE
                if (datalist?.data?.isSelected == 1){
                    datalist?.data?.isSelected = 2
                    holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
                }else if (datalist?.data?.isSelected == 2){
                    datalist?.data?.isSelected = 1
                    holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
                }
                if (onItemClick != null) {
                    onItemClick.onItemSelection(datalist, position,datalist.data.isSelected)
                }
            }

            holder?.cvMain?.setOnClickListener {
                if(onItemClick!=null){
                    onItemClick?.onMusicItemVideoClick(holder.adapterPosition)
                }
            }

            holder?.rlMoreInfo?.setOnClickListener {
                if(onItemClick!=null){
                    onItemClick?.onMusicItemThreeDotClick(holder.adapterPosition)
                }
            }



//            if(position%2==0){
//                holder.cvMain.setBackgroundColor(ContextCompat.getColor(context,R.color.home_bg_color))
//            }else{
//                holder.cvMain.setBackgroundColor(ContextCompat.getColor(context,R.color.active_stock_color))
//            }
        }else if(holder is TVShowViewHolder){
            if (position % 2 == 0){
                holder.cvBgView.setBackgroundColor(0)
            }else{
                holder.cvBgView.setBackgroundColor(Color.parseColor("#33000000"))
            }

            holder.tvShowDownloading.visibility = View.GONE
            holder.tvShowDownloadSize.visibility = View.GONE
            holder.tvShowtitle.text =datalist?.data?.title
            holder.tvShowsub.text = datalist?.data?.subtitle
            holder.tvTVShowDownload.text = datalist?.data?.misc?.nudity
            holder.tvTVShowDownload.hide()
            holder.vDevider.hide()
            holder.view2.hide()
            holder.tvShowaction.hide()
            holder.tvShowlanguage.text = ""+datalist?.data?.misc?.lang
            holder.tvShowlanguage.hide()
            ImageLoader.loadImage(context,holder.tvShowimage,datalist?.data?.image!!,R.drawable.bg_gradient_placeholder)
            holder.tvShowDownloadSize.text = ""+datalist.data?.duration

            if (datalist?.data?.isSelected == 1){
                holder.clSelection?.visibility = View.VISIBLE
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else if (datalist?.data?.isSelected == 2){
                holder.clSelection?.visibility = View.VISIBLE
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else{
                holder.clSelection?.visibility = View.GONE
            }
            holder.clSelection?.setOnClickListener {
                holder.clSelection?.visibility = View.VISIBLE
                if (datalist?.data?.isSelected == 1){
                    datalist?.data?.isSelected = 2
                    holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
                }else if (datalist?.data?.isSelected == 2){
                    datalist?.data?.isSelected = 1
                    holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
                }
                if (onItemClick != null) {
                    onItemClick.onItemSelection(datalist, position,datalist.data.isSelected)
                }
            }


            holder?.cvMain?.setOnClickListener {
                if(onItemClick!=null){
                    onItemClick?.onMusicItemVideoClick(holder.adapterPosition)
                }
            }

            holder?.rlMoreInfo?.setOnClickListener {
                if(onItemClick!=null){
                    onItemClick?.onMusicItemThreeDotClick(holder.adapterPosition)
                }
            }

        } else if(holder is ViewHolder){
            if (position % 2 == 0){
                holder.cvBgView.setBackgroundColor(0)
            }else{
                holder.cvBgView.setBackgroundColor(Color.parseColor("#33000000"))
            }
            var musicdata = musicVideoList?.get(holder.adapterPosition)
            holder.rlDownloadeStates.visibility = View.GONE
            holder.download.visibility = View.GONE
            holder.downloadSize.visibility = View.GONE
            holder.Title.text = musicdata?.data?.title
            holder.subTitle.text = musicdata?.data?.subtitle
            holder.downloadSize.text = ""+musicdata?.data?.duration
            holder.tvDuration.text = DateUtils.formatElapsedTime(musicdata?.data?.duration!!.toLong())
            ImageLoader.loadImage(context,holder.image,musicdata?.data?.image!!,R.drawable.bg_gradient_placeholder)

            if (datalist?.data?.isSelected == 1){
                holder.clSelection?.visibility = View.VISIBLE
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else if (datalist?.data?.isSelected == 2){
                holder.clSelection?.visibility = View.VISIBLE
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else{
                holder.clSelection?.visibility = View.GONE
            }
            holder.clSelection?.setOnClickListener {
                holder.clSelection?.visibility = View.VISIBLE
                if (datalist?.data?.isSelected == 1){
                    datalist?.data?.isSelected = 2
                    holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
                }else if (datalist?.data?.isSelected == 2){
                    datalist?.data?.isSelected = 1
                    holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
                }
                if (onItemClick != null) {
                    onItemClick.onItemSelection(datalist, position,datalist.data.isSelected)
                }
            }

            holder.cvMain.setOnClickListener {
                if (onItemClick != null) {
                    onItemClick.onMusicItemVideoClick(position)
                }
            }

            holder?.rlMoreInfo?.setOnClickListener {
                if(onItemClick!=null){
                    onItemClick?.onMusicItemThreeDotClick(holder.adapterPosition)
                }
            }


        }
    }
}