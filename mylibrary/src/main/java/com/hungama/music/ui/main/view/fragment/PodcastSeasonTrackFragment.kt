package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SearchBucketAdapter
import com.hungama.music.data.model.SearchBucketModel
import kotlinx.android.synthetic.main.fr_search_bucket_data.*

class PodcastSeasonTrackFragment : BaseFragment(), SearchBucketAdapter.SearchResult {
    lateinit var searchAdapter : SearchBucketAdapter
    var searchDataList = mutableListOf<SearchBucketModel>()
    var mTrack=PlaylistModel.Data.Body.Row.Data.Misc.Track()

    companion object {
        fun newInstance(track: PlaylistModel.Data.Body.Row.Data.Misc.Track) : PodcastSeasonTrackFragment {
            val fragment = PodcastSeasonTrackFragment()
           var bundle=Bundle()
            bundle.putParcelable("TRACK",track)
            fragment.arguments=bundle
            return fragment
        }

    }

    override fun initializeComponent(view: View) {
        rvTab.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        searchAdapter = SearchBucketAdapter(requireContext(),this)
        rvTab.adapter = searchAdapter
        searchDataList.add(SearchBucketModel("Rehan Badshah’s Podcast","25 Episodes   Param Singh", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Badshah","45 Episodes   Katy Milkman", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Fakeera the Fakir Bhadshah","25 Episodes   Param Singh", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Be Badshah or King","15 Episodes   Katy Milkman", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("California Now live","15 Episodes   Maninder Buttar", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Top 40 Song","Guru Randhawa, Nora fate", R.drawable.bg_gradient_placeholder))
        searchAdapter.setSearchlist(searchDataList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_podcast, container, false)
    }

    override fun SearchItemClick() {

    }

}