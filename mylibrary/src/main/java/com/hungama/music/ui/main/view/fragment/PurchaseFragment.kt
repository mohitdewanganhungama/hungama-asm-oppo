package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.media3.common.util.Util
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.TabAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.data.model.OnParentItemClickListener
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.data.model.RowsItem
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.fragment_purchase.*
import java.io.IOException
import java.net.URL

class PurchaseFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View, TabLayout.OnTabSelectedListener {
    var artImageUrl:String? = null
    var selectedContentId:String? = null
    var playerType:String? = null

    var tabAdapter: TabAdapter? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    private lateinit var tracksViewModel: TracksContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_purchase, container, false)
    }
    override fun initializeComponent(view: View) {
        setData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    fun URL.toBitmap(): Bitmap?{
        return try {
            BitmapFactory.decodeStream(openStream())
        }catch (e: IOException){
            null
        }
    }


    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {

    }

    override fun onDestroy() {
        super.onDestroy()
        //tracksViewModel.onCleanup()
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setTabData(seasonsModel: PlaylistModel.Data.Body.Row?) {
        if (seasonsModel != null && seasonsModel.seasons != null && seasonsModel.seasons?.size!! > 0) {

            seasonsModel?.seasons?.forEachIndexed { index, season ->
                fragmentList.add(
                    TVShowFragment.addfrag(
                        index,
                        seasonsModel?.seasons,
                        seasonsModel?.seasons?.get(index),
                        null
                    )
                )
                val pos=index+1
                fragmentName.add("Season " + pos)
                tabLayout.addTab(tabLayout.newTab().setText("Season " + pos))
            }

            viewPagerSetUp()
            tabView.visibility = View.VISIBLE
        } else {
            tabView.visibility = View.GONE
        }
    }

    private fun setData() {
        fragmentList.add(EventFragment(1))
        fragmentName.add("Events")
        tabLayout.addTab(tabLayout.newTab().setText("Events"))
        fragmentList.add(EventFragment(2))
        fragmentName.add("Rented Movies")
        tabLayout.addTab(tabLayout.newTab().setText("Rented Movies"))

        viewPagerSetUp()
    }

    private fun viewPagerSetUp() {
        tabAdapter = TabAdapter(requireActivity(), tabLayout.tabCount, fragmentList, fragmentName)
        viewPager.adapter = tabAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = fragmentName.get(position)
        }.attach()
        viewPager.offscreenPageLimit = 1
        tabLayout.addOnTabSelectedListener(this)
        onTabSelected(tabLayout.getTabAt(0))
//        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
        viewPager.isUserInputEnabled = false
        //viewPager.addOnPageChangeListener(this)


    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text
        )
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_medium
        )
        tab?.let {
            setStyleForTab(it, Typeface.NORMAL, typeface)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text
        )
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }
    }

    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        tab.view.children.find { it is TextView }?.let { tv ->
            (tv as TextView).post {
                tv.setTypeface(typeface, style)
            }
        }
    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setLog("onPageSelected", "Selected position:" + position)
        }
    }
}