package com.hungama.music.utils.customview.stories

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hungama.music.data.model.BodyDataItem
import com.hungama.music.ui.main.view.fragment.StoryDisplayFragment

class StoryPagerAdapter(
    fragmentManager: FragmentManager, private val storyList: ArrayList<BodyDataItem>
    , private val updateStoryUserList: (userIndex: Int, viewIndex: Int) -> Unit,
    private val setVideoStoryType:(isVideoStory: Boolean, onResumeCalled: Boolean) -> Unit
)
    : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {

        return StoryDisplayFragment.newInstance(position,
            storyList[position].id.toString(), updateStoryUserList, setVideoStoryType)
    }

    override fun getCount(): Int {
        return storyList.size
    }

    fun findFragmentByPosition(viewPager: ViewPager, position: Int): Fragment? {
        try {
            val f = instantiateItem(viewPager, position)
            return f as? Fragment
        } finally {
            finishUpdate(viewPager)
        }
    }
}