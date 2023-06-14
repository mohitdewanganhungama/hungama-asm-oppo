package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.hungama.music.R
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.fr_library_blank.*
import kotlinx.android.synthetic.main.fr_library_blank.btnExplore
import kotlinx.android.synthetic.main.fr_video_watchlist.*
import kotlinx.android.synthetic.main.header_main.*


/**
 * A simple [Fragment] subclass.
 * Use the [LibraryBlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LibraryBlankFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_library_blank, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)

        btnExplore?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val bundle = Bundle()
            bundle.putBoolean(Constant.isTabSelection, true)
            bundle.putString(Constant.tabName, "rent")
            (activity as MainActivity).applyScreen(2,bundle)
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && activity!=null){
            (activity as BaseActivity).showBottomNavigationBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }
}