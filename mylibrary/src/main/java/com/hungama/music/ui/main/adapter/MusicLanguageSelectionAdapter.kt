package com.hungama.music.ui.main.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.hungama.music.R
import com.hungama.music.data.model.MusicLanguageSelectionModel
import com.hungama.music.model.LangItem
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.customview.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class MusicLanguageSelectionAdapter(
    val type:Int,
    val ctx: Context,
    val lists: List<MusicLanguageSelectionModel.Data.Body.Row.Item?>?,
    val onChildItemClick: OnChildItemClick?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    var list: List<MusicLanguageSelectionModel.Data.Body.Row.Item?>? = lists

    var itemClick: ((LangItem) -> Unit)? = null
    var margin = 0.0
    var marginTop = 0.0
    var marginBottom = 0.0
    var marginStart = 0.0
    var marginEnd = 0.0
    var itemPadding = 0.0
    var itemPaddingTop = 0.0
    var itemPaddingBottom = 0.0
    var itemPaddingStart = 0.0
    var itemPaddingEnd = 0.0
    var parentStartSpacing = 0.0
    var noOfColums = 0.0
    var itemWidth = 0.0
    var lineOne = 0.0
    var lineTwo = 0.0
    var lineThree = 0.0
    var textSize = 0.0
    var imageHeightByAspectRatio = 0.0
    var itemHeight = 0.0
    var maxScreenWidth: Int = 0

    private inner class MovieGenreSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        /*var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)*/
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivSelection:ImageView = itemView.findViewById(R.id.ivSelection)
        val llMain: RelativeLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            /*if (list.releasedate != null) {

                tvSubTitle.text = "formattedDate"
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
*/
            if (list!!.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }

            if (list.isSelected){
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
            }else{
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
            }

            llMain.setOnClickListener {
                setLog("IsSelectedLang", "true")
                if (list.isSelected){
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
                    list.isSelected = false
                }else{
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
                    list.isSelected = true
                }
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position,list.isSelected)
                }
            }
        }
    }
    private inner class MusicSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
//        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        /*var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)*/
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivSelection:ImageView = itemView.findViewById(R.id.ivSelection)
        val llMain: RelativeLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!

//            if (list!!.title != null) {
//                tvTitle.text = list.title
//                tvTitle.visibility = View.VISIBLE
//            } else {
//                tvTitle.visibility = View.GONE
//            }

            /*if (list.releasedate != null) {

                tvSubTitle.text = "formattedDate"
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
*/
            llMain.layoutParams.width = itemWidth.toInt()
            llMain.layoutParams.height = imageHeightByAspectRatio.toInt()
            llMain.requestLayout()

            ivUserImage.layoutParams.width = itemWidth.toInt()
            ivUserImage.layoutParams.height = imageHeightByAspectRatio.toInt()
            ivUserImage.requestLayout()

            if (list!!.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }

            if (list.isSelected){
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
            }else{
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
            }

            llMain.setOnClickListener {
                setLog("IsSelectedLang", "true")
                if (list.isSelected){
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
                    list.isSelected = false
                }else{
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
                    list.isSelected = true
                }
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position,list.isSelected)
                }
            }
        }
    }

    private inner class ArtistSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        //var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivArtistImage: ShapeableImageView = itemView.findViewById(R.id.ivArtistImage)
        val ivSelection:ImageView = itemView.findViewById(R.id.ivSelection)
        val llMain: RelativeLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list2 = list?.get(position)?.data

            if (list2?.title != null) {
                tvTitle.text = list2.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            /*if (list.releasedate != null) {

                tvSubTitle.text = "formattedDate"
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
*/
            llMain.layoutParams.width = itemWidth.toInt()
            llMain.layoutParams.height = itemHeight.toInt()
            llMain.requestLayout()

            ivArtistImage.layoutParams.width = itemWidth.toInt() - ctx.resources.getDimensionPixelSize(R.dimen.dimen_52).toDouble().toInt()
            ivArtistImage.layoutParams.height = itemWidth.toInt() - ctx.resources.getDimensionPixelSize(R.dimen.dimen_52).toDouble().toInt()
            ivArtistImage.requestLayout()
            if (list2?.image != null) {
                ImageLoader.loadImageRound(
                    ctx,
                    ivArtistImage,
                    list2.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            else{
                ivArtistImage.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.bg_gradient_placeholder))
            }
            if (list?.get(position)?.isSelected!!){
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
            }else{
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
            }
            llMain.setOnClickListener {
                setLog("IsSelectedLang", "true")
                if (list?.get(position)?.isSelected!!){
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
                    list?.get(position)?.isSelected = false
                    //lists?.get(position)?.isSelected = false
                }else{
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
                    list?.get(position)?.isSelected = true
                    //lists?.get(position)?.isSelected = true
                }
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, list?.get(position)?.isSelected!!)
                }
            }
        }
    }

    private inner class VideoSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        //        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        /*var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)*/
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivSelection:ImageView = itemView.findViewById(R.id.ivSelection)
        val llMain: RelativeLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!
            setLog("TAG", "bind: this adapter is working")
            setLog("TAG", "bind: "+list.title)
//            if (list!!.title != null) {
//                tvTitle.text = list.title
//                tvTitle.visibility = View.VISIBLE
//            } else {
//                tvTitle.visibility = View.GONE
//            }

            /*if (list.releasedate != null) {

                tvSubTitle.text = "formattedDate"
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
*/
            llMain.layoutParams.width = itemWidth.toInt()
            llMain.layoutParams.height = imageHeightByAspectRatio.toInt()
            llMain.requestLayout()

            ivUserImage.layoutParams.width = itemWidth.toInt()
            ivUserImage.layoutParams.height = imageHeightByAspectRatio.toInt()
            ivUserImage.requestLayout()
            if (list!!.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            if (list.isSelected){
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
            }else{
                ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
            }
            llMain.setOnClickListener {
                setLog("IsSelectedLang", "true")
                if (list.isSelected){
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_un_selection))
                    list.isSelected = false
                }else{
                    ivSelection.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_round_selection))
                    list.isSelected = true
                }
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position,list.isSelected)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        marginStart = 0.0
        marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
        marginTop = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
        marginBottom = 0.0
        itemPaddingStart = 0.0
        itemPaddingEnd = 0.0
        itemPaddingTop = 0.0
        itemPaddingBottom = 0.0

        noOfColums = 2.0
        maxScreenWidth = CommonUtils.getDeviceWidth(ctx)
        parentStartSpacing = ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
        itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
        lineOne = 0.0
        lineTwo = 0.0
        lineThree = 0.0
        textSize = lineOne + lineTwo + lineThree
        imageHeightByAspectRatio = itemWidth * 9 / 16
        itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
        if (type == 1){
            return MusicSelectionViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.muusc_selection_item_layout, parent, false)
            )
        }else if (type == 2){
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            marginTop = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            noOfColums = 3.0
            maxScreenWidth = CommonUtils.getDeviceWidth(ctx)
            parentStartSpacing = ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 2.3 / 2
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return ArtistSelectionViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.artist_selection_item_layout, parent, false)
            )
        }else if(type == 3){
            return VideoSelectionViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.muusc_selection_item_layout, parent, false)
            )
        }else if(type == 4){
            return MovieGenreSelectionViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.video_language_selection_item_layout, parent, false)
            )
        }else{
            return MusicSelectionViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.muusc_selection_item_layout, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (type == 1){
            (holder as MusicSelectionViewHolder).bind(position)
        }else if(type == 2){
            (holder as ArtistSelectionViewHolder).bind(position)
        }else if(type == 3){
            (holder as VideoSelectionViewHolder).bind(position)
        }else if(type == 4){
            (holder as MovieGenreSelectionViewHolder).bind(position)
        }else{
            (holder as MusicSelectionViewHolder).bind(position)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, selected: Boolean)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    list = lists
                } else {
                    val resultList = ArrayList<MusicLanguageSelectionModel.Data.Body.Row.Item?>()
                    for (row in lists!!) {
                        if (row?.data?.title?.lowercase(Locale.ROOT)?.contains(charSearch.lowercase(Locale.ROOT)!!)!!
                        ) {
                            resultList.add(row)
                        }
                    }
                    list = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = list
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                list = results?.values as ArrayList<MusicLanguageSelectionModel.Data.Body.Row.Item?>?
                notifyDataSetChanged()
            }

        }
    }
}