package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.*
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.InAppSelfHandledModel
import com.hungama.music.data.model.OrignalSeason
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ProgressiveSurveyViewEvent
import com.hungama.music.eventanalytic.util.callbacks.inapp.InAppCallback
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.TvShowDetailsFragment
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getDeviceHeight
import com.hungama.music.utils.CommonUtils.getDeviceWidth
import com.hungama.music.utils.CommonUtils.loadBannerAds
import com.hungama.music.utils.CommonUtils.setArtImageBg
import com.hungama.music.utils.CommonUtils.setArtImageBgGradient
import com.hungama.music.utils.CommonUtils.setArtImageDarkBg
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.ORIENTATION_VERTICAL
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.ShowMoreTextView
import com.hungama.music.utils.customview.blurview.CustomShapeBlurView
import com.hungama.music.utils.customview.circleimageview.CircleImageView
import com.hungama.music.utils.customview.emojiratingbar.EmojiRatingBar
import com.hungama.music.utils.customview.fontview.FontAwesomeImageView
import com.hungama.music.utils.customview.stories.CircularStatusView
import com.hungama.music.R
import com.moengage.inapp.MoEInAppHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BucketChildAdapter(
    val mContext: Context,
    var list: ArrayList<BodyRowsItemsItem?>,
    val varient: Int = ORIENTATION_VERTICAL,
    val onChildItemClick: OnChildItemClick?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val ROW_ITYPE_1 = 1
        const val ROW_ITYPE_2 = 2
        const val ROW_ITYPE_3 = 3
        const val ROW_ITYPE_4 = 4
        const val ROW_ITYPE_5 = 5
        const val ROW_ITYPE_6 = 6
        const val ROW_ITYPE_7 = 7
        const val ROW_ITYPE_8 = 8
        const val ROW_ITYPE_9 = 9
        const val ROW_ITYPE_10 = 10
        const val ROW_ITYPE_11 = 11
        const val ROW_ITYPE_12 = 12
        const val ROW_ITYPE_13 = 13
        const val ROW_ITYPE_14 = 14
        const val ROW_ITYPE_15 = 15
        const val ROW_ITYPE_16 = 16
        const val ROW_ITYPE_17 = 17
        const val ROW_ITYPE_18 = 18
        const val ROW_ITYPE_19 = 19
        const val ROW_ITYPE_20 = 20
        const val ROW_ITYPE_21 = 21
        const val ROW_ITYPE_22 = 22
        const val ROW_ITYPE_23 = 23
        const val ROW_ITYPE_25 = 25
        const val ROW_ITYPE_41 = 41
        const val ROW_ITYPE_42 = 42
        const val ROW_ITYPE_43 = 43
        const val ROW_ITYPE_44 = 44
        const val ROW_ITYPE_45 = 45
        const val ROW_ITYPE_46 = 46
        const val ROW_ITYPE_47 = 47
        const val ROW_ITYPE_48 = 48
        const val ROW_ITYPE_51 = 51
        const val ROW_ITYPE_99999 = 99999
        const val ROW_ITYPE_1000 = 1000
        const val ROW_ITYPE_101 = 101
        const val ROW_ITYPE_102 = 102
        const val ROW_ITYPE_103 = 103
        const val ROW_ITYPE_104 = 104
        const val ROW_ITYPE_201 = 201
        var simpleExoplayer: SimpleExoPlayer? = null
    }

    private val ctx: Context = mContext
//    var list =mainList

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
    var commonTopMargin = 0.0

    var firstTime = true

    internal fun addData(listItems: ArrayList<BodyRowsItemsItem?>) {
        var size = list.size
        list.addAll(listItems)
        var sizeNew = list.size
        notifyItemRangeChanged(size, sizeNew)
    }

    private inner class IType1ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val ivCircularStatusView: CircularStatusView =
            itemView.findViewById(R.id.circular_status_view)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (!TextUtils.isEmpty(list?.title)) {
                    tvTitle.text = list?.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list?.continueStatus!!) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                }

                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()

                val params2: ViewGroup.LayoutParams = ivCircularStatusView.layoutParams
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivCircularStatusView.requestLayout()

                if (!TextUtils.isEmpty(list.image)) {
                    val params3: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params3.height = imageHeightByAspectRatio.toInt() - ctx.resources.getDimensionPixelSize(R.dimen.dimen_8)
                    params3.width = itemWidth.toInt() - ctx.resources.getDimensionPixelSize(R.dimen.dimen_8)
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.color.colorPlaceholder
                    )
                }

                setLog(TAG, "bind: list?.image!!: IType1ViewHolder" + list.image)

                val notSeenColor = itemView.context.resources.getColor(R.color.story_not_seen)
                val seenColor: Int =
                    itemView.context.resources.getColor(R.color.half_opacity_white_color)
                val notSeenTextColor =
                    itemView.context.resources.getColor(R.color.colorBlack)
                val seenTextColor = itemView.context.resources.getColor(R.color.story_seen_text)

                list.misc?.post?.count?.let { ivCircularStatusView.setPortionsCount(it) }
                ivCircularStatusView.setPortionSpacing(10)
                if (!list.misc?.post?.items.isNullOrEmpty()) {
                    var i = 0
                    var allSeen = false
                    for (item in list.misc?.post?.items?.iterator()!!) {
                        setLog("TAG", "bind: item.watch:${item.watch}")
                        val color = if (item.watch) seenColor else notSeenColor
                        allSeen = item.watch
                        ivCircularStatusView.setPortionColorForIndex(i, color)
                        i++
                    }
                    val textColor = if (allSeen) seenTextColor else notSeenTextColor
                    tvTitle.setTextColor(textColor)
                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }


        }
    }

    private inner class IType2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }
                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                    setLog(TAG, "bind: list?.image!!:IType2ViewHolder  " + list.image)
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvConentTitle: TextView = itemView.findViewById(R.id.tvConentTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivUserImage1: ImageView = itemView.findViewById(R.id.ivUserImage1)
        val ivUserImage2: ImageView = itemView.findViewById(R.id.ivUserImage2)
        val ivUserImage3: ImageView = itemView.findViewById(R.id.ivUserImage3)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val clImages: RelativeLayout = itemView.findViewById(R.id.clImages)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list == null) {
                    return
                }

                if (list.title != null && !TextUtils.isEmpty(list.title)) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.INVISIBLE
                }

                if (list.subTitle != null && !TextUtils.isEmpty(list.subTitle)) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.INVISIBLE
                }
                setLog("TAG", "bind list :${list} \nmisc:${list.misc}")
                var text = ""
                if (list.misc != null && list.misc.lang != null) {
                    if (list.misc.lang is String) {
                        if (!TextUtils.isEmpty(list.misc.lang)) {
                            text = list.misc.lang
                        }

                    } else {
                        if (!(list.misc.lang as List<String?>).isNullOrEmpty()) {
                            text = TextUtils.join(",", list.misc.lang)
                        }
                    }
                }

                if (!TextUtils.isEmpty(text)) {
                    tvConentTitle.text = text
                    tvConentTitle.visibility = View.VISIBLE
                } else {
                    tvConentTitle.visibility = View.INVISIBLE
                }

                /*if (list.misc != null && !list.misc?.lang.isNullOrEmpty()) {
                    val cText=list.misc?.lang.toString().replace("[","",true)
                    val cText1=cText.replace("]","",true)
                    tvConentTitle.text = ""+cText1
                    tvConentTitle.visibility = View.VISIBLE
                } else {
                    tvConentTitle.visibility = View.INVISIBLE
                }*/

                val params6: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params6.height = itemHeight.toInt()
                params6.width = itemWidth.toInt()
                ivUserImage.requestLayout()

                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()

                val imageHeight = imageHeightByAspectRatio.toInt()
                val imageWidth =
                    itemWidth.toInt() - ctx.resources.getDimensionPixelSize(R.dimen.dimen_159) - ctx.resources.getDimensionPixelSize(
                        R.dimen.dimen_16
                    )

                val params5: ViewGroup.LayoutParams = clImages.layoutParams
                params5.height = imageHeight
                params5.width = imageWidth
                clImages.requestLayout()

                val params1: ViewGroup.LayoutParams = ivUserImage1.layoutParams
                params1.height = imageHeight
                params1.width = imageWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_32)
                ivUserImage1.requestLayout()

                val params2: ViewGroup.LayoutParams = ivUserImage2.layoutParams
                params2.height = imageHeight
                params2.width = imageWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_32)
                ivUserImage2.requestLayout()

                val params3: ViewGroup.LayoutParams = ivUserImage3.layoutParams
                params3.height = imageHeight
                params3.width = imageWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_32)
                ivUserImage3.requestLayout()
                if (!list.images.isNullOrEmpty()) {

                    if (list.images.size > 0) {
                        ImageLoader.loadImage(
                            ctx,
                            ivUserImage1,
                            list.images.get(0),
                            R.drawable.bg_gradient_placeholder
                        )
                    }
                    if (list.images.size > 1) {
                        ImageLoader.loadImage(
                            ctx,
                            ivUserImage2,
                            list.images.get(1),
                            R.drawable.bg_gradient_placeholder
                        )
                    }

                    if (list.images.size > 2) {

                        ImageLoader.loadImage(
                            ctx,
                            ivUserImage3,
                            list.images.get(2),
                            R.drawable.bg_gradient_placeholder
                        )
                    }


                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType4ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                tvSubTitle.visibility = View.GONE

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.color.colorPlaceholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType5ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvLanguageView: TextView = itemView.findViewById(R.id.tvLanguageView)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivPlay: AppCompatImageView = itemView.findViewById(R.id.ivPlay)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                var height = 0.0
                if (!TextUtils.isEmpty(list!!.title)) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    //height += ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
                    tvTitle.visibility = View.INVISIBLE
                }

                if (!TextUtils.isEmpty(list.subTitle)) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    //height += ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
                    tvSubTitle.visibility = View.INVISIBLE
                }

                if (!list.type.equals("51") && list.duration != null && !TextUtils.isEmpty(list.duration)) {
                    tvTime.text = DateUtils.formatElapsedTime(list.duration?.toLong()!!)
                    tvTime.visibility = View.VISIBLE
                } else {
                    //height += ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
                    tvTime.visibility = View.INVISIBLE
                }

                if (list.misc != null && !TextUtils.isEmpty(list.misc.f_playcount)) {
                    var text = ""
                    if (list.misc.lang != null) {
                        if (list.misc.lang is String) {
                            if (!TextUtils.isEmpty(list.misc.lang)) {
                                text = list.misc.lang
                            }

                        } else {
                            if (!(list.misc.lang as List<String?>).isNullOrEmpty()) {
                                text = TextUtils.join(",", list.misc.lang)
                            }
                        }

                    }
                    if (!list.type.equals("51")) {
                        if (!TextUtils.isEmpty(text)) {
                            //text = text+" - "+ ratingWithSuffix(list.misc.playcount) + ctx.getString(R.string.discover_str_25)
                            text =
                                text + " • " + list.misc.f_playcount + " " + ctx.getString(R.string.discover_str_25)
                        } else {
                            /*text = ratingWithSuffix(list.misc.playcount) + ctx.getString(R.string.discover_str_25)*/
                            text =
                                list.misc.f_playcount + " " + ctx.getString(R.string.discover_str_25)
                        }
                    }

                    tvLanguageView.text = text
                    tvLanguageView.visibility = View.GONE
                } else {
                    tvLanguageView.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                    tvLanguageView.visibility = View.GONE
                    ivPlay.visibility = View.GONE
                    tvTime.visibility = View.GONE
                }
                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            } else {
                itemView.hide()
            }
        }
    }

    private inner class IType6ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: TextView = itemView.findViewById(R.id.txtRent)
        val ivSubscription: ImageView = itemView.findViewById(R.id.ivSubscription)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rating: TextView = itemView.findViewById(R.id.txtRating)
        val rlRating: RelativeLayout = itemView.findViewById(R.id.rlRating)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list[position]?.data!!

                if (list.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )

                    setLog(
                        "TAG",
                        "IType6ViewHolder bind: ivUserImage:${list.image} position:${position}"
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                if (list.misc?.rating_critic.equals("0") || list.misc?.rating_critic.equals(
                        "NA", true
                    )
                ) {
                    rlRating.hide()
                } else {
                    if (list.misc != null && !TextUtils.isEmpty(list.misc.rating_critic)) {
                        val ratingString = list.misc.rating_critic
                        val ratingDouble = ratingString?.toFloat()
                        setLog("TAG", "bind: " + ratingDouble)
                        rating.text = ratingDouble.toString()
                    } else {
                        rlRating.hide()
                    }
                }

                if (list.misc != null && list.misc.movierights != null && list.misc.movierights?.size!! > 0) {
                    Utils.setMovieRightTextForBucket(
                        ivSubscription, list.misc.movierights!!, ctx, list.id.toString(), false
                    )
                } else {
                    txtRent.visibility = View.GONE
                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType7ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rlFreeStrip: RelativeLayout = itemView.findViewById(R.id.rlFreeStrip)
        val ivEqualizerAnim: LottieAnimationView = itemView.findViewById(R.id.ivEqualizerAnim)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val dataItem = list.get(position)?.data

                setLog("TAG", "bind ->list size:" + list.size)
                setLog("TAG", "bind position:${position} title:" + dataItem?.title)
                tvTitle.text = dataItem?.title
                tvTitle.invalidate()

                val subTitle = Utils.getContentTypeName(dataItem?.type!!)
                setLog("TAG", "bind position:${position} subTitle:" + subTitle)
                if (!TextUtils.isEmpty(subTitle)) {
                    tvSubTitle.text = subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }


                val params: ViewGroup.LayoutParams = llMain.layoutParams
                //params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                /*val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()*/
                ImageLoader.loadImage(
                    ctx, ivUserImage, dataItem.image, R.drawable.bg_gradient_placeholder
                )
                setLog("TAG", "bind position:${position}  dataItem?.image!!:" + dataItem.image)

                if (dataItem.isCurrentPlaying == true) {
                    ivEqualizerAnim.show()
                    ivEqualizerAnim.playAnimation()
                } else {
                    ivEqualizerAnim.hide()
                    ivEqualizerAnim.cancelAnimation()
                    ivEqualizerAnim.progress = 0f
                }

                if(!CommonUtils.isUserHasGoldSubscription() && dataItem.misc?.movierights?.contains("AMOD") == true && CommonUtils.getSongDurationConfig().enable_minutes_quota)
                    rlFreeStrip.show() else  rlFreeStrip.hide()

                //ivUserImage?.invalidate()
                llMain.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, dataItem.misc?.explicit!!)) {
                        onChildItemClick?.onUserClick(position, llMain)
                    }
                }

                CommonUtils.setExplicitContent(ctx, llMain, dataItem.misc?.explicit!!)

            } else {
                setLog(
                    "TAG", "bind IType7ViewHolder is position:$position list?.size:${list.size}"
                )
            }

        }
    }

    private inner class IType8ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivEqualizerAnim: LottieAnimationView = itemView.findViewById(R.id.ivEqualizerAnim)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }
                val params: ViewGroup.LayoutParams = llMain.layoutParams
                //params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                /*val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()*/
                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                    setLog(TAG, "bind: list.image  IType8ViewHolder " + list.image)
                }

                if (list.isCurrentPlaying == true) {
                    ivEqualizerAnim.show()
                    ivEqualizerAnim.playAnimation()
                } else {
                    ivEqualizerAnim.hide()
                    ivEqualizerAnim.cancelAnimation()
                    ivEqualizerAnim.progress = 0f
                }
                llMain.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc?.explicit!!)) {
                        onChildItemClick?.onUserClick(position, llMain)
                    }
                }
                if (list.misc != null && list.misc.explicit != null) {
                    CommonUtils.setExplicitContent(ctx, llMain, list.misc.explicit)
                }
            }

        }
    }

    private inner class IType9ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: RelativeLayout = itemView.findViewById(R.id.llMain)
        val rootLayout: ImageView = itemView.findViewById(R.id.rootParent)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }
                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                if (list.image != null) {
                    setArtImageBgGradient(true, list.image, rootLayout)
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()

                    val params3: ViewGroup.LayoutParams = rootLayout.layoutParams
                    params3.height = itemHeight.toInt()
                    params3.width = itemWidth.toInt()
                    rootLayout.requestLayout()

                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height =
                        (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_16) - ctx.resources.getDimensionPixelSize(
                            R.dimen.dimen_52
                        )).toInt()
                    params2.width =
                        (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_64)).toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType10ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                //setLog("IType10ViewHolder", "title - ${list?.title}")
                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                }

                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = imageHeightByAspectRatio.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType11ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvLang: TextView = itemView.findViewById(R.id.tvLang)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivParent: ImageView = itemView.findViewById(R.id.ivParent)

        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                } else {
                    tvTitle.text = ""
                }

                /*if (!TextUtils.isEmpty(list?.misc?.f_playcount)) {
                    *//*tvSubTitle.text = CommonUtils.ratingWithSuffix(list.misc?.playcount)+ctx.getString(
                        R.string.discover_str_23)*//*
                    tvSubTitle.text = list.misc?.f_playcount + " " + ctx.getString(
                        R.string.discover_str_23
                    )
                } else {
                    tvSubTitle.text = ""
                }*/
                //Removed by Shreya - 11-07-2022
                tvSubTitle.hide()

                if (list.misc?.lang != null) {
                    if (list.misc.lang is String) {
                        if (!TextUtils.isEmpty(list.misc.lang)) {
                            tvLang.text = list.misc.lang
                        } else {
                            tvLang.text = ""
                        }

                    } else {
                        if (!(list.misc.lang as List<String?>).isNullOrEmpty()) {
                            tvLang.text = TextUtils.join(",", list.misc.lang)
                        } else {
                            tvLang.text = ""
                        }
                    }

                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvSubTitle.visibility = View.GONE
                    tvTitle.visibility = View.GONE
                    tvLang.visibility = View.GONE
                }

                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height =
                        itemHeight.toInt() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_11)
                            .toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height =
                        (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_11)).toInt()
                    params2.width =
                        (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_11)).toInt()
//                params2.width = (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_16)- ctx.resources.getDimensionPixelSize(R.dimen.dimen_18)).toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.color.colorPlaceholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }
        }
    }

    private inner class IType12ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: TextView = itemView.findViewById(R.id.tvSubTitle2)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }
                if (list.misc != null && !TextUtils.isEmpty(list.misc.f_playcount)) {
                    tvSubTitle2.text =
                        list.misc.f_playcount + " " + ctx.getString(R.string.discover_str_24)
                    tvSubTitle2.visibility = View.GONE
                } else {
                    tvSubTitle2.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                    tvSubTitle2.visibility = View.GONE
                }

                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType13ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            setLog(
                TAG, "onBindViewHolder: IType13ViewHolder position :${position} size:${list.size}"
            )
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }
                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType14ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val pbSong: ProgressBar = itemView.findViewById(R.id.pbSong)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val imageMain: RelativeLayout = itemView.findViewById(R.id.imageMain)
        val blurView: CustomShapeBlurView = itemView.findViewById(R.id.blurView)
        val cardMain: CardView = itemView.findViewById(R.id.cardMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                    setLog("TAG", "bind: list.subTitle " + list.subTitle)
                    setLog("TAG", "bind: tvTitle " + list.title)
                    setLog("TAG", "bind: " + tvTitlePlaceHolder)
                } else {
                    tvSubTitle.visibility = View.GONE
                }
                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }
                if (list.image != null) {

                    val params5: ViewGroup.LayoutParams = cardMain.layoutParams
                    params5.height = itemHeight.toInt()
                    params5.width = itemWidth.toInt()
                    cardMain.requestLayout()

                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()

                    val params4: ViewGroup.LayoutParams = blurView.layoutParams
                    params4.height = itemHeight.toInt()
                    params4.width = itemWidth.toInt()
                    blurView.requestLayout()

                    val params3: ViewGroup.LayoutParams = imageMain.layoutParams
                    params3.height = imageHeightByAspectRatio.toInt()
                    params3.width = itemWidth.toInt()
                    imageMain.requestLayout()

                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()

                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                try {
                    pbSong.max = list.duration?.toInt()!!
                    pbSong.progress =
                        HungamaMusicApp.getInstance().getContentDuration(list.id!!)?.toInt()!!
                } catch (e: Exception) {

                }



                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType15ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)

        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                if (list?.image != null) {

                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType16ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val ivUshape: ImageView = itemView.findViewById(R.id.ivUshape)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootParent)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (!TextUtils.isEmpty(list.misc?.f_FavCount)) {
                    tvSubTitle.text = list.misc?.f_FavCount
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.text = "0"
                    tvSubTitle.visibility = View.VISIBLE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }
                if (list.image != null) {
                    setArtImageDarkBg(true, list.image, ivUshape)
                    setArtImageBg(true, list.image, rootLayout)
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params3: ViewGroup.LayoutParams = rootLayout.layoutParams
                    params3.height = (itemHeight - ctx.resources.getDimensionPixelSize(R.dimen.dimen_10)).toInt()
                    params3.width = itemWidth.toInt()
                    rootLayout.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    setLog("fshgsalgba", " $imageHeightByAspectRatio $itemWidth")
                    params2.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_11) - ctx.resources.getDimensionPixelSize(R.dimen.dimen_37)).toInt()
                    params2.width = (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_45)).toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(ctx, ivUserImage, list.image, R.color.colorPlaceholder)

                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType18ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: ConstraintLayout = itemView.findViewById(R.id.main)

        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                if (list?.image != null) {
                    ImageLoader.loadImageWithFullScreen(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType19ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvHeaderTitle: TextView = itemView.findViewById(R.id.tvHeaderTitle)
        var tvHeaderSubTitle: TextView = itemView.findViewById(R.id.tvHeaderSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivHeader: CircleImageView = itemView.findViewById(R.id.ivHeader)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list?.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }
                if (list?.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list?.misc != null) {
                    if (list.misc.headerTitle != null) {
                        tvHeaderTitle.text = list.misc.headerTitle
                        tvHeaderTitle.visibility = View.VISIBLE
                    }

                    if (list.misc.headerSubTitle != null) {
                        tvHeaderSubTitle.text = list.misc.headerSubTitle
                        tvHeaderSubTitle.visibility = View.VISIBLE
                    }

                    if (list.misc.headerImage != null) {
                        ImageLoader.loadImage(
                            ctx, ivHeader, list.misc.headerImage, R.color.colorPlaceholder
                        )
                    }

                }



                if (list?.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType20ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params2.height = itemHeight.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                if (list?.image != null) {

                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                ivUserImage.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType21ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: AppCompatTextView = itemView.findViewById(R.id.txtRent)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootParent)
        val ivRent: ImageView = itemView.findViewById(R.id.ivRent)
        val llRent: LinearLayoutCompat = itemView.findViewById(R.id.llRent)
        val rating: TextView = itemView.findViewById(R.id.txtRating)
        val rlRating: RelativeLayout = itemView.findViewById(R.id.rlRating)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                applyButtonTheme(ctx, llRent)
                val tempPos = list?.sequence!! + 1
                if (tempPos > 9) {
                    tvNumber.text = "" + tempPos
                } else {
                    tvNumber.text = "0" + tempPos

                }

                /*val paint: TextPaint = tvNumber.getPaint()
                val width = paint.measureText(tvNumber?.text?.toString())
                val textShader: Shader = LinearGradient(
                    0f, 0f, width, tvNumber.getTextSize(), intArrayOf(
                        Color.parseColor("#80FFFFFF"),
                        Color.parseColor("#26FFFFFF")
                    ), null, Shader.TileMode.CLAMP
                )
                tvNumber?.getPaint().setShader(textShader)
                tvNumber?.invalidate()*/

                /*TextPaint paint = tvNumber.getPaint();
            float width = paint.measureText(tvNumber.getText().toString());
            float angleInRadians = (float) Math.toRadians(90);
            float length = width;

            float endX = (float) (Math.cos(angleInRadians) * length);
            float endY = (float) (Math.sin(angleInRadians) * length);


            Shader textShader = new LinearGradient(0f, 0f,endX,endY,
                    new int[]{
                            Color.parseColor("#ffffff"),
                            Color.parseColor("#00ffffff")
                    }, null, Shader.TileMode.CLAMP);
            tvNumber.getPaint().setShader(textShader);*/
                val c1 = ContextCompat.getColor(
                    ctx, R.color.colorWhite
                )
                val c2 = ContextCompat.getColor(
                    ctx, R.color.transparent
                )
                val shader: Shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    tvNumber.textSize,
                    intArrayOf(c1, Color.TRANSPARENT),
                    floatArrayOf(0f, 0.9f),
                    Shader.TileMode.CLAMP
                )
                tvNumber.paint.shader = shader
                if (list.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                tvTitle.invalidate()

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list.misc != null && list.misc.movierights != null && list.misc.movierights?.size!! > 0) {
                    setLog("TAG", "bind: movie right:${list.misc.movierights}")
                    Utils.setMovieRightTextForBucketWithPlay(txtRent, ivRent, list.misc.movierights!!, ctx, list.id.toString())
                } else {
                    txtRent.visibility = View.GONE
                }

                if (list.misc?.rating_critic.toString().equals("0", true)) {
                    rlRating.hide()
                } else {
                    try {
                        var ratingString = list.misc?.rating_critic
                        var ratingDouble = ratingString?.toFloat()
                        setLog("TAG", "bind: " + ratingDouble)
                        rating.text = ratingDouble.toString()
                        rlRating.show()
                    } catch (e: Exception) {
                        rlRating.hide()
                    }

                }

                if (list.image != null) {
                    setArtImageBg(true, list.image, rootLayout)
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    rootLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            ctx, R.color.home_bg_color
                        )
                    )
                }

                ivUserImage.invalidate()
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }

    }

    private inner class IType22ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (!TextUtils.isEmpty(list?.date)) {
                    tvDay.text = com.hungama.music.utils.DateUtils.convertDate(
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_DD,
                        list?.date
                    )
                    tvMonth.text = com.hungama.music.utils.DateUtils.convertDate(
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_MMM,
                        list?.date
                    )
                }

                if (list?.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType23ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubTitle: AppCompatTextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: AppCompatTextView = itemView.findViewById(R.id.txtRent)
        val tvSubTitle2: AppCompatTextView = itemView.findViewById(R.id.tvSubTitle2)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                //applyButtonTheme(ctx, txtRent)
                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {

                    var subTitle = list.subTitle


                    var text = ""
                    if (list.misc != null && list.misc.lang != null) {
                        if (list.misc.lang is String) {
                            if (!TextUtils.isEmpty(list.misc.lang)) {
                                text = list.misc.lang
                            }

                        } else {
                            if (!(list.misc.lang as List<String?>).isNullOrEmpty()) {
                                text = TextUtils.join(",", list.misc.lang)
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(text)) {
                        subTitle = "$subTitle - $text"
                    }
                    tvSubTitle.text = subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list.misc != null && list.misc.movierights != null && list.misc.movierights?.size!! > 0) {
                    Utils.setMovieRightTextForDetail(
                        txtRent, list.misc.movierights!!, ctx, list.id.toString()
                    )
                } else {
                    txtRent.visibility = View.GONE
                }
                if (list.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType41ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivLeftImage: ImageView = itemView.findViewById(R.id.ivLeftImage)
        val ivRightImage: ImageView = itemView.findViewById(R.id.ivRightImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootParent)
        var tvName1: TextView = itemView.findViewById(R.id.tvName1)
        var tvName2: TextView = itemView.findViewById(R.id.tvName2)
        var cardMain: CardView = itemView.findViewById(R.id.cardMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                if (list?.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list!!.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list.title != null) {
                    val str = list.title.toString().trim()
                    try {
                        val splited: List<String> = str.split(" ")
                        if (!splited.isNullOrEmpty()) {
                            if (!TextUtils.isEmpty(splited[splited.size - 1])) {
                                tvName1.text = splited[splited.size - 1].trim().toString()
                                tvName1.visibility = View.VISIBLE
                            } else {
                                tvName1.visibility = View.INVISIBLE
                            }
                            tvName2.visibility = View.INVISIBLE
                        } else {
                            tvName1.visibility = View.INVISIBLE
                            tvName2.visibility = View.INVISIBLE
                        }

                        /*for (i in 0 until splited.size) {
                            if (i == 0) {
                                if (!TextUtils.isEmpty(splited[0])){
                                    tvName1.text = splited.get(0).trim().toString()
                                    tvName1.visibility = View.VISIBLE
                                }else{
                                    tvName1.visibility = View.INVISIBLE
                                }
                            } else if (i == 1) {
                                if (!TextUtils.isEmpty(splited[1])){
                                    tvName2.text = splited.get(1).trim().toString()
                                    tvName2.visibility = View.VISIBLE
                                } else {
                                    tvName2.visibility = View.INVISIBLE
                                }
                            }
                        }*/
                    } catch (e: Exception) {
                        tvName1.visibility = View.INVISIBLE
                        tvName2.visibility = View.INVISIBLE
                    }
                } else {
                    tvName1.visibility = View.INVISIBLE
                    tvName2.visibility = View.INVISIBLE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()

                val params4: ViewGroup.LayoutParams = cardMain.layoutParams
                params4.height = imageHeightByAspectRatio.toInt()
                params4.width = itemWidth.toInt()
                cardMain.requestLayout()

                val params6: ViewGroup.LayoutParams = rootLayout.layoutParams
                params6.height = imageHeightByAspectRatio.toInt()
                params6.width = itemWidth.toInt()
                rootLayout.requestLayout()

                val params2: ViewGroup.LayoutParams = ivLeftImage.layoutParams
                params2.height =
                    (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(
                        R.dimen.font_26
                    )).toInt()
                params2.width =
                    (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(
                        R.dimen.font_26
                    )).toInt()
                ivLeftImage.requestLayout()
                if (list.images != null && list.images.size > 0) {

                    ImageLoader.loadImage(
                        ctx, ivLeftImage, list.images.get(0), R.drawable.bg_gradient_placeholder
                    )
                } else {
                    ImageLoader.loadImage(
                        ctx, ivLeftImage, "", R.drawable.bg_gradient_placeholder
                    )
                }

                val params3: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params3.height =
                    (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_50) - ctx.resources.getDimensionPixelSize(
                        R.dimen.font_26
                    )).toInt()
                params3.width =
                    (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_50) - ctx.resources.getDimensionPixelSize(
                        R.dimen.font_26
                    )).toInt()
                ivUserImage.requestLayout()
                if (list.images != null && list.images.size > 1) {
                    setArtImageBg(true, list.images.get(1), rootLayout)

                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.images.get(1), R.drawable.bg_gradient_placeholder
                    )
                } else {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, "", R.drawable.bg_gradient_placeholder
                    )
                }

                val params5: ViewGroup.LayoutParams = ivRightImage.layoutParams
                params5.height =
                    (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(
                        R.dimen.font_26
                    )).toInt()
                params5.width =
                    (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(
                        R.dimen.font_26
                    )).toInt()
                ivRightImage.requestLayout()
                if (list.images != null && list.images.size > 2) {

                    ImageLoader.loadImage(
                        ctx,
                        ivRightImage,
                        list.images.get(2),
                        R.drawable.bg_gradient_placeholder
                    )
                } else {
                    ImageLoader.loadImage(
                        ctx, ivRightImage, "", R.drawable.bg_gradient_placeholder
                    )
                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType42ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        //        val ivUserImage2: ImageView = itemView.findViewById(R.id.ivUserImage2)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list?.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list?.misc != null && !TextUtils.isEmpty(list.misc.f_playcount)) {
                    tvSubTitle.text =
                        list.misc.f_playcount + " " + ctx.getString(R.string.discover_str_24)
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list?.continueStatus!!) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                if (list.images != null && list.images.size > 0) {
                    val turl = list.images.get((0..list.images.size - 1).random())
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, turl, R.drawable.bg_gradient_placeholder
                    )
                } else if(list.image != null){
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )

                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType43ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle2: TextView = itemView.findViewById(R.id.tvTitle2)
        var tvSubTitle2: TextView = itemView.findViewById(R.id.tvSubTitle2)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootParent)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list?.title != null) {
                    tvTitle2.text = list.title
                    tvTitle2.visibility = View.VISIBLE
                } else {
                    tvTitle2.visibility = View.GONE
                }

                if (list!!.subTitle != null) {
                    tvSubTitle2.text = list.subTitle
                    tvSubTitle2.visibility = View.VISIBLE
                } else {
                    tvSubTitle2.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle2.visibility = View.GONE
                    tvSubTitle2.visibility = View.GONE
                }

                if (list.image != null) {
                    setArtImageBg(true, list.image, rootLayout)
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height =
                        (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_40)).toInt()
                    params2.width =
                        (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_40)).toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType44ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.handleName != null) {
                    val makersName =
                        itemView.context.getString(R.string.playlist_str_2) + " " + list.handleName
                    tvSubTitle.text = "" + makersName
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (list.continueStatus) {
                    itemHeight = itemHeight - textSize
                    tvTitle.visibility = View.GONE
                    tvSubTitle.visibility = View.GONE
                }

                val params: ViewGroup.LayoutParams = llMain.layoutParams
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType45ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                if (list?.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list?.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }


        }
    }

    private inner class IType46ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                if (list?.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list?.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }
        }
    }

    private inner class IType48ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data
                var height = 0.0
                if (!TextUtils.isEmpty(list!!.title)) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.INVISIBLE
                }

                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.ic_game_placeholder
                    )
                }

                if (list.attributeGameRating?.isEmpty() == false) {
                    tvRating.text = list.attributeGameRating?.get(0)
                } else tvRating.text = "0.0"

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }
        }
    }

    private inner class IType25ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCast: TextView = itemView.findViewById(R.id.tvCast)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        /*var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: ShowMoreTextView = itemView.findViewById(R.id.tvSubTitle2)*/

        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val Main: LinearLayoutCompat = itemView.findViewById(R.id.Main)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val data = list.get(position)?.data

                if (data?.title != null) {
                    tvTitle.text = data.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (data?.misc?.cast != null) {
                    tvCast.text = data.misc.cast
                    tvCast.visibility = View.VISIBLE
                } else {
                    tvCast.visibility = View.GONE
                }

                if (data?.image != null && !TextUtils.isEmpty(data.image)) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, data.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    private inner class IType1000ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: TextView = itemView.findViewById(R.id.tvSubTitle2)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val list = list.get(position)?.data

                if (list!!.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (list.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }
                if (list.subTitle != null) {
                    tvSubTitle2.text = list.subTitle
                    CommonUtils.makeTextViewResizable(tvSubTitle2, 2, "read more", true)
                    tvSubTitle2.visibility = View.VISIBLE
                } else {
                    tvSubTitle2.visibility = View.GONE
                }
                /*if (list.time != null) {
                    tvTime.text = list.subTitle
                    tvTime.visibility = View.VISIBLE
                } else {
                    tvTime.visibility = View.GONE
                }*/
                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx, ivUserImage, list.image, R.drawable.bg_gradient_placeholder
                    )
                }
                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position)
                    }
                }
            }

        }
    }

    private inner class IType101ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var btnYes: Button = itemView.findViewById(R.id.btnYes)
        var btnNo: Button = itemView.findViewById(R.id.btnNo)
        var ivClose: ImageView = itemView.findViewById(R.id.ivClose)
        var mInAppSelfHandledModel: InAppSelfHandledModel? = null

        var isEventCall = false

        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val model = list.get(position)?.data

                setLog("setMoengageData", "bind: model:${model}")
                tvTitle.text = model?.title
                if (model?.options != null && model.options.size > 0) {


                    InAppCallback.mInAppCampaignList.values.forEachIndexed { index, inAppSelfHandledModel ->
                        if (model.id?.contains(inAppSelfHandledModel.campaignId, true)!!) {
                            mInAppSelfHandledModel = inAppSelfHandledModel
                        }
                    }

                    if (mInAppSelfHandledModel != null) {
                        val inAppSelfHandledModel = Gson().fromJson<InAppSelfHandledModel>(
                            mInAppSelfHandledModel?.inAppCampaign?.selfHandledCampaign?.payload,
                            InAppSelfHandledModel::class.java
                        ) as InAppSelfHandledModel


                        if (inAppSelfHandledModel != null) {
                            btnYes.text = "" + inAppSelfHandledModel.options.get(0)
                            btnNo.text = "" + inAppSelfHandledModel.options.get(1)
                        }

                    }

                    btnYes.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {
                            //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked

                            mInAppSelfHandledModel?.userAnswer = btnYes.text.toString()
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {

                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }

                    btnNo.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {

                            //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked
                            mInAppSelfHandledModel?.userAnswer = btnNo.text.toString()
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {
                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }

                    ivClose.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {

                            //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {
                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }

                    if (!isEventCall && mInAppSelfHandledModel != null) {
                        isEventCall = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val dataMap = HashMap<String, String>()
                            dataMap.put(
                                EventConstant.CAMPAIGN_ID_EPROPERTY,
                                mInAppSelfHandledModel?.campaign_id!!
                            )
                            dataMap.put(
                                EventConstant.TEMPLATE_ID_EPROPERTY,
                                mInAppSelfHandledModel?.templateId!!
                            )
                            dataMap.put(
                                EventConstant.TITLE_EPROPERTY, mInAppSelfHandledModel?.title!!
                            )
                            dataMap.put(
                                EventConstant.SUBTITLE_EPROPERTY, mInAppSelfHandledModel?.subTitle!!
                            )
                            dataMap.put(
                                EventConstant.OPTION_EPROPERTY, "" + mInAppSelfHandledModel?.options
                            )
                            dataMap.put(
                                EventConstant.BOTTOM_NAV_POSITION_EPROPERTY,
                                "" + mInAppSelfHandledModel?.bottom_nav_position!!
                            )
                            dataMap.put(
                                EventConstant.TOP_NAV_POSITION_EPROPERTY,
                                "" + mInAppSelfHandledModel?.top_nav_position!!
                            )
                            dataMap.put(
                                EventConstant.POSITION_EPROPERTY,
                                "" + mInAppSelfHandledModel?.position!!
                            )
                            EventManager.getInstance()
                                .sendEvent(ProgressiveSurveyViewEvent(dataMap))
                        }

                        // call whenever in-app is shown
                        MoEInAppHelper.getInstance()
                            .selfHandledShown(ctx, mInAppSelfHandledModel?.inAppCampaign!!)
                    }
                }
            }

        }
    }

    private inner class IType102ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tv1: TextView = itemView.findViewById(R.id.tv1)
        var tv2: TextView = itemView.findViewById(R.id.tv2)
        var tv3: TextView = itemView.findViewById(R.id.tv3)
        var tv4: TextView = itemView.findViewById(R.id.tv4)
        var ivClose: ImageView = itemView.findViewById(R.id.ivClose)
        var mInAppSelfHandledModel: InAppSelfHandledModel? = null
        var isEventCall = false

        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val model = list.get(position)?.data

                setLog("setMoengageData", "bind: model:${model}")

                InAppCallback.mInAppCampaignList.values.forEachIndexed { index, inAppSelfHandledModel ->
                    if (model?.id?.contains(inAppSelfHandledModel.campaignId, true)!!) {
                        mInAppSelfHandledModel = inAppSelfHandledModel
                    }
                }

                tvTitle.text = model?.title
                if (model?.options?.size!! > 0) {
                    tv1.text = "" + model.options.get(0)
                    tv1.visibility = View.VISIBLE

                    tv1.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {
//                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked
                            if (mInAppSelfHandledModel != null) {
                                mInAppSelfHandledModel?.userAnswer = tv1.text.toString()
                                MoEInAppHelper.getInstance().selfHandledClicked(
                                    ctx, mInAppSelfHandledModel?.inAppCampaign!!
                                )
                            }

                            if (onChildItemClick != null) {

                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }
                } else {
                    tv1.visibility = View.GONE
                }

                if (model.options.size > 1) {
                    tv2.text = "" + model.options.get(1)
                    tv2.visibility = View.VISIBLE

                    tv2.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {
                            //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked

                            mInAppSelfHandledModel?.userAnswer = tv2.text.toString()
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {

                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }
                } else {
                    tv2.visibility = View.GONE
                }

                if (model.options.size > 2) {
                    tv3.text = "" + model.options.get(2)
                    tv3.visibility = View.VISIBLE

                    tv3.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {
                            //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked

                            mInAppSelfHandledModel?.userAnswer = tv3.text.toString()
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {

                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }
                } else {
                    tv3.visibility = View.GONE
                }

                if (model.options.size > 3) {
                    tv4.text = "" + model.options.get(3)
                    tv4.visibility = View.VISIBLE

                    tv4.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {
                            //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                            // call whenever in-app is clicked

                            mInAppSelfHandledModel?.userAnswer = tv4.text.toString()
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {

                                onChildItemClick.onInAppSubmitClick(
                                    mInAppSelfHandledModel, position
                                )
                            }
                        }

                    }
                } else {
                    tv4.visibility = View.GONE
                }

                ivClose.setOnClickListener {
                    if (InAppCallback.mInAppCampaignList.values.size > 0) {

                        //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                        if (mInAppSelfHandledModel != null) {
                            // call whenever in-app is clicked
                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)
                        }


                        if (onChildItemClick != null) {
                            onChildItemClick.onInAppSubmitClick(mInAppSelfHandledModel, position)
                        }
                    }

                }


                if (!isEventCall && mInAppSelfHandledModel != null) {
                    isEventCall = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val dataMap = HashMap<String, String>()
                        dataMap.put(
                            EventConstant.CAMPAIGN_ID_EPROPERTY,
                            mInAppSelfHandledModel?.campaign_id!!
                        )
                        dataMap.put(
                            EventConstant.TEMPLATE_ID_EPROPERTY,
                            mInAppSelfHandledModel?.templateId!!
                        )
                        dataMap.put(EventConstant.TITLE_EPROPERTY, mInAppSelfHandledModel?.title!!)
                        dataMap.put(
                            EventConstant.SUBTITLE_EPROPERTY, mInAppSelfHandledModel?.subTitle!!
                        )
                        dataMap.put(
                            EventConstant.OPTION_EPROPERTY, "" + mInAppSelfHandledModel?.options
                        )
                        dataMap.put(
                            EventConstant.BOTTOM_NAV_POSITION_EPROPERTY,
                            "" + mInAppSelfHandledModel?.bottom_nav_position!!
                        )
                        dataMap.put(
                            EventConstant.TOP_NAV_POSITION_EPROPERTY,
                            "" + mInAppSelfHandledModel?.top_nav_position!!
                        )
                        dataMap.put(
                            EventConstant.POSITION_EPROPERTY,
                            "" + mInAppSelfHandledModel?.position!!
                        )
                        EventManager.getInstance().sendEvent(ProgressiveSurveyViewEvent(dataMap))
                    }


                    // call whenever in-app is shown
                    MoEInAppHelper.getInstance()
                        .selfHandledShown(ctx, mInAppSelfHandledModel?.inAppCampaign!!)
                }
            }

        }
    }

    private inner class IType103ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var emojiRatingBar: EmojiRatingBar = itemView.findViewById(R.id.emoji_rating_bar)
        var ivClose: ImageView = itemView.findViewById(R.id.ivClose)
        var btnSumit: Button = itemView.findViewById(R.id.btnSumit)

        var mInAppSelfHandledModel: InAppSelfHandledModel? = null
        var isEventCall = false
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val model = list.get(position)?.data

                setLog("setMoengageData", "bind: model:${model}")

                InAppCallback.mInAppCampaignList.values.forEachIndexed { index, inAppSelfHandledModel ->
                    if (model?.id?.contains(inAppSelfHandledModel.campaignId, true)!!) {
                        mInAppSelfHandledModel = inAppSelfHandledModel
                    }
                }

                tvTitle.text = model?.title
                tvSubTitle.text = model?.subTitle


                btnSumit.setOnClickListener {
                    if (InAppCallback.mInAppCampaignList.values.size > 0) {

                        //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                        // call whenever in-app is clicked
                        mInAppSelfHandledModel?.userAnswer =
                            emojiRatingBar.getCurrentRateStatus().name
                        MoEInAppHelper.getInstance()
                            .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                        if (onChildItemClick != null) {
                            onChildItemClick.onInAppSubmitClick(mInAppSelfHandledModel, position)
                        }
                    }

                }

                ivClose.setOnClickListener {
                    if (InAppCallback.mInAppCampaignList.values.size > 0) {

                        //                    SharedPrefHelper.getInstance().save(InAppCallback.mInAppCampaign?.campaignId!!,InAppCallback.mInAppCampaign?.campaignId!!)
                        // call whenever in-app is clicked
                        MoEInAppHelper.getInstance()
                            .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                        if (onChildItemClick != null) {
                            onChildItemClick.onInAppSubmitClick(mInAppSelfHandledModel, position)
                        }
                    }

                }

                if (!isEventCall && mInAppSelfHandledModel != null) {
                    isEventCall = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val dataMap = HashMap<String, String>()
                        dataMap.put(
                            EventConstant.CAMPAIGN_ID_EPROPERTY,
                            mInAppSelfHandledModel?.campaign_id!!
                        )
                        dataMap.put(
                            EventConstant.TEMPLATE_ID_EPROPERTY,
                            mInAppSelfHandledModel?.templateId!!
                        )
                        dataMap.put(EventConstant.TITLE_EPROPERTY, mInAppSelfHandledModel?.title!!)
                        dataMap.put(
                            EventConstant.SUBTITLE_EPROPERTY, mInAppSelfHandledModel?.subTitle!!
                        )
                        dataMap.put(
                            EventConstant.OPTION_EPROPERTY, "" + mInAppSelfHandledModel?.options
                        )
                        dataMap.put(
                            EventConstant.BOTTOM_NAV_POSITION_EPROPERTY,
                            "" + mInAppSelfHandledModel?.bottom_nav_position!!
                        )
                        dataMap.put(
                            EventConstant.TOP_NAV_POSITION_EPROPERTY,
                            "" + mInAppSelfHandledModel?.top_nav_position!!
                        )
                        dataMap.put(
                            EventConstant.POSITION_EPROPERTY,
                            "" + mInAppSelfHandledModel?.position!!
                        )
                        EventManager.getInstance().sendEvent(ProgressiveSurveyViewEvent(dataMap))
                    }


                    // call whenever in-app is shown
                    MoEInAppHelper.getInstance()
                        .selfHandledShown(ctx, mInAppSelfHandledModel?.inAppCampaign!!)
                }
            }


        }
    }

    private inner class IType104ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: ShapeableImageView = itemView.findViewById(R.id.ivImage)
        var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)

        var mInAppSelfHandledModel: InAppSelfHandledModel? = null
        var isEventCall = false
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                val model = list.get(position)?.data

                InAppCallback.mInAppCampaignList.values.forEachIndexed { index, inAppSelfHandledModel ->
                    if (model?.id?.contains(inAppSelfHandledModel.campaignId, true)!!) {
                        mInAppSelfHandledModel = inAppSelfHandledModel
                    }
                }
                setLog(
                    "IType104ViewHolder",
                    "IType104ViewHolder mInAppSelfHandledModel:${mInAppSelfHandledModel?.toString()}"
                )

                mInAppSelfHandledModel?.let {
                    try {
                        val resolution = mInAppSelfHandledModel?.resolution
                        val delim = "x"
                        if (resolution != null && !TextUtils.isEmpty(resolution) && resolution.contains(
                                delim
                            )
                        ) {
                            val resolutionList = resolution.split(delim)
                            if (resolutionList.size > 1 && resolutionList.get(0)
                                    .toIntOrNull() != null && resolutionList.get(1)
                                    .toIntOrNull() != null
                            ) {
                                setLog(
                                    "IType104ViewHolder",
                                    "aspectRatio-1-${(9).toDouble() / (16).toDouble()}-imageHeightByAspectRatio-$imageHeightByAspectRatio-itemHeight-$itemHeight"
                                )
                                val tempWidth = (resolutionList.get(0)).toDouble()
                                val tempHeight = (resolutionList.get(1)).toDouble()
                                val aspectRatio = tempHeight / tempWidth
                                imageHeightByAspectRatio = itemWidth * aspectRatio
                                itemHeight =
                                    imageHeightByAspectRatio + textSize + marginTop + marginBottom
                                setLog(
                                    "IType104ViewHolder",
                                    "aspectRatio-2-$aspectRatio-imageHeightByAspectRatio-$imageHeightByAspectRatio-itemHeight-$itemHeight"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val params: ViewGroup.LayoutParams = clMain.layoutParams
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    clMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivImage.layoutParams
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivImage.requestLayout()

                    ImageLoader.loadImage(
                        ctx,
                        ivImage,
                        mInAppSelfHandledModel?.image_url!!,
                        R.drawable.bg_gradient_placeholder
                    )

                    ivImage.setOnClickListener {
                        if (InAppCallback.mInAppCampaignList.values.size > 0) {


                            MoEInAppHelper.getInstance()
                                .selfHandledClicked(ctx, mInAppSelfHandledModel?.inAppCampaign!!)

                            if (onChildItemClick != null) {
                                if (!mInAppSelfHandledModel?.deeplink.isNullOrEmpty()) {
                                    val intent = CommonUtils.getDeeplinkIntentData(
                                        Uri.parse(
                                            mInAppSelfHandledModel?.deeplink
                                        )
                                    )
                                    intent.setClass(ctx, MainActivity::class.java)
                                    ctx.startActivity(intent)
                                }

                                ivImage.isEnabled = false
                                ivImage.isClickable = false
                                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                    ivImage.isEnabled = true
                                    ivImage.isClickable = true
                                }, 2000)
                            }
                        }

                    }

                    if (!isEventCall && mInAppSelfHandledModel != null) {
                        isEventCall = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val dataMap = HashMap<String, String>()
                            dataMap.put(
                                EventConstant.CAMPAIGN_ID_EPROPERTY,
                                mInAppSelfHandledModel?.campaign_id!!
                            )
                            dataMap.put(
                                EventConstant.TEMPLATE_ID_EPROPERTY,
                                mInAppSelfHandledModel?.templateId!!
                            )
                            dataMap.put(
                                EventConstant.TITLE_EPROPERTY, mInAppSelfHandledModel?.title!!
                            )
                            dataMap.put(
                                EventConstant.SUBTITLE_EPROPERTY, mInAppSelfHandledModel?.subTitle!!
                            )
                            dataMap.put(
                                EventConstant.OPTION_EPROPERTY, "" + mInAppSelfHandledModel?.options
                            )
                            dataMap.put(
                                EventConstant.BOTTOM_NAV_POSITION_EPROPERTY,
                                "" + mInAppSelfHandledModel?.bottom_nav_position!!
                            )
                            dataMap.put(
                                EventConstant.TOP_NAV_POSITION_EPROPERTY,
                                "" + mInAppSelfHandledModel?.top_nav_position!!
                            )
                            dataMap.put(
                                EventConstant.POSITION_EPROPERTY,
                                "" + mInAppSelfHandledModel?.position!!
                            )
                            EventManager.getInstance()
                                .sendEvent(ProgressiveSurveyViewEvent(dataMap))
                        }


                        // call whenever in-app is shown
                        MoEInAppHelper.getInstance()
                            .selfHandledShown(ctx, mInAppSelfHandledModel?.inAppCampaign!!)
                    }
                }

            }


        }
    }

    private inner class IType201ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var adView: AdView = itemView.findViewById(R.id.adView)
        var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        var ad_view_container: FrameLayout = itemView.findViewById(R.id.ad_view_container)
        private var initialLayoutComplete = false
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                ad_view_container.viewTreeObserver.addOnGlobalLayoutListener {
                    if (!initialLayoutComplete) {
                        setLog(
                            "HomeBucketBannerAds",
                            "position:${position} adUnitId-${list.get(position)?.adUnitId.toString()}"
                        )
                        initialLayoutComplete = true
                        val adView = loadBannerAds(
                            ctx,
                            list[position]?.adUnitId.toString(),
                            AdSize.MEDIUM_RECTANGLE,
                            ad_view_container
                        )
                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                // Code to be executed when an ad finishes loading.
                                setLog("HomeBucketBannerAds", "Loaded")
                                Utils.setMarginsTop(
                                    clMain,
                                    ctx.resources.getDimensionPixelSize(R.dimen.common_two_bucket_space_listing_page)
                                )
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                // Code to be executed when an ad request fails.
                                setLog("HomeBucketBannerAds", "Failed-" + adError.message)
                            }

                            override fun onAdOpened() {
                                setLog("HomeBucketBannerAds", "onAdOpened")
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                            }

                            override fun onAdClicked() {
                                setLog("HomeBucketBannerAds", "onAdClicked")
                                // Code to be executed when the user clicks on an ad.
                            }

                            override fun onAdClosed() {
                                setLog("HomeBucketBannerAds", "onAdClosed")
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                setLog("HomeBucketBannerAds", "onAdImpression")
                            }
                        }
                    }
                }
            }
        }
    }

    fun playPlayer() {
        simpleExoplayer?.play()
    }

    fun pausePlayer() {
        simpleExoplayer?.pause()
    }

    fun releasePlayer() {
        try {
            simpleExoplayer?.stop()
            simpleExoplayer?.release()
            simpleExoplayer = null
        } catch (e: Exception) {

        }

    }

    @OptIn(UnstableApi::class)
    private inner class IType47ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        var ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        var exoPlayerView: PlayerView = itemView.findViewById(R.id.episode_player_view)
        var llMute: LinearLayoutCompat = itemView.findViewById(R.id.llMute)
        var ivMuteUnmute: FontAwesomeImageView = itemView.findViewById(R.id.ivMuteUnmute)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvReadMore: ShowMoreTextView = itemView.findViewById(R.id.tvSubTitle)
        var rvOriginalEpisodes: RecyclerView = itemView.findViewById(R.id.rvOriginalEpisodes)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val preViewProgressBar: ProgressBar = itemView.findViewById(R.id.preViewProgressBar)
        var currentVolume = 0.0f
        var isMute = true
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position) {
                setLog(
                    "TAG",
                    "IType47ViewHolder 0 bind: data:${list.get(position)?.orignalItems?.get(0)}"
                )

                if (!list.get(position)?.orignalItems.isNullOrEmpty() && list.get(position)?.orignalItems?.size!! > 0) {

//                val list = list.get(position)?.data
//                val imageUrl = list?.image
                    var videoUri: String? = null
//                videoUri = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
//                videoUri = "https://hunstream.hungama.com/c/5/f60/f2e/58125461/58125461_,750,100,400,1000,1600,3000,6500,.mp4.m3u8?vjyjrH8V8iq1KfVtl8bIiLkQt-RYo_Xc_ITUZH8i7ioESjkwQGT7AQdH2Rnr4kWB6OSep5la4dcrLU4c80-JgXI6wqKTNT6NX7ja83eSH8gnoRfa5yJz4XZ7yIm2"
                    videoUri = list.get(position)?.orignalItems?.get(0)?.videoUrl

                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        list.get(position)?.orignalItems?.get(0)?.image!!,
                        R.drawable.bg_gradient_placeholder
                    )

                    if (!videoUri.isNullOrBlank()) {
                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-isVisible-${list.get(position)?.orignalItems?.get(0)?.isVisible}--position-$position")
                        if (list.get(position)?.orignalItems?.get(0)?.isVisible == true && (ctx as MainActivity).getAudioPlayerPlayingStatus() != Constant.playing) {
                            setLog(
                                "DiscoverTabFragment",
                                "DiscoverTabFragment-setData-onScrolled-centerPos-true-title-${
                                    list.get(position)?.orignalItems?.get(0)?.data?.title
                                }"
                            )
                            ivUserImage.Invisiable()
                            exoPlayerView.show()
                            preViewProgressBar.show()
                            val renderersFactory = DefaultRenderersFactory(ctx)

                            var mediaSource: MediaSourceFactory? = null
                            val track = Track()
                            track.id =
                                list.get(position)?.orignalItems?.get(0)?.data?.id?.toLong()!!
                            track.title = list.get(position)?.orignalItems?.get(0)?.data?.title
                            track.url = videoUri
                            mediaSource =
                                DefaultMediaSourceFactory(DemoUtil.getDataSourceFactory(ctx))

                            simpleExoplayer = SimpleExoPlayer.Builder(ctx, renderersFactory)
                                .setMediaSourceFactory(mediaSource)
                                .setHandleAudioBecomingNoisy(true).build()
                            setLog("TAG", "bind: videoUri:${videoUri}")

                            simpleExoplayer?.setMediaItem(CommonUtils.setMediaItem(track))
                            simpleExoplayer?.prepare()
                            exoPlayerView.player = simpleExoplayer
                            simpleExoplayer?.playWhenReady = true
                            currentVolume = simpleExoplayer?.volume!!
                            if (isMute) {
                                simpleExoplayer?.volume = 0.0f
                                ivMuteUnmute.setImageDrawable(
                                    ctx.faDrawable(
                                        R.string.icon_mute,
                                        R.color.colorWhite,
                                        ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                            .toFloat()
                                    )
                                )
                            } else {
                                simpleExoplayer?.volume = currentVolume
                                ivMuteUnmute.setImageDrawable(
                                    ctx.faDrawable(
                                        R.string.icon_unmute,
                                        R.color.colorWhite,
                                        ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                            .toFloat()
                                    )
                                )
                            }



                            simpleExoplayer?.addListener(object : Player.Listener {
                                override fun onPlayerError(error: PlaybackException) {
                                    super.onPlayerError(error)
                                    setLog("TAG", "onPlayerError: ${error}")
                                    setLog(
                                        "BucketChildAdapter",
                                        "BucketChildAdapter-IType47ViewHolder-onPlayerError: ${error}"
                                    )
                                    ivUserImage.show()
                                    exoPlayerView.Invisiable()
                                    preViewProgressBar.hide()
                                }

                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    super.onIsPlayingChanged(isPlaying)
                                    setLog(
                                        "BucketChildAdapter",
                                        "BucketChildAdapter-IType47ViewHolder-onIsPlayingChanged-isPlaying-${isPlaying}"
                                    )
                                    if (isPlaying) {
                                        ivUserImage.hide()
                                        exoPlayerView.show()
                                        preViewProgressBar.hide()
                                        llMute.show()
                                        llMute.setOnClickListener {
                                            if (isMute) {
                                                simpleExoplayer?.volume = currentVolume
                                                ivMuteUnmute.setImageDrawable(
                                                    ctx.faDrawable(
                                                        R.string.icon_unmute,
                                                        R.color.colorWhite,
                                                        ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                                            .toFloat()
                                                    )
                                                )
                                            } else {
                                                simpleExoplayer?.volume = 0.0f
                                                ivMuteUnmute.setImageDrawable(
                                                    ctx.faDrawable(
                                                        R.string.icon_mute,
                                                        R.color.colorWhite,
                                                        ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                                            .toFloat()
                                                    )
                                                )
                                            }
                                            isMute = !isMute
                                        }
                                    } else {
                                        ivUserImage.show()
                                        exoPlayerView.Invisiable()
                                        llMute.hide()
                                        preViewProgressBar.hide()
                                        llMute.setOnClickListener(null)
                                    }
                                }
                            })
                            exoPlayerView.addOnAttachStateChangeListener(object :
                                View.OnAttachStateChangeListener {
                                override fun onViewAttachedToWindow(v: View) {
                                    setLog("TAG", "onViewRecycled-onViewAttachedToWindow-$v")
                                }

                                override fun onViewDetachedFromWindow(v: View) {
                                    setLog("TAG", "onViewRecycled-onViewDetachedFromWindow-$v")
                                    exoPlayerView.player?.stop()
                                    exoPlayerView.player?.release()
                                    preViewProgressBar.hide()
                                }

                            })
                        } else {
                            setLog(
                                "DiscoverTabFragment",
                                "DiscoverTabFragment-setData-onScrolled-centerPos-false-title-${
                                    list.get(position)?.orignalItems?.get(0)?.data?.title
                                }"
                            )
                            ivUserImage.show()
                            exoPlayerView.Invisiable()
                            exoPlayerView.player?.stop()
                            exoPlayerView.player?.release()
                            preViewProgressBar.hide()
                        }
                    } else {
                        ivUserImage.show()
                        exoPlayerView.Invisiable()
                        preViewProgressBar.hide()
                    }
                    ivMore.setOnClickListener {
                        setLog("original", "Original-47-onMoreClick")
                        val bundle = Bundle()
                        bundle.putString(
                            Constant.defaultContentImage,
                            list.get(position)?.orignalItems?.get(0)?.image
                        )
                        bundle.putString(Constant.defaultContentId, list.get(position)?.id)
                        var varient = 1
                        bundle.putString(
                            Constant.defaultContentPlayerType, list.get(position)?.type?.toString()
                        )
                        val tvShowDetailsFragment = TvShowDetailsFragment(varient)
                        tvShowDetailsFragment.arguments = bundle
                        if (Utils.getCurrentFragment(ctx) != null) {
                            CommonUtils.addFragment(
                                ctx,
                                R.id.fl_container,
                                Utils.getCurrentFragment(ctx),
                                tvShowDetailsFragment,
                                false
                            )
                        }
                    }

                    rvOriginalEpisodes.layoutManager =
                        LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
                    rvOriginalEpisodes.visibility = View.VISIBLE

                    val SWIPE_MIN_DISTANCE = 100
                    val gestureDetector =
                        GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                            override fun onDown(e: MotionEvent): Boolean {
                                setLog("original", "Original-47-UP-onDown")
                                rvOriginalEpisodes.parent.requestDisallowInterceptTouchEvent(true)
                                return true
                            }

                            override fun onScroll(
                                startevent: MotionEvent,
                                finishevent: MotionEvent,
                                distanceX: Float,
                                distanceY: Float
                            ): Boolean {
                                try {
                                    setLog("original", "Original-47-UP-onScroll")
                                    val deltaX: Float = finishevent.x - startevent.x
                                    val deltaY: Float = finishevent.y - startevent.y
                                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                                        //Scrolling Horizontal
                                        if (Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
                                            if (deltaX > 0) {
                                                setLog("original", "Original-47-Right one")
                                            } else {
                                                setLog("original", "Original-47-Left one")
                                            }
                                        }
                                    } else {
                                        //Scrolling Vertical
                                        if (Math.abs(deltaY) > SWIPE_MIN_DISTANCE) {
                                            if (deltaY > 0) {
                                                setLog("original", "Original-47-Down one")
                                            } else {
                                                setLog("original", "Original-47-Up one")
                                            }
                                            rvOriginalEpisodes.parent.requestDisallowInterceptTouchEvent(
                                                false
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                }
                                return super.onScroll(startevent, finishevent, distanceX, distanceY)
                            }
                        })
                    rvOriginalEpisodes.addOnItemTouchListener(object :
                        RecyclerView.OnItemTouchListener {
                        override fun onInterceptTouchEvent(
                            rv: RecyclerView, event: MotionEvent
                        ): Boolean {
                            gestureDetector.onTouchEvent(event)
                            return false
                        }

                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                            setLog("original", "Original-47-onRequestDisallowInterceptTouchEvent-1")
                        }

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                            setLog("original", "Original-47-onTouchEvent-1")
                        }
                    })

                    tvTitle.text = list.get(position)?.orignalItems?.get(0)?.data?.title
                    if (!TextUtils.isEmpty(list.get(position)?.orignalItems?.get(0)?.description)) {
                        tvReadMore.text = list.get(position)?.orignalItems?.get(0)?.description
                        SaveState.isCollapse = true
                        tvReadMore.text = list.get(position)?.orignalItems?.get(0)?.description
                        tvReadMore.setShowingLine(2)
                        tvReadMore.addShowMoreText("read more")
                        tvReadMore.addShowLessText("read less")
                        tvReadMore.setShowMoreColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                        tvReadMore.setShowLessTextColor(
                            ContextCompat.getColor(
                                ctx, R.color.colorWhite
                            )
                        )
                        tvReadMore.setShowMoreStyle(Typeface.BOLD)
                        tvReadMore.setShowLessStyle(Typeface.BOLD)
                        tvReadMore.visibility = View.VISIBLE
                    } else {
                        tvReadMore.visibility = View.GONE
                    }

                    val adapter = OrignalEpisodeListAdapter(
                        ctx,
                        list.get(position)?.orignalItems?.get(0)?.data?.misc?.track as ArrayList<OrignalSeason.OrignalData.OrignalMisc.OrignalMiscTrack>,
                        initGlide()
                    )
                    rvOriginalEpisodes.adapter = adapter
                    rvOriginalEpisodes.setHasFixedSize(true)
                } else {
                    clMain.hide()
                }
            }

        }
    }

    private fun initGlide(): RequestManager {
        val options = RequestOptions()
        return Glide.with(ctx).setDefaultRequestOptions(options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        setLog("TAG", "onCreateViewHolder: " + viewType)
        if (!hasInitParentDimensions) {
            maxScreenWidth = getDeviceWidth(ctx)
            maxScreenHeight = getDeviceHeight(ctx)
            maxImageAspectRatio = maxScreenWidth.toFloat() / maxScreenHeight.toFloat()
            hasInitParentDimensions = true
        }
        commonTopMargin =
            ctx.resources.getDimensionPixelSize(R.dimen.common_space_between_title_and_artwork_space_listing_page)
                .toDouble()

        if (viewType == ROW_ITYPE_1) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble()
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 4.10
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_11)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_5).toDouble()
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            val circularViewExtraSpace =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_5).toDouble()
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + circularViewExtraSpace
            return IType1ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_1_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_2) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.65
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType2ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_2_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_3) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            //itemPaddingTop = ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()
            //itemPaddingBottom = ctx.resources.getDimensionPixelSize(R.dimen.dimen_17).toDouble()
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.10
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing

            val playIconSpacing = ctx.resources.getDimensionPixelSize(R.dimen.dimen_24).toDouble()
            textSize = lineOne + lineTwo + lineThree + playIconSpacing
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType3ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_3_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_4) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            return IType4ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_4_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_5) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 1.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType5ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_5_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_6) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.40
            }

            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 2
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType6ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_6_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_7) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 2.10
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType7ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_7_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_8) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.05
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType8ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_8_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_9) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 1.80
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType9ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_9_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_10) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType10ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_10_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_11) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_12).toDouble()
            lineTwo = 0.0
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType11ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_11_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_12) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.85
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType12ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_12_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_13) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.10
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 4
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType13ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_13_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_14) {
            //margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            //itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 1
            itemPadding = 0.0
            noOfColums = 2.5
            itemWidth = (maxScreenWidth - margin - itemPadding) / noOfColums
            lineOne =
                (ctx.resources.getDimensionPixelSize(R.dimen.font_15) + ctx.resources.getDimensionPixelSize(
                    R.dimen.dimen_12
                )).toDouble()
            lineTwo =
                (ctx.resources.getDimensionPixelSize(R.dimen.font_13) + ctx.resources.getDimensionPixelSize(
                    R.dimen.dimen_3
                )).toDouble()
            lineThree =
                (ctx.resources.getDimensionPixelSize(R.dimen.dimen_0) + ctx.resources.getDimensionPixelSize(
                    R.dimen.dimen_0
                ) + ctx.resources.getDimensionPixelSize(R.dimen.dimen_2_minus)).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = (itemWidth * 9 / 16)
            itemHeight =
                imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_20)
            return IType14ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_14_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_15) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType15ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_15_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_16) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.90
            }
            parentStartSpacing = ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_14).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_9).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_11).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            setLog("fshgsalgba", "16 $itemWidth" + imageHeightByAspectRatio.toString())
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType16ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_16_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_18) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.0
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            setLog("fshgsalgba", "18 " + imageHeightByAspectRatio.toString())
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType18ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_18_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_19) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.0
            }
            parentStartSpacing = 0.0
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType19ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_19_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_20) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.10
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType20ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_20_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_21) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.0
            }
            parentStartSpacing = 0.0
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType21ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_21, parent, false)
            )
        } else if (viewType == ROW_ITYPE_22) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.40
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 2
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType22ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_22_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_23) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 1.0
            } else {
                noOfColums = 1.0
            }
            parentStartSpacing = 0.0
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType23ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_23, parent, false)
            )
        } else if (viewType == ROW_ITYPE_41) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.20
                parentStartSpacing =
                    ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            } else {
                noOfColums = 2.95
                parentStartSpacing =
                    ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble()
            }
            parentStartSpacing /= noOfColums
            itemWidth =
                (maxScreenWidth - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType41ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_41_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_42) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.65
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType42ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_42_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_43) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.75
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType43ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_43_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_44) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_12).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            return IType44ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_2_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_45) {

            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = if (varient == ORIENTATION_VERTICAL) {
                2.0
            } else {
                1.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom

            return IType45ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_45_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_46) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = if (varient == ORIENTATION_VERTICAL) {
                2.0
            } else {
                2.10
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing


            imageHeightByAspectRatio = itemWidth * 3 / 4
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom

            return IType46ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_46_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_47) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_12).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            return IType47ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_47_original, parent, false)
            )
        } else if (viewType == ROW_ITYPE_48) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 1.50
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()
            textSize = lineOne
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom

            return IType48ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_48, parent, false)
            )
        } else if (viewType == ROW_ITYPE_51) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            noOfColums = if (varient == ORIENTATION_VERTICAL) {
                1.0
            } else {
                1.0
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType18ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_18_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_25) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            return IType25ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_1001, parent, false)
            )
        } else if (viewType == ROW_ITYPE_1000) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            noOfColums = 1.0
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_chart_detail_v2, parent, false)
            )
        } else if (viewType == ROW_ITYPE_101) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            return IType101ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_inapp_101, parent, false)
            )
        } else if (viewType == ROW_ITYPE_102) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            return IType102ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_inapp_102, parent, false)
            )
        } else if (viewType == ROW_ITYPE_103) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            return IType103ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_inapp_103, parent, false)
            )
        } else if (viewType == ROW_ITYPE_104) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            noOfColums = 1.0
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom
            return IType104ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_inapp_104, parent, false)
            )
        } else if (viewType == ROW_ITYPE_201) {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            return IType201ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_inapp_add_201, parent, false)
            )
        } else {
            marginStart = 0.0
            marginEnd = 0.0
            marginTop = 0.0
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL) {
                noOfColums = 2.0
            } else {
                noOfColums = 2.75
            }
            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13)
                .toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight =
                imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            return IType2ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_2_dynamic, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, row_position: Int) {
        val position = holder.adapterPosition
        setLog(TAG, "onBindViewHolder: position :${position} size:${list.size}")
        if (position < list.size) {
            setLog(
                "onBindViewHolderData",
                "" + position + " " + list[position]?.itype.toString() + " " + list[position]?.data?.title
            )
            if (list[position]?.itype == ROW_ITYPE_1) {
                (holder as IType1ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_2) {
                (holder as IType2ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_3) {
                (holder as IType3ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_4) {
                (holder as IType4ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_5) {
                (holder as IType5ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_6) {
                (holder as IType6ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_7) {
                setLog("TAG", "bind onBindViewHolder position:${position}")
                (holder as IType7ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_8) {
                (holder as IType8ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_9) {
                (holder as IType9ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_10) {
                (holder as IType10ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_11) {
                (holder as IType11ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_12) {
                (holder as IType12ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_13) {
                (holder as IType13ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_14) {
                (holder as IType14ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_15) {
                (holder as IType15ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_16) {
                (holder as IType16ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_18) {
                (holder as IType18ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_19) {
                (holder as IType19ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_20) {
                (holder as IType20ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_21) {
                (holder as IType21ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_22) {
                (holder as IType22ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_23) {
                (holder as IType23ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_41) {
                (holder as IType41ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_42) {
                (holder as IType42ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_43) {
                (holder as IType43ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_44) {
                (holder as IType44ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_45) {
                setLog("BucketType" + " " + list[position]?.itype + " " + list[position]?.data?.title)
                (holder as IType45ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_46) {
                (holder as IType46ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_47) {
                (holder as IType47ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_48) {
                (holder as IType48ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_51) {
                (holder as IType18ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_25) {
                (holder as IType25ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_1000) {
                (holder as IType1000ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_99999) {
                (holder as IType2ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_101) {
                (holder as IType101ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_102) {
                (holder as IType102ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_103) {
                (holder as IType103ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_104) {
                (holder as IType104ViewHolder).bind(position)
            } else if (list[position]?.itype == ROW_ITYPE_201) {
                (holder as IType201ViewHolder).bind(position)
            } else {
                //(holder as IType2ViewHolder).bind(position)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        //return list?.get(position)?.itype!!
        if (list.get(position)?.itype != null) {
            return list.get(position)?.itype!!
        } else {
            return -1
        }
    }

    fun clearData() {
        setLog("TAG", "clearData: 1 list size:${list.size}")
        list = ArrayList<BodyRowsItemsItem?>()
        notifyDataSetChanged()
        setLog("TAG", "clearData: 2 list size:${list.size}")
    }

    fun refreshData(items: ArrayList<BodyRowsItemsItem?>) {
        setLog("TAG", "refreshDatarefreshData continue-watching : 1 list size:${list.size}")
//        list=items
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
//        notifyDataSetChanged();
//        notifyItemRangeChanged(0,list?.size-1)

        setLog("TAG", "refreshData continue-watching: 2 list size:${list.size - 1}")
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, view: View? = null)
        fun onInAppSubmitClick(childPosition: InAppSelfHandledModel?, position: Int)
    }

    fun gcd(p: Int, q: Int): Int {
        return if (q == 0) p else gcd(q, p % q)
    }

    fun ratio(a: Int, b: Int) {
        val gcd = gcd(a, b)
        if (a > b) {
            showAnswer(a / gcd, b / gcd)
        } else {
            showAnswer(b / gcd, a / gcd)
        }
    }

    fun showAnswer(a: Int, b: Int) {
        println("$a $b")
    }
}