package com.hungama.music.player.videoplayer

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.player.videoplayer.adapter.VideoQualityAdapter
import com.hungama.music.data.model.VideoQuality
import com.hungama.music.ui.main.viewmodel.MusicLanguageViewModel
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.subtitle_select_bottom_sheet_layout.*
import java.util.*
import kotlin.collections.ArrayList

class VideoQualitySelectBottomSheetFragment(val videoQualityMap: HashMap<String, Int>) :
    SuperBottomSheetFragment() {
    var onVideoQualityItemClick: OnVideoQualityItemClick? = null
    var musicLanguageListViewModel: MusicLanguageViewModel? = null
    var myLocale: Locale? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.video_quality_select_bottom_sheet_layout, container, false)
    }

    var videoQualityArrayList = ArrayList<VideoQuality>()
    var defaultQualitySelected =false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvQuality.apply {
            layoutManager =
                GridLayoutManager(context, 1)

            videoQualityArrayList.clear()
            defaultQualitySelected =false
            videoQualityMap.keys.forEach {
                val videoQuality = VideoQuality()
                if (it.equals(getString(R.string.quality_1))){
                    videoQuality.id = 1
                }else if (it.equals(getString(R.string.video_player_str_27))){
                    videoQuality.id = 2
                }else if (it.equals(getString(R.string.video_player_str_28))){
                    videoQuality.id = 3
                }else if (it.equals(getString(R.string.video_player_str_29))){
                    videoQuality.id = 4
                }
                videoQuality.title = it
                videoQuality.bandwidth = it
                videoQuality.bitrate = videoQualityMap.get(it)


                if (!TextUtils.isEmpty(
                        SharedPrefHelper.getInstance().get(PrefConstant.LAST_VIDEO_QUALITY, "")
                    )
                    && it.contains(
                        SharedPrefHelper.getInstance().get(PrefConstant.LAST_VIDEO_QUALITY, "")
                    )
                ) {
                    videoQuality.isSelected = true
                    defaultQualitySelected=true
                }else{
                    videoQuality.isSelected = false
                }

                videoQualityArrayList.add(videoQuality)
            }
            val sortedList = videoQualityArrayList.sortedWith(compareBy { it.id })
            videoQualityArrayList = ArrayList()
            videoQualityArrayList.addAll(sortedList)
            if(!defaultQualitySelected){
                videoQualityArrayList.get(0).isSelected=true
            }

            adapter = VideoQualityAdapter(context, videoQualityArrayList,
                object : VideoQualityAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {

                        if (onVideoQualityItemClick != null) {
                            onVideoQualityItemClick?.onVideoResolutionClick(
                                videoQualityArrayList.get(
                                    childPosition
                                )
                            )
                        }
                        setLog(
                            "TAG",
                            "VideoQualityAdapter onUserClick:" + videoQualityArrayList.get(
                                childPosition
                            )
                        )



                    }


                })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }

    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.video_sheet_rounded_corner)

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_275).toInt()

    override fun getWidth(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_375).toInt()

    //override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_160).toInt()

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)


    fun addVideoListener(onVideoQualityItemClick: OnVideoQualityItemClick) {
        this.onVideoQualityItemClick = onVideoQualityItemClick
    }

    interface OnVideoQualityItemClick {
        fun onVideoResolutionClick(childPosition: VideoQuality)
    }
}

