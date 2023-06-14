package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.SearchRecommendationModel
import com.hungama.music.data.model.SearchRespModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.circleimageview.CircleImageView

class SearchRecommendedAdapter(
    var context: Context,
    var searchItem: SearchRecommendedAdapter.SearchResult,
    val rowList: SearchRecommendationModel
)
    : RecyclerView.Adapter<SearchRecommendedAdapter.ViewHolder>() {


    var searchDataList = SearchRecommendationModel()

    private var hasInitParentDimensions = false
    private var maxScreenWidth: Int = 0
    private var maxScreenHeight: Int = 0
    private var maxImageAspectRatio: Float = 1f

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

    internal fun setSearchlist(searchdata: SearchRecommendationModel){
        searchDataList = searchdata
        notifyDataSetChanged()
    }

    /*companion object {
        private val CAT_IMAGE_IDS = intArrayOf(
            R.drawable.cat_1,
            R.drawable.cat_2,
            R.drawable.cat_3,
            R.drawable.cat_4,
            R.drawable.cat_5,
            R.drawable.cat_6,
            R.drawable.cat_7,
            R.drawable.cat_8,
            R.drawable.cat_10,
        )
    }*/

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView),View.OnClickListener{
        var image : ImageView
        var clMain : ConstraintLayout
        var rlRadio : RelativeLayout
        var ivRadioImage : CircleImageView
        var ivRadioImage2 : ImageView
        var ivArtistImage : CircleImageView
        init {

            itemView.setOnClickListener(this)

            image = itemView.findViewById(R.id.ivSearch)
            clMain = itemView.findViewById(R.id.clMain)
            rlRadio = itemView.findViewById(R.id.rlRadio)
            ivRadioImage = itemView.findViewById(R.id.ivRadioImage)
            ivRadioImage2 = itemView.findViewById(R.id.ivRadioImage2)
            ivArtistImage = itemView.findViewById(R.id.ivArtistImage)
        }

        override fun onClick(p0: View?){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        maxScreenWidth = CommonUtils.getDeviceWidth(context)
        maxScreenHeight = CommonUtils.getDeviceHeight(context)
        maxImageAspectRatio = maxScreenWidth.toFloat() / maxScreenHeight.toFloat()
        var view = LayoutInflater.from(context).inflate(R.layout.search_recommended_row_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var searchdata = searchDataList?.get(position)
        if (position == 0 || position == 1){
            Utils.setMarginsTop(holder.clMain, 0)
        }else{
            Utils.setMarginsTop(holder.clMain, context.resources.getDimensionPixelSize(R.dimen.dimen_10))
        }
        //holder.image.setImageResource(searchdata?.data?.image!!)
        if (CommonUtils.isRadioContent(searchdata?.data?.type?.toString())){
            holder.image.visibility = View.GONE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.VISIBLE
            marginStart = 0.0
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            if (position == 0 || position == 1){
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_0).toDouble()
            }else{
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            }

            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 2.0
            parentStartSpacing = (context.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()+context.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()) / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            holder.ivRadioImage.layoutParams.height = imageHeightByAspectRatio.toInt()
            holder.ivRadioImage.layoutParams.width = itemWidth.toInt()
            holder.ivRadioImage2.layoutParams.height = imageHeightByAspectRatio.toInt()
            holder.ivRadioImage2.layoutParams.width = itemWidth.toInt()
            if (!TextUtils.isEmpty(searchdata?.data?.image)){

                ImageLoader.loadImage(
                    context,
                    holder.ivRadioImage,
                    searchdata?.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.ivRadioImage,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
        }else if (CommonUtils.isArtistContent(searchdata?.data?.type?.toString())){

            marginStart = 0.0
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            if (position == 0 || position == 1){
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_0).toDouble()
            }else{
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            }
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 2.0
            parentStartSpacing = (context.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()+context.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()) / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            /*if (position % 2 == 0){
                imageHeightByAspectRatio = itemWidth * 9 / 16
                holder.image.visibility = View.VISIBLE
                holder.ivArtistImage.visibility = View.GONE
                holder.rlRadio.visibility = View.GONE
                itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
                holder.image.layoutParams.height = imageHeightByAspectRatio.toInt()
                holder.image.layoutParams.width = itemWidth.toInt()
                if (!TextUtils.isEmpty(searchdata?.data?.image)){
                    ImageLoader.loadImage(
                        context,
                        holder.image,
                        searchdata?.data?.image!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }else{
                    ImageLoader.loadImage(
                        context,
                        holder.image,
                        "",
                        R.drawable.bg_gradient_placeholder
                    )
                }
            }else{*/
                imageHeightByAspectRatio = itemWidth * 1 / 1
                holder.image.visibility = View.GONE
                holder.ivArtistImage.visibility = View.VISIBLE
                holder.rlRadio.visibility = View.GONE

                itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
                holder.ivArtistImage.layoutParams.height = imageHeightByAspectRatio.toInt()
                holder.ivArtistImage.layoutParams.width = itemWidth.toInt()
                if (!TextUtils.isEmpty(searchdata?.data?.image)){
                    ImageLoader.loadImage(
                        context,
                        holder.ivArtistImage,
                        searchdata?.data?.image!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }else{
                    ImageLoader.loadImage(
                        context,
                        holder.ivArtistImage,
                        "",
                        R.drawable.bg_gradient_placeholder
                    )
                }
            //}

        }else if (CommonUtils.isMovieContent(searchdata?.data?.type?.toString())){

            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            marginStart = 0.0
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            if (position == 0 || position == 1){
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_0).toDouble()
            }else{
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            }
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 2.0
            parentStartSpacing = (context.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()+context.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()) / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 2

            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            holder.image.layoutParams.height = imageHeightByAspectRatio.toInt()
            holder.image.layoutParams.width = itemWidth.toInt()
            if (!TextUtils.isEmpty(searchdata?.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchdata?.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

        }else if (CommonUtils.isMusicVideoContent(searchdata?.data?.type?.toString())){

            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            marginStart = 0.0
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            if (position == 0 || position == 1){
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_0).toDouble()
            }else{
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            }
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 2.0
            parentStartSpacing = (context.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()+context.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()) / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16

            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            holder.image.layoutParams.height = imageHeightByAspectRatio.toInt()
            holder.image.layoutParams.width = itemWidth.toInt()
            if (!TextUtils.isEmpty(searchdata?.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchdata?.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

        }else if (CommonUtils.isTVShowContent(searchdata?.data?.type?.toString())){

            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            marginStart = 0.0
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            if (position == 0 || position == 1){
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_0).toDouble()
            }else{
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            }
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 2.0
            parentStartSpacing = (context.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()+context.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()) / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 4

            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            holder.image.layoutParams.height = imageHeightByAspectRatio.toInt()
            holder.image.layoutParams.width = itemWidth.toInt()
            if (!TextUtils.isEmpty(searchdata?.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchdata?.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

        }else{
            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            marginStart = 0.0
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            if (position == 0 || position == 1){
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_0).toDouble()
            }else{
                marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            }
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 2.0
            parentStartSpacing = (context.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()+context.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()) / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            /*if (position % 2 == 0){
                imageHeightByAspectRatio = itemWidth * 3 / 2
            }else{
                imageHeightByAspectRatio = itemWidth * 1 / 1
            }*/
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            holder.image.layoutParams.height = imageHeightByAspectRatio.toInt()
            holder.image.layoutParams.width = itemWidth.toInt()
            if (!TextUtils.isEmpty(searchdata?.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchdata?.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
        }
        holder.clMain.setOnClickListener {
            if (searchItem != null){
                if (searchdata != null) {
                    searchItem.SearchItemClick(searchdata, position)
                }
            }
        }

        /*if (!TextUtils.isEmpty(searchdata?.data?.image)){
            ImageLoader.loadImage(
                context,
                holder.ivArtistImage,
                searchdata?.data?.image!!,
                R.drawable.bg_gradient_placeholder
            )
            *//*Glide.with(context)
                .asBitmap()
                .load(searchdata?.data?.image!!)
                .into(object : SimpleTarget<Bitmap>(){
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val w = bitmap.getWidth()
                        val h = bitmap.getHeight()

                        holder.ivArtistImage.layoutParams.height = h
                        holder.ivArtistImage.layoutParams.width = w
                        holder.ivArtistImage.requestLayout()

                        holder.ivArtistImage.setImageBitmap(bitmap);
                    }

                })*//*
        }else{
            ImageLoader.loadImage(
                context,
                holder.ivArtistImage,
                "",
                R.drawable.bg_gradient_placeholder
            )
        }*/
        /*val pos = position % CAT_IMAGE_IDS.size
        holder.ivArtistImage.setImageResource(CAT_IMAGE_IDS[pos])*/
        /*val lp = holder.ivArtistImage.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1f
        }*/
    }

    override fun getItemCount(): Int {
        return searchDataList.size
    }
   //override fun getItemCount() = CAT_IMAGE_IDS.size * 4
    interface SearchResult{
        fun SearchItemClick(searchdata: SearchRecommendationModel.SearchRecommendationModelItem, position: Int)
    }
}