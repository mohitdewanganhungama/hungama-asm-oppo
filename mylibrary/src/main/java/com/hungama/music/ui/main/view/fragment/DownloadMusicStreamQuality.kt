package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.adapter.MusicPlaybackSettingQualityAdapter
import com.hungama.music.data.model.MusicPlaybackSettingStreamQualityModel
import com.hungama.music.data.model.QualityAction
import com.hungama.music.utils.Constant.DOWNLOAD_MUSICSTREAMQUALITY
import kotlinx.android.synthetic.main.music_playback_setting_stream_quality.*
import java.util.HashMap

class DownloadMusicStreamQuality(
    val streamQualityList: ArrayList<MusicPlaybackSettingStreamQualityModel>,
    val onItemClick: OnItemClick
) : SuperBottomSheetFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.music_playback_setting_stream_quality, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle.setText(getString(R.string.general_setting_str_20))

        btnAsk?.setOnClickListener {
            dismiss()
        }
        rvStreamQuality.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 1)
            adapter = MusicPlaybackSettingQualityAdapter(requireContext(), streamQualityList,
                object : MusicPlaybackSettingQualityAdapter.OnItemClick {
                    override fun onUserClick(position: Int, onlyDismissPopUp:Boolean) {
                        if (onItemClick != null){
                            if (!onlyDismissPopUp){
                                val userDataMap= HashMap<String, String>()
                                userDataMap.put(EventConstant.MUSIC_DOWNLOAD_QUALITY,streamQualityList?.get(position)?.title!!)
                                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                                onItemClick.onUserClick(position, DOWNLOAD_MUSICSTREAMQUALITY)
                            }
                            dismiss()
                        }
                    }


                }, QualityAction.MUSIC_PLAYBACK_DOWNLOAD_QUALITY)
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_400).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

    interface OnItemClick {
        fun onUserClick(position: Int, settingType:Int)
    }
}

