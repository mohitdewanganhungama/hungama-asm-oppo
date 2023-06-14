package com.hungama.music.player.videoplayer

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.data.model.PlayableContentModel
import com.hungama.music.player.videoplayer.adapter.AudioSubtitleAdapter
import com.hungama.music.player.videoplayer.adapter.SubtitleAdapter
import com.hungama.music.ui.main.viewmodel.MusicLanguageViewModel
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.audio_subtitle_select_bottom_sheet_layout.*
import java.util.*
import kotlin.collections.ArrayList

class AudioSubtitleSelectBottomSheetFragment(
    val videoListModel: PlayableContentModel?,
    val audioVideoListModel: ArrayList<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem>
) :
    SuperBottomSheetFragment() {
    var onSubTitleItemClick: OnAudioSubTitleItemClick? = null
    var musicLanguageListViewModel: MusicLanguageViewModel? = null
    var myLocale: Locale? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.audio_subtitle_select_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (videoListModel != null&&videoListModel?.data?.head?.headData?.misc?.sl?.subtitle!=null && !TextUtils.isEmpty(videoListModel?.data?.head?.headData?.misc?.sl?.subtitle?.link)) {
            val subtitleArrayList=ArrayList<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem>()
            subtitleArrayList.add(videoListModel?.data?.head?.headData?.misc?.sl?.subtitle!!)
            rvQuality.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = SubtitleAdapter(
                    context, subtitleArrayList,
                    object : SubtitleAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            setLog(
                                "TAG",
                                "SubtitleAdapter onUserClick:" + subtitleArrayList?.get(
                                    childPosition
                                )
                            )

                            if (onSubTitleItemClick != null) {
                                onSubTitleItemClick?.onAudioSubTitleItemClick(
                                    videoListModel!!,
                                    subtitleArrayList?.get(childPosition)!!
                                )
                            }
                            dismiss()
                        }


                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }else{
            rlSubtitleSetting?.visibility = View.GONE
            innerLine?.visibility = View.GONE
            //Toast.makeText(requireContext(), getString(R.string.video_player_str_23), Toast.LENGTH_LONG).show()
        }

        rvLanguage.apply {
            layoutManager =
                GridLayoutManager(context, 1)
            adapter = AudioSubtitleAdapter(
                context, audioVideoListModel,
                object : AudioSubtitleAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {
                        /*setLog(
                            "TAG",
                            "SubtitleAdapter onUserClick:" + audioVideoListModel.get(
                                childPosition
                            )
                        )

                        if (onSubTitleItemClick != null) {
                            onSubTitleItemClick?.onAudioSubTitleItemClick(
                                videoListModel!!,
                                videoListModel?.subtitle?.get(childPosition)!!
                            )
                        }*/
                        dismiss()
                    }


                })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }

    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.video_sheet_rounded_corner)

    override fun isSheetAlwaysExpanded(): Boolean  = true
    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_230).toInt()

    override fun getWidth(): Int = requireContext().resources.getDimension(R.dimen.dimen_375).toInt()

    //override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_160).toInt()

    override fun getBackgroundColor(): Int = requireContext().resources.getColor(R.color.transparent)

    fun addSubTitleListener(onSubTitleItemClick: OnAudioSubTitleItemClick){
        this.onSubTitleItemClick=onSubTitleItemClick
    }

    interface OnAudioSubTitleItemClick {
        fun onAudioSubTitleItemClick(model: PlayableContentModel?, subtitleItem: PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?)
    }

}

