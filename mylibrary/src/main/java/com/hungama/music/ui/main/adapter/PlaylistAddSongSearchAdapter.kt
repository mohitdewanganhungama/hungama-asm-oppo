package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.RecommendedSongListRespModel
import com.hungama.music.data.model.SearchRespModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.blurview.CustomShapeBlurView

class PlaylistAddSongSearchAdapter(
    val context: Context,
    val rowList: ArrayList<BodyRowsItemsItem>,
    val onChildItemClick: OnChildItemClick?
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val ctx: Context = context

        private inner class IType12ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
            val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
            val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
            val ivAddSong: ImageView = itemView.findViewById(R.id.ivAddSong)
            fun bind(position: Int) {
                var searchdata = rowList[position]

                if (!TextUtils.isEmpty(searchdata?.data?.title)) {
                    tvTitle.text = searchdata?.data?.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (!TextUtils.isEmpty(searchdata?.data?.subTitle)) {
                    tvSubTitle.text = searchdata?.data?.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }


                if (searchdata?.data?.image != null) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        searchdata?.data?.image!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onPlaySongClick(position)
                    }
                }

                ivAddSong.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onAddSongClick(position)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            setLog("TAG", "setPlaylistDetailsListData onCreateViewHolder: list:${rowList?.size}")
            return IType12ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_playlist_add_song, parent, false)
            )
        }

        override fun getItemCount(): Int {
            setLog("TAG", "setPlaylistDetailsListData 5 getItemCount ${rowList?.size}")
            return rowList.size
        }
        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as IType12ViewHolder).bind(position)
        }

        interface OnChildItemClick {
            fun onPlaySongClick(childPosition: Int)
            fun onAddSongClick(childPosition: Int)
        }
    }