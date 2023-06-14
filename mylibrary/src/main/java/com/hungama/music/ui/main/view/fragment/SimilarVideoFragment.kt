package com.hungama.music.ui.main.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.data.model.*
import com.hungama.music.R
import com.hungama.music.ui.main.adapter.VideoSimilarAdapter
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.fragment_similar_video.*


class SimilarVideoFragment(videoDetailRespModel: PlaylistDynamicModel?, val onSimilarVideoClick: OnSimilarVideoClick?) : BottomSheetDialogFragment() {

    var selectedContentId = ""
    var videoDetailModel = videoDetailRespModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_similar_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLog("TAG", "onViewCreated: videoDetailRespModel " + videoDetailModel)
        setSimilarVideoData()
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_310)
        dialog.behavior.isDraggable = false
        return dialog
    }

    private fun setSimilarVideoData() {
        if (videoDetailModel?.data?.body != null && !videoDetailModel?.data?.body?.recomendation.isNullOrEmpty()) {
            var similarVideoIndex = -1
            videoDetailModel?.data?.body?.recomendation?.forEachIndexed { index, rowsItem ->
                if (rowsItem != null && !rowsItem.keywords.isNullOrEmpty()){
                    if (rowsItem.keywords?.get(0).equals("similar-videos")){
                        similarVideoIndex = index
                    }
                }
            }
            if (similarVideoIndex >= 0){
                rvSimilarMusicVideos.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = VideoSimilarAdapter(context,
                        videoDetailModel?.data?.body?.recomendation?.get(similarVideoIndex)?.items,
                        object : VideoSimilarAdapter.OnItemClick {
                            override fun onUserClick(childPosition: Int) {
                                /*val bundle = Bundle()
                            bundle.putString(
                                "id",
                                videoDetailModel.data.body.similar.get(childPosition).data.id
                            )
                            val videoDetailsFragment = MusicVideoDetailsFragment()
                            videoDetailsFragment.arguments = bundle
                            refreshCurrentFragment(requireActivity(), videoDetailsFragment)*/
//                            playNextVideo(videoDetailModel.data.body.similar.get(childPosition).data.id)
                                if (onSimilarVideoClick != null){
                                    dismiss()
                                    onSimilarVideoClick.onSimilarVideoClick(videoDetailModel?.data?.body?.recomendation?.get(similarVideoIndex)?.items?.get(childPosition))
                                }
                            }
                        })
                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                    setHasFixedSize(true)
                }
            }else{
                dismiss()
                val messageModel = MessageModel(getString(R.string.similar_video_not_found),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }else{
            dismiss()
            val messageModel = MessageModel(getString(R.string.similar_video_not_found),
                MessageType.NEUTRAL, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    interface OnSimilarVideoClick{
        fun onSimilarVideoClick(contentData: BodyRowsItemsItem?)
    }
}