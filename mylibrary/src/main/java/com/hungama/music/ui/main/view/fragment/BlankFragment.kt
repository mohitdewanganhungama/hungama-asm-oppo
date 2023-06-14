//package com.hungama.music.ui.main.view.fragment
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.Menu
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.hungama.music.R
//import com.hungama.music.ui.base.BaseActivity
//import com.hungama.music.ui.base.BaseFragment
//import com.hungama.music.utils.hide
//import com.hungama.music.utils.show
//import kotlinx.android.synthetic.main.fr_blank.*
//import kotlinx.android.synthetic.main.header_main.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//
///**
// * A simple [Fragment] subclass.
// * Use the [BlankFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class BlankFragment : BaseFragment() {
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fr_blank, container, false)
//    }
//
//    override fun initializeComponent(view: View) {
//        ivSearch?.setOnClickListener(this)
//        ivUserPersonalImage?.setOnClickListener(this)
//        baseMainScope.launch {
//            if (isAdded){
//                tvCommingSoon?.hide()
//                comingSoonShimmerLayout?.show()
//                comingSoonShimmerLayout?.startShimmer()
//                /*delay(2000)
//                comingSoonShimmerLayout?.stopShimmer()
//                tvCommingSoon?.show()
//                comingSoonShimmerLayout?.hide()*/
//            }
//        }
//    }
//
//    override fun onClick(v: View) {
//        super.onClick(v)
//        if (v == ivUserPersonalImage) {
//            addFragment(R.id.fl_container, this, ProfileFragment(), false)
//        }
//    }
//
//    override fun onHiddenChanged(hidden: Boolean) {
//        super.onHiddenChanged(hidden)
//        if(!hidden && activity!=null){
//            (activity as BaseActivity).showBottomNavigationBar()
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//       return false
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        return false
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        comingSoonShimmerLayout?.stopShimmer()
//    }
//}