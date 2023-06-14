package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SearchBucketAdapter
import com.hungama.music.data.model.SearchBucketModel
import kotlinx.android.synthetic.main.fr_search_bucket_data.*

class SearchShortVideoFragment : BaseFragment() , SearchBucketAdapter.SearchResult{
    lateinit var searchAdapter : SearchBucketAdapter
    var searchDataList = mutableListOf<SearchBucketModel>()
    override fun initializeComponent(view: View) {
        rvTab.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        searchAdapter = SearchBucketAdapter(requireContext(),this)
        rvTab.adapter = searchAdapter
        searchDataList.add(SearchBucketModel("Udaariyan","Punjabi", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Paani Paani","Hindi", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Baawla","Tamil", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Paagal Badshah","Hindi", R.drawable.bg_gradient_placeholder))
        searchDataList.add(SearchBucketModel("Best Of Badshah","English", R.drawable.bg_gradient_placeholder))
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
        return inflater.inflate(R.layout.fr_short_video, container, false)
    }

    override fun SearchItemClick() {

    }


}