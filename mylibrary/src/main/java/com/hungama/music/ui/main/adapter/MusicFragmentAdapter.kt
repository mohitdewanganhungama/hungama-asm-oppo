package com.hungama.music.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hungama.music.ui.main.view.fragment.UpNextMusicFragment
import com.hungama.music.ui.main.view.fragment.LyricsMusicFragment

class MusicFragmentAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {

    companion object{
        const val NUM_ITEMS = 1
    }

    override fun getItem(position: Int): Fragment {
        when(position){
            //0 -> return UpNextMusicFragment()
            0 -> return LyricsMusicFragment()
            //2 -> return UpNextMusicFragment()
            //3 -> return LyricsMusicFragment()
        }
        return UpNextMusicFragment()
    }

    override fun getCount(): Int {
        return  NUM_ITEMS
    }
}