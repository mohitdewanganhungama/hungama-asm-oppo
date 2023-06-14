package com.hungama.music.ui.main.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.DetailPlaylistAdapter
import kotlinx.android.synthetic.main.dialog_delete_song_from_queue.*


class PlaylistDeleteBottomsheet(val listener : deleteFromPlaylist) : BottomSheetDialogFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_delete_song_from_queue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llNo.setOnClickListener {
            dismiss()
        }
        llYes.setOnClickListener {
            listener.deteleSong(true)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_224)
//        dialog?.behavior.isDraggable = false
//        dialog?.setCancelable(false)
//        dialog?.setCanceledOnTouchOutside(false)
        return dialog
    }

    interface deleteFromPlaylist{
        fun deteleSong(status:Boolean)
    }

}