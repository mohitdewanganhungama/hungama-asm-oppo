package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.hungama.music.R
import com.hungama.music.data.model.LibraryMusicModel
import com.hungama.music.ui.main.view.fragment.LibraryMusicAllFragment
import com.hungama.music.ui.main.view.fragment.LibraryPodcastFragment
import com.hungama.music.ui.main.view.fragment.VideoDownloadAllFragment
import com.hungama.music.ui.main.view.fragment.VideoWatchlistFragment
import com.hungama.music.utils.ImageLoader

class LibraryMusicAdapter(
    var context: Context,
    var musicPlaylist: ArrayList<LibraryMusicModel>,
    var onClick: PlayListItemClick
) : RecyclerView.Adapter<LibraryMusicAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var playlistTitle = itemView.findViewById(R.id.tvTitle) as TextView
        var playlistSubTitle = itemView.findViewById(R.id.tvSubTitle) as TextView
        var playlistshape = itemView.findViewById(R.id.ivLibraryMusic) as ShapeableImageView
        var llMain = itemView.findViewById(R.id.llMain) as LinearLayoutCompat
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_all_library, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var musicData = musicPlaylist[position]
        holder.playlistTitle.text = musicData.Title
        if (!TextUtils.isEmpty(musicData.SubTitle)) {
            holder.playlistSubTitle.text = musicData.SubTitle
            holder.playlistSubTitle.visibility = View.VISIBLE
        } else {
            holder.playlistSubTitle.visibility = View.GONE
        }

        if (musicData?.id.equals("" + LibraryMusicAllFragment.CREATE_PLAYLIST_ID)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.bottom_dialog_delete_btn_background)
//            holder.playlistshape.setBackgroundResource(R.drawable.ic_create_playlist_add)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.ic_create_playlist_add)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_add_white
            )
//            holder.add.visibility = View.VISIBLE
        }
        else if (musicData?.id.equals("" + LibraryMusicAllFragment.MY_DEVICE)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.bottom_dialog_delete_btn_background)
//            holder.playlistshape.setBackgroundResource(R.drawable.ic_create_playlist_add)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.ic_create_playlist_add)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_my_device_icon
            )
//            holder.add.visibility = View.VISIBLE
        }
        else if(musicData?.id.equals(""+ LibraryMusicAllFragment.DOWNLOADING_PROGRESS) || musicData?.id.equals(""+ LibraryPodcastFragment.DOWNLOADING_PROGRESS)){
//            holder.playlistshape.setBackgroundResource(R.drawable.image_downloading_in_progress)
//            holder.playlistshape.setImageResource(R.drawable.download_img)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_downloading_in_progress)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.download_img
            )
        } else if (musicData?.id.equals("" + LibraryMusicAllFragment.DOWNLOADED_SONGS)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.image_downloaded_rectangle)
//            holder.playlistshape.setImageResource(R.drawable.ic_chart_download)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_downloaded_rectangle)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_chart_download
            )
        } else if (musicData?.id.equals("" + LibraryMusicAllFragment.FAVORITED_SONGS)) {
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_favorite_rectangle)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.heart
            )
//            holder.playlistshape.setImageResource(R.drawable.heart)
        }else  if(musicData.id.equals(""+ LibraryMusicAllFragment.DOWNLOADED_PODCAST) || musicData.id.equals(""+ LibraryPodcastFragment.DOWNLOADED_PODCAST)){
//            holder.playlistshape.setBackgroundResource(R.drawable.image_downloaded_podcast_rectangle)
//            holder.playlistshape.setImageResource(R.drawable.ic_chart_download)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_downloaded_podcast_rectangle)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_chart_download
            )

        } else if (musicData?.id.equals("" + LibraryPodcastFragment.FOLLOW_MORE_PODCAST_ID)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.ic_create_playlist_add)
//            holder.playlistshape.setImageResource(R.drawable.ic_add_podcast)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.ic_create_playlist_add)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_add_podcast
            )
        } else if (musicData?.id.equals("" + LibraryPodcastFragment.FOLLOW_MORE_PODCAST_ID)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.ic_create_playlist_add)
//            holder.playlistshape.setImageResource(R.drawable.ic_add_podcast)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.ic_create_playlist_add)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_add_podcast
            )
        }else  if(musicData?.id.equals(""+ VideoDownloadAllFragment.MOVIE_LISTING) || musicData?.id.equals(""+ VideoWatchlistFragment.MOVIE_ID) || musicData?.id.equals(""+ VideoWatchlistFragment.MOVIE_ID)){
//            holder.playlistshape.setBackgroundResource(R.drawable.image_rectagle_movie)
//            holder.playlistshape.setImageResource(R.drawable.image_movie)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_rectagle_movie)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.image_movie
            )
        }else  if(musicData?.id.equals(""+ VideoDownloadAllFragment.TV_SHOW_LISTING) || musicData?.id.equals(""+ VideoWatchlistFragment.TV_SHOW)){
//            holder.playlistshape.setBackgroundResource(R.drawable.image_rectamgle_tvshow)
//            holder.playlistshape.setImageResource(R.drawable.image_tvshow)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_rectamgle_tvshow)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.image_tvshow
            )

        } else if (musicData?.id.equals("" + VideoDownloadAllFragment.MUSIC_VIDEO_SONGS)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.image_rectangle_musicvideo)
//            holder.playlistshape.setImageResource(R.drawable.image_musicvideo)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.image_rectangle_musicvideo)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.image_musicvideo
            )
        } else if (musicData?.id.equals("" + VideoWatchlistFragment.SHORT_VIDEO)) {
//            holder.playlistshape.setBackgroundResource(R.drawable.img_short_videos)
//            holder.playlistshape.setImageResource(R.drawable.ic_short_video)
            holder.playlistshape.background = ContextCompat.getDrawable(context,R.drawable.img_short_videos)
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                "",
                R.drawable.ic_short_video
            )
        } else {
            ImageLoader.loadImage(
                context,
                holder.playlistshape,
                musicData.image,
                R.drawable.bg_gradient_placeholder
            )

        }

        holder?.llMain?.setOnClickListener {
            if (onClick != null) {
                onClick?.libraryItemOnClick(musicData)
            }
        }
    }

    override fun getItemCount(): Int {
        return musicPlaylist.size
    }

    fun refreshData(musicplayList: java.util.ArrayList<LibraryMusicModel>) {
        this.musicPlaylist = musicplayList
        notifyDataSetChanged()
    }

    interface PlayListItemClick {
        fun libraryItemOnClick(musicData: LibraryMusicModel)
    }

}