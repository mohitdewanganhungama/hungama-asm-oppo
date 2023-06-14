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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.fragment.SearchAllTabFragment
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.blurview.CustomShapeBlurView

class SearchAdapter(
    var context: Context,
    var searchItem: SearchResult,
    val rowList: ArrayList<BodyRowsItemsItem>,
    private val tabId: Int
):RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    var searchDataList = rowList

    internal fun getSearchDataList():ArrayList<BodyRowsItemsItem>{
        return searchDataList
    }

    internal fun addData(listItems: List<BodyRowsItemsItem>) {
        val size = this.searchDataList.size
        this.searchDataList.addAll(listItems)
        val sizeNew = this.searchDataList.size
        notifyItemRangeChanged(size, sizeNew)
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView),View.OnClickListener{
        var image : ImageView
        var title : TextView
        var subTitle : TextView
        var ivBack : ImageView
        var clMain : ConstraintLayout
        var rlRadio : RelativeLayout
        var ivRadioImage : ImageView
        var ivArtistImage : ImageView
        var ivMovie : ImageView
        var ivTvShow : ImageView
        var ivMusicVideo : ImageView
        var blImage : CustomShapeBlurView
        var ivEqualizerAnim: LottieAnimationView
        var rlFreeStrip: RelativeLayout

        init {

            itemView.setOnClickListener(this)

            image = itemView.findViewById(R.id.ivSearch)
            title = itemView.findViewById(R.id.tvTitle)
            subTitle = itemView.findViewById(R.id.tvSubTitle)
            ivBack = itemView.findViewById(R.id.ivRowSearchBack)
            clMain = itemView.findViewById(R.id.clMain)
            rlRadio = itemView.findViewById(R.id.rlRadio)
            ivRadioImage = itemView.findViewById(R.id.ivRadioImage)
            ivArtistImage = itemView.findViewById(R.id.ivArtistImage)
            ivMovie = itemView.findViewById(R.id.ivMovie)
            blImage = itemView.findViewById(R.id.blImage)
            ivTvShow = itemView.findViewById(R.id.ivTvShow)
            ivMusicVideo = itemView.findViewById(R.id.ivMusicVideo)
            ivEqualizerAnim = itemView.findViewById(R.id.ivEqualizerAnim)
            rlFreeStrip = itemView.findViewById(R.id.rlFreeStrip)
        }

        override fun onClick(p0: View?){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_search_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchData = searchDataList[position]

        if (position == 0){
            Utils.setMarginsTop(holder.clMain, 0)
        }else{
            Utils.setMarginsTop(holder.clMain, context.resources.getDimensionPixelSize(R.dimen.dimen_15))
        }

        holder.rlFreeStrip.hide()

        setLog("movieRights", " " + searchData.data?.misc?.movierights?.toString())
        if (CommonUtils.isRadioContent(searchData.data?.type)){
            holder.image.visibility = View.GONE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.VISIBLE
            setLog("TAG", "onBindViewHolder: isRadioContent title"+searchData.data?.title)
            setLog("TAG", "onBindViewHolder: isRadioContent image"+searchData.data?.image!!)
            if (!TextUtils.isEmpty(searchData.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.ivRadioImage,
                    searchData.data?.image!!,
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
            holder.blImage.hide()
            holder.ivMusicVideo.hide()
            holder.ivMovie.hide()
            holder.ivTvShow.hide()
        }
        else if (CommonUtils.isArtistContent(searchData.data?.type)){
            setLog("TAG", "onBindViewHolder: is artist content is working")
            holder.ivArtistImage.visibility = View.VISIBLE
            holder.ivMovie.visibility = View.GONE
            holder.image.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            setLog("TAG", "onBindViewHolder: isArtistContent title"+searchData.data?.title)
            setLog("TAG", "onBindViewHolder: artist image"+searchData.data?.image!!)
            if (!TextUtils.isEmpty(searchData.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.ivArtistImage,
                    searchData.data?.image!!,
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
            holder.blImage.hide()
            holder.ivMusicVideo.hide()
            holder.ivMovie.hide()
            holder.ivTvShow.hide()

        }
        else if (CommonUtils.isMovieContent(searchData.data?.type)){
            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            setLog("TAG", "onBindViewHolder: isMovieContent image"+searchData.data?.title!!)
            setLog("TAG", "onBindViewHolder: isMovieContent image"+searchData.data?.image!!)
            holder.rlRadio.visibility = View.GONE
            if (!TextUtils.isEmpty(searchData.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchData.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
                ImageLoader.loadImage(
                    context,
                    holder.ivMovie,
                    searchData.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                ImageLoader.loadImage(
                    context,
                    holder.ivMovie,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
            holder.blImage.show()
            holder.ivMovie.show()
            holder.ivMusicVideo.hide()
            holder.ivTvShow.hide()
        }
        else if (CommonUtils.isTVShowContent(searchData.data?.type)){
            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            setLog("TAG", "onBindViewHolder: isTVShowContent image"+searchData.data?.title!!)
            setLog("TAG", "onBindViewHolder: isTVShowContent image"+searchData.data?.image!!)
            if (!TextUtils.isEmpty(searchData.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchData.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
                ImageLoader.loadImage(
                    context,
                    holder.ivTvShow,
                    searchData.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                ImageLoader.loadImage(
                    context,
                    holder.ivTvShow,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
            holder.blImage.show()
            holder.ivTvShow.show()
            holder.ivMusicVideo.hide()
            holder.ivMovie.hide()
        }
        else if (CommonUtils.isMusicVideoContent(searchData.data?.type))
        {
            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            setLog("TAG", "onBindViewHolder: isMusicVideoContent image"+searchData.data?.title!!)
            setLog("TAG", "onBindViewHolder: isMusicVideoContent image"+searchData.data?.image!!)
            if (!TextUtils.isEmpty(searchData.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchData.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
                ImageLoader.loadImage(
                    context,
                    holder.ivMusicVideo,
                    searchData.data?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                ImageLoader.loadImage(
                    context,
                    holder.ivMusicVideo,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
            holder.blImage.show()
            holder.ivMusicVideo.show()
            holder.ivMovie.hide()
            holder.ivTvShow.hide()
        }
        else{
            if(!CommonUtils.isUserHasGoldSubscription() && searchData.data?.misc?.movierights?.contains("AMOD") == true && CommonUtils.getSongDurationConfig().enable_minutes_quota)
                holder.rlFreeStrip.show()
            else
                holder.rlFreeStrip.hide()

            holder.image.visibility = View.VISIBLE
            holder.ivArtistImage.visibility = View.GONE
            holder.rlRadio.visibility = View.GONE
            holder.blImage.hide()
            holder.ivMusicVideo.hide()
            holder.ivMovie.hide()
            holder.ivTvShow.hide()
            if (!TextUtils.isEmpty(searchData.data?.image)){
                ImageLoader.loadImage(
                    context,
                    holder.image,
                    searchData.data?.image!!,
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

        if (!TextUtils.isEmpty(searchData.data?.title)){
            holder.title.text = searchData.data?.title
            setLog("TAG", "onBindViewHolder: isMusicVideoContent title"+searchData.data?.title)
            holder.title.visibility = View.VISIBLE
        }else{
            holder.title.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(searchData.data?.type)){
            /*var contentType=Utils.getContentTypeName(searchData.data?.type!!)
            if (contentType.equals("Audio")){
                contentType = ""
            }else if (contentType.equals("Audio Album")){
                contentType = "Album"
            }*/
            if(tabId== SearchAllTabFragment.all){
                //setLog("Search","Search contentType:${contentType} type:${searchData.data?.type} title:${searchData?.data?.title}")
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }else if(tabId== SearchAllTabFragment.song){
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE

            }else if(tabId== SearchAllTabFragment.album){
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }else if(tabId== SearchAllTabFragment.artist || tabId== SearchAllTabFragment.radio){
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }else if(tabId== SearchAllTabFragment.podcast){
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }else if(tabId== SearchAllTabFragment.playlist){
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }else if(tabId== SearchAllTabFragment.musicVideo|| tabId== SearchAllTabFragment.movie||tabId== SearchAllTabFragment.shortVideo || tabId== SearchAllTabFragment.shortFilm || tabId== SearchAllTabFragment.liveEvent || tabId== SearchAllTabFragment.tvshow){
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }else{
                if (!TextUtils.isEmpty(searchData.data?.subTitle)){
                    "${searchData.data?.subTitle}".also { holder.subTitle.text = it }
                }
                holder.subTitle.visibility = View.VISIBLE
            }


        }else{
            holder.subTitle.visibility = View.GONE
        }

        if(!searchData.data?.type?.isEmpty()!!){
            if (searchData.data?.type?.toInt() == 21 || searchData.data?.type?.toInt() == 110 || searchData.data?.type?.toInt() == 15 || searchData.data?.type?.toInt() == 33 || searchData.data?.type?.toInt() == 34
                || searchData.data?.type?.toInt() == 35 || searchData.data?.type?.toInt() == 36 || searchData.data?.type?.toInt() == 77777){
                holder.ivBack.visibility = View.INVISIBLE
            }else{
                holder.ivBack.visibility = View.VISIBLE
            }
        }

        if (searchData.data?.isCurrentPlaying == true){
            holder.ivEqualizerAnim.show()
            holder.ivEqualizerAnim.playAnimation()
        }else{
            holder.ivEqualizerAnim.hide()
            holder.ivEqualizerAnim.cancelAnimation()
            holder.ivEqualizerAnim.progress = 0f
        }


        holder.clMain.setOnClickListener {
            searchItem.searchItemClick(searchData, position)
        }
    }

    override fun getItemCount(): Int {
        return searchDataList.size
    }
    interface SearchResult{
        fun searchItemClick(searchData: BodyRowsItemsItem, position: Int)
    }
}