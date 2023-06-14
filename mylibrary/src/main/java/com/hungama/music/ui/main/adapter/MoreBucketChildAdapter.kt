package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.*
import com.hungama.music.utils.customview.stories.CircularStatusView
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.setArtImageBg
import com.hungama.music.utils.CommonUtils.setArtImageDarkBg
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.ORIENTATION_VERTICAL
import com.hungama.music.utils.customview.circleimageview.CircleImageView


class MoreBucketChildAdapter(
    context: Context,
    list: List<BodyRowsItemsItem?>,
    val varient: Int = ORIENTATION_VERTICAL,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        const val ROW_ITYPE_41 = 41
        const val ROW_ITYPE_42 = 42
        const val ROW_ITYPE_43 = 43
        const val ROW_ITYPE_99999 = 99999
        const val ROW_ITYPE_1000 = 1000
    }

    private val ctx: Context = context
    var list: List<BodyRowsItemsItem?>? = list



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
    var noOfColums = 2.0
    var itemWidth = 0.0
    var lineOne = 0.0
    var lineTwo = 0.0
    var lineThree = 0.0
    var textSize = 0.0
    var imageHeightByAspectRatio = 0.0
    var itemHeight = 0.0


    private inner class IType1ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val ivCircularStatusView: CircularStatusView =
            itemView.findViewById(R.id.circular_status_view)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }
            if (list.image != null) {
                /*Glide.with(ctx)
                    .load(list.image)
                    .placeholder(R.drawable.bg_gradient_placeholder)
                    .into(ivUserImage)*/
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            //val userStory = getDataFromJson(ctx, 0, position)!!.get(position)
            /*val userStory = list
            ivCircularStatusView.setPortionsCount(userStory.stories!!.size)

            val notSeenColor = itemView.context.resources.getColor(R.color.story_not_seen)
            val seenColor: Int =
                itemView.context.resources.getColor(R.color.half_opacity_white_color)
            val notSeenTextColor = itemView.context.resources.getColor(R.color.story_not_seen_text)
            val seenTextColor = itemView.context.resources.getColor(R.color.story_seen_text)
            setLog("IsAllSeen", userStory.isAllStorySeen!!.toString())
            if (userStory.isAllStorySeen!!) {
                //set all portions color
                ivCircularStatusView.setPortionsColor(seenColor)
                tvTitle.setTextColor(seenTextColor)
            } else {
                var allSeen = false
                for (i in 0 until userStory.stories!!.size) {
                    val status = userStory.viewInex
                    val color = if (i < status!!) seenColor else notSeenColor
                    allSeen = i < status
                    //set specific color for every portion
                    ivCircularStatusView.setPortionColorForIndex(i, color)
                }
                val textColor = if (allSeen) seenTextColor else notSeenTextColor
                tvTitle.setTextColor(textColor)
            }*/

            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }

        }
    }

    private inner class IType2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list?.size!! > position) {
                val list = list?.get(position)?.data

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
                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        list.image,
                        R.drawable.bg_gradient_placeholder
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
                        onChildItemClick.onUserClick(position)
                    }
                }
            }
        }
    }

    private inner class IType3ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
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
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
//                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {
                tvSubTitle.text = list.subTitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
//                tvSubTitle.visibility = View.GONE
            }

            if (list.misc != null&&list.misc?.languages!=null) {
                val cText=list.misc?.languages.toString().replace("[","",true)
                val cText1=cText.replace("]","",true)
                tvConentTitle.text = ""+cText1
                tvConentTitle.visibility = View.VISIBLE
            } else {
//                tvConentTitle.visibility = View.GONE
            }

            val params6: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
            params6.height = itemHeight.toInt()
            params6.width = itemWidth.toInt()
            ivUserImage.requestLayout()

            val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
            params.height = itemHeight.toInt()
            params.width = itemWidth.toInt()
            llMain.requestLayout()

            val imageHeight = imageHeightByAspectRatio.toInt()
            val imageWidth = itemWidth.toInt() - ctx.resources.getDimensionPixelSize(R.dimen.dimen_159) - ctx.resources.getDimensionPixelSize(R.dimen.dimen_16)

            val params5: ViewGroup.LayoutParams = clImages.getLayoutParams()
            params5.height = imageHeight
            params5.width = imageWidth
            clImages.requestLayout()

            val params1: ViewGroup.LayoutParams = ivUserImage1.getLayoutParams()
            params1.height = imageHeight
            params1.width = imageWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_32)
            ivUserImage1.requestLayout()

            val params2: ViewGroup.LayoutParams = ivUserImage2.getLayoutParams()
            params2.height = imageHeight
            params2.width = imageWidth  - ctx.resources.getDimensionPixelSize(R.dimen.dimen_32)
            ivUserImage2.requestLayout()

            val params3: ViewGroup.LayoutParams = ivUserImage3.getLayoutParams()
            params3.height = imageHeight
            params3.width = imageWidth  - ctx.resources.getDimensionPixelSize(R.dimen.dimen_32)
            ivUserImage3.requestLayout()
            if (list.images != null) {
                if (list?.images?.size!! > 0) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage1,
                        list?.images?.get(0)!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                if (list?.images?.size!!>1) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage2,
                        list?.images?.get(1)!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                if (list?.images?.size!!>2) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage3,
                        list?.images?.get(2)!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }


            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType4ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {
                //tvSubTitle.text = list.subTitle
                //tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
            val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
            params.height = itemHeight.toInt()
            params.width = itemWidth.toInt()
            llMain.requestLayout()
            val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
            params2.height = imageHeightByAspectRatio.toInt()
            params2.width = itemWidth.toInt()
            ivUserImage.requestLayout()
            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
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
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType5ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvLanguageView: TextView = itemView.findViewById(R.id.tvLanguageView)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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

            if (list.duration != null&&!TextUtils.isEmpty(list.duration)) {
                tvTime.text = DateUtils.formatElapsedTime(list.duration?.toLong()!!)
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }

            if (list.misc != null && !TextUtils.isEmpty(list.misc.f_playcount)) {
                var text=""
                if(list?.misc?.lang!=null){
                    if (list?.misc?.lang is String){
                        if (!TextUtils.isEmpty(list?.misc?.lang)){
                            text=list?.misc?.lang
                        }

                    }else{
                        if (!(list?.misc?.lang as List<String?>).isNullOrEmpty()){
                            text=TextUtils.join(",",list?.misc?.lang)
                        }
                    }

                }
                if(!TextUtils.isEmpty(text)){
                    text = text+" • "+ list.misc.f_playcount+ " " +ctx.getString(R.string.discover_str_24)
                }else{
                    text = list.misc.f_playcount+ " " +ctx.getString(R.string.discover_str_24)
                }

                tvLanguageView.setText(text)
                tvLanguageView.visibility = View.VISIBLE
            } else {
                tvLanguageView.visibility = View.GONE
            }

            if (list.image != null) {
                /*ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )*/
            } else {
                if (list.title != null) {
                    tvTitlePlaceHolder.text = list.title
                    tvTitlePlaceHolder.visibility = View.VISIBLE
                } else {
                    tvTitlePlaceHolder.visibility = View.GONE
                }
            }

            /*val margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8)
            val itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14) * 2
            val noOfColums = 2
            val itemWidth = (maxImageWidth - margin- itemPadding) / noOfColums
            val lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15) + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13)
            val lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13) + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3)
            val lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13) + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4)
            val textSize = lineOne + lineTwo + lineThree
            val imageHeightByAspectRatio = itemWidth * 9 / 16
            val itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22)*/
            val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
            params.height = itemHeight.toInt()
            params.width = itemWidth.toInt()
            llMain.requestLayout()
            val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
            params2.height = imageHeightByAspectRatio.toInt()
            params2.width = itemWidth.toInt()
            ivUserImage.requestLayout()
            Glide.with(ctx)
                .asBitmap()
                .load(list.image)
                .into(object : SimpleTarget<Bitmap>(){
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        /*val w = bitmap.getWidth()
                        val h = bitmap.getHeight()
                        val aspectRatio: Float = w.toFloat() / h.toFloat()
                        val targetImageWidth: Int =
                            if (aspectRatio < maxImageAspectRatio) {
                                // Tall image: height = max, width adjusts
                                (maxImageHeight * aspectRatio).roundToInt()
                            } else {
                                // Wide image: width = max
                                //maxImageWidth
                                (maxImageWidth - (3*8))
                            }*/

                        /*ivUserImage.layoutParams = ViewGroup.LayoutParams(
                            targetImageWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )*/

                        ivUserImage.setImageBitmap(bitmap);
                    }

                })
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType6ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: TextView = itemView.findViewById(R.id.txtRent)
        val ivSubscription: ImageView = itemView.findViewById(R.id.ivSubscription)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rlRating:RelativeLayout = itemView.findViewById(R.id.rlRating)
        val rating :TextView = itemView.findViewById(R.id.txtRating)
        fun bind(position: Int) {
            if(position<list?.size!!){
                val list = list!![position]?.data!!
                if (list.type.toString().equals(Constant.VIDEO_SHORT_FILMS, true)){
                    rlRating.hide()
                }else{
                    rlRating.show()
                }
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
                if (list.image != null) {
                    val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                    params.height = itemHeight.toInt()
                    params.width = itemWidth.toInt()
                    llMain.requestLayout()
                    val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                    params2.height = imageHeightByAspectRatio.toInt()
                    params2.width = itemWidth.toInt()
                    ivUserImage.requestLayout()
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        list.image,
                        R.drawable.bg_gradient_placeholder
                    )
                } else {
                    if (list.title != null) {
                        tvTitlePlaceHolder.text = list.title
                        tvTitlePlaceHolder.visibility = View.VISIBLE
                    } else {
                        tvTitlePlaceHolder.visibility = View.GONE
                    }
                }

                if (list.misc?.rating_critic?.toString().equals("0")){
                    rating.text = "1.0"
                }else{
                    val ratingString = list.misc?.rating_critic
                    val ratingDouble = ratingString?.toFloat()
                    setLog("TAG", "bind: "+ratingDouble)
                    rating.text = ratingDouble.toString()
                }

                if(list?.misc!=null&&list?.misc?.movierights!=null&&list?.misc?.movierights?.size!!>0){
                    Utils.setMovieRightTextForBucket(ivSubscription, list?.misc?.movierights!!, ctx, list?.id.toString(),false)
                }else{
                    txtRent.visibility=View.GONE
                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position)
                    }
                }
            }

        }
    }

    private inner class IType7ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {
                //tvSubTitle.text = list.subTitle
                //tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType8ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
            if (list.image != null) {
                /*val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight
                params.width = itemWidth
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio
                params2.width = itemWidth
                ivUserImage.requestLayout()*/
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType9ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout:LinearLayoutCompat = itemView.findViewById(R.id.rootParent)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
            if (list!!.image != null) {
                setArtImageBg(true, list.image!!, rootLayout)
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_16)- ctx.resources.getDimensionPixelSize(R.dimen.dimen_52)).toInt()
                params2.width = (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_64)).toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType10ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list?.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType11ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvLang: TextView = itemView.findViewById(R.id.tvLang)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivParent: ImageView = itemView.findViewById(R.id.ivParent)

        fun bind(position: Int) {
            val list = list?.get(position)?.data

            if (list!!.title != null) {
                tvTitle.text = list.title
            } else {
                tvTitle.text = ""
            }

            if (!TextUtils.isEmpty(list?.misc?.f_playcount)) {
                /*tvSubTitle.text = CommonUtils.ratingWithSuffix(list.misc?.playcount)+ctx.getString(
                    R.string.discover_str_23)*/
                tvSubTitle.text = list.misc?.f_playcount+ " " +ctx.getString(
                    R.string.discover_str_23)
            } else {
                tvSubTitle.text = ""
            }

            if(list?.misc?.lang!=null){
                if (list?.misc?.lang is String){
                    if (!TextUtils.isEmpty(list?.misc?.lang)){
                        tvLang.text = list.misc?.lang
                    } else {
                        tvLang.text = ""
                    }

                }else{
                    if (!(list?.misc?.lang as List<String?>).isNullOrEmpty()){
                        tvLang.text=TextUtils.join(",",list?.misc?.lang)
                    }else {
                        tvLang.text = ""
                    }
                }

            }
            /*if (list.misc?.lang != null && !TextUtils.isEmpty(list.misc?.lang)) {
                tvLang.text = list.misc?.lang
            } else {
                tvLang.text = ""
            }*/

            if (list.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_9)).toInt()
                params2.width = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_9)).toInt()
//                params2.width = (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_16)- ctx.resources.getDimensionPixelSize(R.dimen.dimen_18)).toInt()
                ivUserImage.requestLayout()
//                val params2: ViewGroup.LayoutParams = ivParent.getLayoutParams()
//                params2.height = imageHeightByAspectRatio.toInt()
//                params2.width = itemWidth.toInt()
//                ivParent.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
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
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType12ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: TextView = itemView.findViewById(R.id.tvSubTitle2)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
            if (list.misc != null && !TextUtils.isEmpty(list?.misc?.f_playcount)) {
                tvSubTitle2.text = list?.misc?.f_playcount+ " " +ctx.getString(R.string.discover_str_24)
                tvSubTitle2.visibility = View.VISIBLE
            } else {
                tvSubTitle2.visibility = View.GONE
            }
            if (list.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
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
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType13ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
            if (list.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
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
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType14ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitlePlaceHolder: TextView = itemView.findViewById(R.id.tvTitlePlaceHolder)
        val pbSong: ProgressBar = itemView.findViewById(R.id.pbSong)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            } else {
                if (list.title != null) {
                    tvTitlePlaceHolder.text = list.title
                    tvTitlePlaceHolder.visibility = View.VISIBLE
                } else {
                    tvTitlePlaceHolder.visibility = View.GONE
                }
            }
            pbSong.max=list?.duration?.toInt()!!
            pbSong.progress = HungamaMusicApp?.getInstance()?.getContentDuration(list?.id!!)?.toInt()!!


            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType15ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)

        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list?.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType16ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivUshape: ImageView = itemView.findViewById(R.id.ivUshape)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout:RelativeLayout = itemView.findViewById(R.id.rootParent)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {
                tvSubTitle.text = list.misc?.f_FavCount
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
            if (list.image != null) {
                setArtImageDarkBg(true, list.image, ivUshape)
                setArtImageBg(true, list.image, rootLayout)
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params3: ViewGroup.LayoutParams = rootLayout.getLayoutParams()
                params3.height = (itemHeight - ctx.resources.getDimensionPixelSize(R.dimen.dimen_10)).toInt()
                params3.width = itemWidth.toInt()
                rootLayout.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = (imageHeightByAspectRatio  - ctx.resources.getDimensionPixelSize(R.dimen.dimen_11)- ctx.resources.getDimensionPixelSize(R.dimen.dimen_67)).toInt()
                params2.width = (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_80)).toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )

            }

            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }


    private inner class IType18ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val main: ConstraintLayout = itemView.findViewById(R.id.main)

        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list?.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            main.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType19ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvHeaderTitle: TextView = itemView.findViewById(R.id.tvHeaderTitle)
        var tvHeaderSubTitle: TextView = itemView.findViewById(R.id.tvHeaderSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivHeader: ImageView = itemView.findViewById(R.id.ivHeader)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
                if(list.misc.headerTitle !=null){
                    tvHeaderTitle.text = list.misc.headerTitle
                    tvHeaderTitle.visibility = View.VISIBLE
                }

                if(list.misc.headerSubTitle !=null){
                    tvHeaderSubTitle.text = list.misc.headerSubTitle
                    tvHeaderSubTitle.visibility = View.VISIBLE
                }

                if (list.misc.headerImage != null) {
                    ImageLoader.loadImage(
                        ctx,
                        ivHeader,
                        list.misc.headerImage,
                        R.drawable.bg_gradient_placeholder
                    )
                }

            }

            if (list?.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType20ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        fun bind(position: Int) {
            val list = list!![position]!!.data
            if (list?.image != null) {

                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            ivUserImage.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType21ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: AppCompatTextView = itemView.findViewById(R.id.txtRent)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout:RelativeLayout = itemView.findViewById(R.id.rootParent)
        val llRent: LinearLayoutCompat = itemView.findViewById(R.id.llRent)
        fun bind(position: Int) {
            val list = list!![position]!!.data
            CommonUtils.applyButtonTheme(ctx, llRent)
            if (position < 11) {
                val tempPos = position + 1
                if (tempPos == 10) {
                    tvNumber.text = "" + tempPos
                } else {
                    tvNumber.text = "0$tempPos"
                }
                val paint = tvNumber.paint
                val width = paint.measureText(tvNumber.text.toString())
                val angleInRadians = Math.toRadians(90.0).toFloat()
                val endX = (Math.cos(angleInRadians.toDouble()) * width).toFloat()
                val endY = (Math.sin(angleInRadians.toDouble()) * width).toFloat()
                val textShader: Shader = LinearGradient(
                    0f, 0f, endX, endY, intArrayOf(
                        Color.parseColor("#ffffff"),
                        Color.parseColor("#00ffffff")
                    ), null, Shader.TileMode.CLAMP
                )
                tvNumber.paint.shader = textShader
            }
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

            if(list?.misc!=null&&list?.misc?.movierights!=null&&list?.misc?.movierights?.size!!>0){
                    Utils.setMovieRightTextForDetail(txtRent, list?.misc?.movierights!!, ctx, list?.id.toString())
                }else{
                    txtRent.visibility=View.GONE
                }

            if (list.image != null) {
//                setArtImageBg(true, list.image, rootLayout)
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                rootLayout.setBackgroundColor(ContextCompat.getColor(ctx, R.color.home_bg_color))
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }

    }

    private inner class IType22ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list?.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType23ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubTitle: AppCompatTextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: AppCompatTextView = itemView.findViewById(R.id.txtRent)
        val tvSubTitle2: AppCompatTextView = itemView.findViewById(R.id.tvSubTitle2)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val llRent: LinearLayoutCompat = itemView.findViewById(R.id.llRent)
        fun bind(position: Int) {
            val list = list!![position]!!.data
            applyButtonTheme(ctx, llRent)
            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {

                var subTitle=list.subTitle

                if(list.misc !=null&& list.misc.lang !=null){
                    subTitle=subTitle+" - "+ list.misc.lang
                }
                tvSubTitle.text = subTitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }

            if(list?.misc!=null&&list?.misc?.movierights!=null&&list?.misc?.movierights?.size!!>0){
                Utils.setMovieRightTextForDetail(txtRent, list?.misc?.movierights!!, ctx, list?.id.toString())
            }else{
                txtRent.visibility=View.GONE
            }
            if (list.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType41ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
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
            val list = list?.get(position)?.data
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

            if (list?.title != null) {
                val str = list.title
                val splited: List<String> = str?.split(" ")!!
                for (i in 0 until splited.size) {
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
                }
            } else {
                tvName1.visibility = View.INVISIBLE
                tvName2.visibility = View.INVISIBLE
            }

            val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
            params.height = itemHeight.toInt()
            params.width = itemWidth.toInt()
            llMain.requestLayout()

            val params4: ViewGroup.LayoutParams = cardMain.getLayoutParams()
            params4.height = imageHeightByAspectRatio.toInt()
            params4.width = itemWidth.toInt()
            cardMain.requestLayout()

            val params6: ViewGroup.LayoutParams = rootLayout.getLayoutParams()
            params6.height = imageHeightByAspectRatio.toInt()
            params6.width = itemWidth.toInt()
            rootLayout.requestLayout()

            val params2: ViewGroup.LayoutParams = ivLeftImage.getLayoutParams()
            params2.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(R.dimen.font_26)).toInt()
            params2.width = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(R.dimen.font_26)).toInt()
            ivLeftImage.requestLayout()
            if (list?.images != null && list?.images?.size!! > 0) {

                ImageLoader.loadImage(
                    ctx,
                    ivLeftImage,
                    list?.images?.get(0)!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    ctx,
                    ivLeftImage,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

            val params3: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
            params3.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_50) - ctx.resources.getDimensionPixelSize(R.dimen.font_26)).toInt()
            params3.width = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_50) - ctx.resources.getDimensionPixelSize(R.dimen.font_26)).toInt()
            ivUserImage.requestLayout()
            if (list?.images != null && list?.images?.size!! > 1) {
                setArtImageBg(true, list?.images?.get(1)!!, rootLayout)

                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list?.images?.get(1)!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

            val params5: ViewGroup.LayoutParams = ivRightImage.getLayoutParams()
            params5.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(R.dimen.font_26)).toInt()
            params5.width = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_65) - ctx.resources.getDimensionPixelSize(R.dimen.font_26)).toInt()
            ivRightImage.requestLayout()
            if (list?.images != null && list?.images?.size!! > 2) {

                ImageLoader.loadImage(
                    ctx,
                    ivRightImage,
                    list?.images?.get(2)!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    ctx,
                    ivRightImage,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType42ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivUserImage2: ImageView = itemView.findViewById(R.id.ivUserImage2)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
                val list = list!![position]!!.data

                if (list?.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

            if (list!!.misc != null && !TextUtils.isEmpty(list.misc?.f_playcount)) {
                tvSubTitle.text = list.misc?.f_playcount+ " " +ctx.getString(R.string.discover_str_24)
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }

            if (list?.images!! != null && list?.images?.size!! > 0) {
                val turl=list?.images?.get((0..list?.images?.size!!-1).random())
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    turl!!,
                    R.drawable.bg_gradient_placeholder
                )
            }

            setLog("imageCheckMoreBucket", position.toString() + " " + list?.image.toString())
            if (list?.image != null) {
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage2.getLayoutParams()
                params2.height = imageHeightByAspectRatio.toInt()
                params2.width = itemWidth.toInt()
                ivUserImage2.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage2,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
        }

    private inner class IType43ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle2: TextView = itemView.findViewById(R.id.tvTitle2)
        var tvSubTitle2: TextView = itemView.findViewById(R.id.tvSubTitle2)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout:RelativeLayout = itemView.findViewById(R.id.rootParent)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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

            if (list?.image != null) {
                setArtImageBg(true, list.image, rootLayout)
                val params: ViewGroup.LayoutParams = llMain.getLayoutParams()
                params.height = itemHeight.toInt()
                params.width = itemWidth.toInt()
                llMain.requestLayout()
                val params2: ViewGroup.LayoutParams = ivUserImage.getLayoutParams()
                params2.height = (imageHeightByAspectRatio - ctx.resources.getDimensionPixelSize(R.dimen.dimen_40)).toInt()
                params2.width = (itemWidth - ctx.resources.getDimensionPixelSize(R.dimen.dimen_40)).toInt()
                ivUserImage.requestLayout()
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    private inner class IType1000ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: TextView = itemView.findViewById(R.id.tvSubTitle2)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

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
                CommonUtils.makeTextViewResizable(tvSubTitle2, 3, "read more", true)
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
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (!hasInitParentDimensions) {
            maxScreenWidth = CommonUtils.getDeviceWidth(ctx)
            maxScreenHeight = CommonUtils.getDeviceHeight(ctx)
            maxImageAspectRatio = maxScreenWidth.toFloat() / maxScreenHeight.toFloat()
            hasInitParentDimensions = true
        }

        if (viewType == ROW_ITYPE_1) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 4.10
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType1ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_1, parent, false)
            )
        } else if (viewType == ROW_ITYPE_2) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.75
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType2ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_2_dynamic, parent, false)
            )
        } else if (viewType == ROW_ITYPE_3) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.20
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType3ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_3, parent, false)
            )
        } else if (viewType == ROW_ITYPE_4) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType4ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_4, parent, false)
            )
        } else if (viewType == ROW_ITYPE_5) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 1.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            textSize = lineOne + lineTwo + lineThree
            val aspectRatioByHeight = 9.toFloat() / 16.toFloat()
            imageHeightByAspectRatio = (itemWidth * aspectRatioByHeight).toDouble()
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22)
            return IType5ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_5_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_6) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }

            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_12).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 2
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType6ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_6_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_7) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.20
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType7ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_7, parent, false)
            )
        } else if (viewType == ROW_ITYPE_8) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.20
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType8ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_8_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_9) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 1.80
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType9ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_9_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_10) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType10ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_10_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_11) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType11ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_11_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_12) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType12ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_12_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_13) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.10
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 4
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType13ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_13_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_14) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 1.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 4
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType14ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_14, parent, false)
            )
        } else if (viewType == ROW_ITYPE_15) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType15ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_15_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_16) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.20
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType16ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_16_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_18) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.0
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType18ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_18, parent, false)
            )
        } else if (viewType == ROW_ITYPE_19) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.0
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType19ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_19, parent, false)
            )
        } else if (viewType == ROW_ITYPE_20) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.20
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType20ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_20, parent, false)
            )
        } else if (viewType == ROW_ITYPE_21) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.0
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType21ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_21_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_22) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.40
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 3 / 2
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType22ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_22_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_23) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 1.0
            }else{
                noOfColums = 1.0
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 21
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType23ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_23, parent, false)
            )
        }else if (viewType == ROW_ITYPE_41) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.50
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType41ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_41_more, parent, false)
            )
        } else if (viewType == ROW_ITYPE_42) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.30
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType42ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_42_more, parent, false)
            )
        }else if (viewType == ROW_ITYPE_43) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.75
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize
            return IType43ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_43_more, parent, false)
            )
        }else if (viewType == ROW_ITYPE_1000) {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            noOfColums = 1.0
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_chart_detail_v2, parent, false)
            )
        } else {
            margin = ctx.resources.getDimensionPixelSize(R.dimen.dimen_8).toDouble()
            itemPadding = ctx.resources.getDimensionPixelSize(R.dimen.dimen_14).toDouble() * 2.0
            if (varient == ORIENTATION_VERTICAL){
                noOfColums = 2.0
            }else{
                noOfColums = 2.75
            }
            itemWidth = (maxScreenWidth - margin- itemPadding) / noOfColums
            lineOne = ctx.resources.getDimensionPixelSize(R.dimen.font_15).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            lineTwo = ctx.resources.getDimensionPixelSize(R.dimen.font_13).toDouble() + ctx.resources.getDimensionPixelSize(R.dimen.dimen_3).toDouble()
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 1 / 1
            itemHeight = imageHeightByAspectRatio + textSize + ctx.resources.getDimensionPixelSize(R.dimen.dimen_22).toDouble()
            return IType2ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_2, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (list!![position]?.itype == ROW_ITYPE_1) {
            (holder as IType1ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_2) {
            (holder as IType2ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_3) {
            (holder as IType3ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_4) {
            (holder as IType4ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_5) {
            (holder as IType5ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_6) {
            (holder as IType6ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_7) {
            (holder as IType7ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_8) {
            (holder as IType8ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_9) {
            (holder as IType9ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_10) {
            (holder as IType10ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_11) {
            (holder as IType11ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_12) {
            (holder as IType12ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_13) {
            (holder as IType13ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_14) {
            (holder as IType14ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_15) {
            (holder as IType15ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_16) {
            (holder as IType16ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_18) {
            (holder as IType18ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_19) {
            (holder as IType19ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_20) {
            (holder as IType20ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_21) {
            (holder as IType21ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_22) {
            (holder as IType22ViewHolder).bind(position)
        } else if (list!![position]?.itype == ROW_ITYPE_23) {
            (holder as IType23ViewHolder).bind(position)
        }else if (list!![position]?.itype == ROW_ITYPE_41) {
            (holder as IType41ViewHolder).bind(position)
        }else if (list!![position]?.itype == ROW_ITYPE_42) {
            (holder as IType42ViewHolder).bind(position)
        }else if (list!![position]?.itype == ROW_ITYPE_43) {
            (holder as IType43ViewHolder).bind(position)
        }else if (list!![position]?.itype == ROW_ITYPE_99999) {
            (holder as IType2ViewHolder).bind(position)
        }else if (list!![position]?.itype == ROW_ITYPE_1000) {
            (holder as IType1000ViewHolder).bind(position)
        } else {
           // (holder as IType2ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (list?.get(position)?.itype != null){
            return list?.get(position)?.itype!!
        }else{
            return -1
        }
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}