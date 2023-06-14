package com.hungama.music.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.main.view.fragment.SwipablePlayerViewFragment
import com.hungama.music.utils.CommonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SwipablePlayerPagerAdapter(fragment: Fragment, val dataList: ArrayList<Track> = ArrayList()) : FragmentStateAdapter(fragment) {
    var dataList2 = dataList
    override fun getItemCount(): Int {
        /*CommonUtils.setLog(
            "SwipablePlayerFragment",
            "SwipablePlayerPagerAdapter-getItemCount()-dataList.size=${dataList.size}"
        )*/
        return dataList2.size
    }

    override fun createFragment(position: Int): Fragment {
        /*CommonUtils.setLog(
            "SwipablePlayerFragment",
            "SwipablePlayerPagerAdapter-createFragment()-track.contentType=${dataList[position]?.contentType}"
        )*/
        return SwipablePlayerViewFragment.newInstance(dataList2[position], position)
    }

    fun updateDataList(dataList: ArrayList<Track>){
        CoroutineScope(Dispatchers.Main).launch{
            if (!dataList.isNullOrEmpty()){
                dataList2 = dataList
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        if(dataList2!=null && position<=dataList?.size!!) {
            return dataList2.get(position).hashCode().toLong()
        }else{
            return -1
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        try {
            if (!dataList2.isNullOrEmpty()){
                return dataList2.find { it.hashCode().toLong() == itemId } != null
            }
        }catch (e:Exception){

        }

        return false
    }
}