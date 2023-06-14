package com.hungama.music.player.videoplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.data.model.PlayableContentModel
import com.hungama.music.player.videoplayer.adapter.SubtitleAdapter
import com.hungama.music.ui.main.viewmodel.MusicLanguageViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.subtitle_select_bottom_sheet_layout.*
import java.util.*
import kotlin.collections.ArrayList

class SubtitleSelectBottomSheetFragment(val videoListModel: PlayableContentModel?) :
    SuperBottomSheetFragment() {
    var onSubTitleItemClick: OnSubTitleItemClick? = null
    var musicLanguageListViewModel: MusicLanguageViewModel? = null
    var myLocale: Locale? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.subtitle_select_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (videoListModel != null && videoListModel?.data?.head?.headData?.misc?.sl?.subtitle!=null) {
            var subtitleList=ArrayList<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem>()
            subtitleList.add(videoListModel?.data?.head?.headData?.misc?.sl?.subtitle!!)
            rvQuality.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = SubtitleAdapter(
                    context, subtitleList,
                    object : SubtitleAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            setLog(
                                "TAG",
                                "SubtitleAdapter onUserClick:" + subtitleList?.get(
                                    childPosition
                                )
                            )

                            if (onSubTitleItemClick != null) {
                                onSubTitleItemClick?.onSubTitleItemClick(
                                    videoListModel!!,
                                    subtitleList?.get(childPosition)!!
                                )
                            }
                        }


                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }else{
            dismiss()
            val messageModel = MessageModel(getString(R.string.no_subtitle_available),
                MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.video_sheet_rounded_corner)

    override fun isSheetAlwaysExpanded(): Boolean  = true
    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_160).toInt()

    override fun getWidth(): Int = requireContext().resources.getDimension(R.dimen.dimen_375).toInt()

    //override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_160).toInt()

    override fun getBackgroundColor(): Int = requireContext().resources.getColor(R.color.transparent)

    fun addSubTitleListener(onSubTitleItemClick: OnSubTitleItemClick){
        this.onSubTitleItemClick=onSubTitleItemClick
    }

    interface OnSubTitleItemClick {
        fun onSubTitleItemClick(model: PlayableContentModel?, subtitleItem: PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?)
    }

}

