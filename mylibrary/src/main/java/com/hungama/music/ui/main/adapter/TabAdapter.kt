package com.hungama.music.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(activity: FragmentActivity, val tabCount: Int, val mFragments: ArrayList<Fragment>,
                 fragmentName: ArrayList<String>):
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return this.tabCount
    }

    override fun createFragment(position: Int): Fragment {
        return mFragments.get(position)
    }
}