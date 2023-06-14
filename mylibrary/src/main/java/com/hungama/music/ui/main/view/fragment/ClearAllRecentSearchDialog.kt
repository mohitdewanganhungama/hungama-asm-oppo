package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.RecentSearchClearTappedEvent
import com.hungama.music.utils.CommonUtils
import com.hungama.music.R
import kotlinx.android.synthetic.main.dialog_clear_search.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class ClearAllRecentSearchDialog(
    val onClearRecentSearch: OnClearRecentSearch,
    val recentCountSize: Int
) : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_clear_search, container, false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_200)
        dialog.behavior.isDraggable = true
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llCancel.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), llCancel!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.RECENT_SEARCH_COUNT_EPROPERTY, "" + recentCountSize!!)
                    hashMap.put(EventConstant.SUCCESS_EPROPERTY, "No")
                    EventManager.getInstance().sendEvent(RecentSearchClearTappedEvent(hashMap))
                }


            } catch (e: Exception) {

            }
            dismiss()
        }
        llDelete.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), llDelete!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.RECENT_SEARCH_COUNT_EPROPERTY, "" + recentCountSize!!)
                    hashMap.put(EventConstant.SUCCESS_EPROPERTY, "Yes")
                    EventManager.getInstance().sendEvent(RecentSearchClearTappedEvent(hashMap))
                }


            } catch (e: Exception) {

            }

            if (onClearRecentSearch != null) {
                onClearRecentSearch.onClearRecentSearch(true)
            }
            dismiss()
        }
    }

    interface OnClearRecentSearch {
        fun onClearRecentSearch(isClear: Boolean)
    }
}