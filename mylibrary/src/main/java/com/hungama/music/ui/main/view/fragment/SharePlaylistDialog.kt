package com.hungama.music.ui.main.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import kotlinx.android.synthetic.main.share_playlist_dialog.*

class SharePlaylistDialog(val makePlaylistPublic: MakePlaylistPublic) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.share_playlist_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_200)
        dialog?.behavior?.isDraggable = true
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llNo?.setOnClickListener {
            dismiss()
        }

        llYes?.setOnClickListener {
            if (context != null){
                makePlaylistPublic?.onMakePlaylistPublic()
            }
            dismiss()
        }
    }

    interface MakePlaylistPublic{
        fun onMakePlaylistPublic()
    }

}