package com.hungama.music.utils.customview

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.toBitmap
import com.hungama.music.R
import kotlinx.android.synthetic.main.layout_tab_view.view.*
import kotlinx.android.synthetic.main.swipable_player_controls_layout.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.URL
import java.util.*


class CustomTabView @JvmOverloads constructor(
    val mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(mContext, attrs, defStyle), View.OnClickListener {

    private var onItemChange: OnTabItemChange? = null
    val isAnimateIcons = true
    private var anim: ObjectAnimator? = null
    init {
        LayoutInflater.from(mContext).inflate(R.layout.layout_tab_view, this, true)
        if (isAnimateIcons){
            ivTabDiscover?.hide()
            ivTabDiscoverAnim?.show()
            ivTabMusic?.hide()
            ivTabMusicAnim?.show()
            ivTabVideo?.hide()
            ivTabVideoAnim?.show()
            ivTabSearch?.hide()
            ivTabSearchAnim?.show()
            ivTabLibrary?.hide()
            ivTabLibraryAnim?.show()
            ivTabPlayer?.show()
        }else{
            ivTabDiscover?.show()
            ivTabDiscoverAnim?.hide()
            ivTabMusic?.show()
            ivTabMusicAnim?.hide()
            ivTabVideo?.show()
            ivTabVideoAnim?.hide()
            ivTabSearch?.show()
            ivTabSearchAnim?.hide()
            ivTabLibrary?.show()
            ivTabLibraryAnim?.hide()
            ivTabPlayer?.show()
        }

        llDiscover?.setOnClickListener(this)
        llMusic?.setOnClickListener(this)
        llVideo?.setOnClickListener(this)
        llSearch?.setOnClickListener(this)
        llLibrary?.setOnClickListener(this)
        llTabPlayer?.setOnClickListener(this)
        llPodcast?.setOnClickListener(this)


        tvTabDiscover?.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlack))
        ivTabDiscover?.isSelected = false
        llDiscover?.performClick()
    }

    override fun onClick(view: View) {
        if (view === llDiscover /*&& !ivTabDiscover.isSelected*/) {
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(0)
            }
            setBottomTabSelection(0)
        } else if (view === llMusic/* && !ivTabMusic.isSelected*/) {
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(1)
            }
            setBottomTabSelection(1)
        } else if (view === llVideo /*&& !ivTabVideo.isSelected*/) {
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(2)
            }
            setBottomTabSelection(2)
        } else if (view === llSearch && !ivTabSearch.isSelected) {
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(3)
            }
            setBottomTabSelection(3)
        } else if (view === llLibrary /*&& !ivTabLibrary.isSelected*/) {
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(4)
            }
            setBottomTabSelection(4)
        } else if (view === llTabPlayer && !ivTabPlayer.isSelected) {
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(5)
            }
            setBottomTabSelection(5)
        } else if (view === llPodcast ){
            if (onItemChange != null) {
                onItemChange?.onTabItemClick(6)
            }
            setBottomTabSelection(6)

        }

    }

    public fun unselectAll() {
        ivTabDiscover?.isSelected = false
        ivTabMusic?.isSelected = false
        ivTabVideo?.isSelected = false
        ivTabSearch?.isSelected = false
        ivTabLibrary?.isSelected = false
        ivTabPlayer?.isSelected = false

        tvTabDiscover?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))
        tvTabMusic?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))
        tvTabVideo?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))
        tvTabSearch?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))
        tvTabLibrary?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))
        tvTabPlayer?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))
        tvTabPodcast?.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey))

        removeAnimationProgress()
    }

    fun setItemChangeListener(onItemChange: OnTabItemChange) {
        this.onItemChange = onItemChange
    }

    fun setMatchLabel(label: String) {
        tvTabMusic?.text = label
    }

    fun setMainViewColor(color: Int) {
        llMainView?.setBackgroundColor(color)
    }

    interface OnTabItemChange {
        fun onTabItemClick(position: Int)
    }
    fun setHomeSelected() {
        unselectAll()
        ivTabDiscover.isSelected = true
        tvTabDiscover.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
    }

    fun setBottomTabSelection(tabId: Int) {
        unselectAll()
        val typeface = ResourcesCompat.getFont(
            mContext,
            R.font.oplus_sans_medium
        )
        when (tabId) {
            0 -> {
                //android.os.Handler(Looper.getMainLooper()).post {
                ivTabDiscover?.isSelected = true
                tvTabDiscover?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabDiscover?.typeface = typeface
                if (isAnimateIcons) {
                    //Utils.likeAnimation(ivTabDiscoverAnim, R.raw.discover_icon, true, R.drawable.ic_home_bottom_navigation_main)
                    ivTabDiscoverAnim?.playAnimation()
                }
                //}
            }
            1 -> {
                ivTabMusic?.isSelected = true
                tvTabMusic?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabMusic?.typeface = typeface
                if (isAnimateIcons) {
                    //Utils.likeAnimation(ivTabMusicAnim, R.raw.listen_icon, true, R.drawable.ic_music_bottom_navigation_main)
                    ivTabMusicAnim?.playAnimation()
                }
            }
            2 -> {
                ivTabVideo?.isSelected = true
                tvTabVideo?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabVideo?.typeface = typeface
                if (isAnimateIcons) {
                    //Utils.likeAnimation(ivTabVideoAnim, R.raw.watch_icon, true, R.drawable.ic_video_bottom_navigation_main)
                    ivTabVideoAnim?.playAnimation()
                }
            }
            3 -> {
                ivTabSearch?.isSelected = true

                tvTabSearch?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabSearch?.typeface = typeface

                if (isAnimateIcons) {
                    //Utils.likeAnimation(ivTabSearchAnim, R.raw.search_icon, true, R.drawable.ic_home_bottom_navigation_main)
                    ivTabSearchAnim?.playAnimation()
                }
            }
            4 -> {
                ivTabLibrary?.isSelected = true
                tvTabLibrary?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabLibrary?.typeface = typeface
                if (isAnimateIcons) {
                    //Utils.likeAnimation(ivTabLibraryAnim, R.raw.library_icon, true, R.drawable.ic_home_bottom_navigation_main)
                    ivTabLibraryAnim.playAnimation()
                }
            }
            5 -> {
                ivTabPlayer?.isSelected = true
                tvTabPlayer?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabPlayer?.typeface = typeface
            }
            6 -> {
                ivTabPodcast?.isSelected = true
                tvTabPodcast?.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
                tvTabPodcast?.typeface = typeface
            }
        }
    }
    fun removeAnimationProgress(){
        if (isAnimateIcons){
            ivTabDiscoverAnim?.cancelAnimation()
            ivTabDiscoverAnim?.progress = 0f
            ivTabMusicAnim?.cancelAnimation()
            ivTabMusicAnim?.progress = 0f
            ivTabVideoAnim?.cancelAnimation()
            ivTabVideoAnim?.progress = 0f
            ivTabSearchAnim?.cancelAnimation()
            ivTabSearchAnim?.progress = 0f
            ivTabLibraryAnim?.cancelAnimation()
            ivTabLibraryAnim?.progress = 0f
        }
    }

    fun updatePlayerTabArtwork(artworkUrl:String?){
        ivTabPlayerFg?.hide()
        try {
            setLog("artworkUrl","${artworkUrl}")
            setLog("ivTabPlayer","${ivTabPlayer}")
            if (!TextUtils.isEmpty(artworkUrl)){
                ImageLoader.loadImage(
                    mContext,ivTabPlayer,
                    artworkUrl!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    mContext,ivTabPlayer,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
        }catch (e:Exception){

        }
        //ivTabPlayer.startAnimation(rotateAnimation)
    }

    fun updatePlayPauseIcon(status:Int){
        /*when (status){
            Constant.playing -> {
                ivTabPlayPause?.setImageDrawable(
                    mContext.faDrawable(
                        R.string.icon_pause_2,
                        R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_9).toFloat()
                    )
                )
                anim?.resume()
                tvTabPlayer?.text = mContext.getString(R.string.now_playing)
            }
            Constant.pause -> {
                ivTabPlayPause?.setImageDrawable(
                    mContext.faDrawable(
                        R.string.icon_play,
                        R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_9).toFloat()
                    )
                )
                anim?.pause()
                tvTabPlayer?.text = mContext.getString(R.string.movie_str_7)
            }
            else -> {
                ivTabPlayPause?.setImageDrawable(
                    mContext.faDrawable(
                        R.string.icon_pause_2,
                        R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_9).toFloat()
                    )
                )
                anim?.pause()
                tvTabPlayer?.text = mContext.getString(R.string.movie_str_7)
            }
        }*/
    }
}