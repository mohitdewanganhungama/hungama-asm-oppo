package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * A neat trick to avoid TransactionTooLargeException while saving our instance state
 */
class SavedInstanceFragment : Fragment() {
    private var mInstanceBundle: Bundle? = null
    fun pushData(instanceState: Bundle?): SavedInstanceFragment {
        if (mInstanceBundle == null) {
            mInstanceBundle = instanceState
        } else {
            mInstanceBundle?.putAll(instanceState)
        }
        return this
    }

    fun popData(): Bundle? {
        val out: Bundle? = mInstanceBundle
        mInstanceBundle = null
        return out
    }

    companion object {
        private const val TAG = "SavedInstanceFragment"
        fun getInstance(fragmentManager: FragmentManager): SavedInstanceFragment {
            var out = fragmentManager.findFragmentByTag(TAG) as SavedInstanceFragment?
            if (out == null) {
                out = SavedInstanceFragment()
                fragmentManager.beginTransaction().add(out, TAG).commit()
            }
            return out
        }
    }

    init { // This will only be called once be cause of setRetainInstance()
        setRetainInstance(true)
    }
}