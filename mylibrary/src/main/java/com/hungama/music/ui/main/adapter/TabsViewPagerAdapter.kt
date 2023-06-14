package com.hungama.music.ui.main.adapter

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter

// This "ViewPagerAdapter" class overrides functions which are
// necessary to get information about which item is selected
// by user, what is title for selected item and so on.*/
class TabsViewPagerAdapter// this is a secondary constructor of ViewPagerAdapter class.
public constructor(supportFragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(supportFragmentManager) {

    // objects of arraylist. One is of Fragment type and
    // another one is of String type.*/
    private final var fragmentList1: ArrayList<Fragment> = ArrayList()
    private final var fragmentTitleList1: ArrayList<String> = ArrayList()

    // returns which item is selected from arraylist of fragments.
    override fun getItem(position: Int): Fragment {
        return fragmentList1.get(position)
    }

    // returns which item is selected from arraylist of titles.
    @Nullable
    override fun getPageTitle(position: Int): CharSequence {
        return fragmentTitleList1.get(position)
    }

    // returns the number of items present in arraylist.
    override fun getCount(): Int {
        return fragmentList1.size
    }

    // this function adds the fragment and title in 2 separate  arraylist.
    fun addFragment(fragment: ArrayList<Fragment>, title: ArrayList<String>) {
        fragmentList1.addAll(fragment)
        fragmentTitleList1.addAll(title)
    }

    override fun saveState(): Parcelable? {
        val bundle: Bundle? = super.saveState() as Bundle?
        bundle?.putParcelableArray(
            "states",
            null
        ) // Never maintain any states from the base class, just null it out

        return bundle
    }
}