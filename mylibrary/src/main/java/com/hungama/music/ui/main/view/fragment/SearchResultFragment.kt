package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.adapter.RadioAdapter
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import kotlinx.android.synthetic.main.fr_search_result.*


class SearchResultFragment : BaseFragment() {

    var userViewModel: UserViewModel? = null
    var userAlbumsList: ArrayList<BookmarkDataModel.Data.Body.Row> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_search_result, container, false)
    }

    override fun onResume() {
        super.onResume()
        setUpViewModel()
    }

    override fun initializeComponent(view: View) {
    }

    override fun onClick(v: View) {
        super.onClick(v)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        //getData()
    }



    private fun getData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getFollwingWithFilter(
                requireContext(),
                Constant.MODULE_FOLLOW,
                "0"
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setUpLists(it?.data)
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun setUpLists(userPlaylistData: BookmarkDataModel) {
        if (userPlaylistData?.data != null && userPlaylistData?.data?.body?.rows?.size!! > 0) {
            userAlbumsList = userPlaylistData?.data?.body?.rows!!

            rvSearchResultList.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = RadioAdapter(
                    context, userAlbumsList,
                    object : RadioAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {

                            val bundle = Bundle()
                            bundle.putString("id",""+userAlbumsList.get(childPosition)?.data?.id)
                            bundle.putString("image", ""+userAlbumsList.get(childPosition)?.data?.image)
                            bundle.putString("playerType", ""+userAlbumsList.get(childPosition)?.data?.type)
                            bundle.putBoolean("varient", false)
                            val albumDetailFragment = ArtistDetailsFragment()
                            albumDetailFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@SearchResultFragment,
                                albumDetailFragment,
                                false
                            )

                        }
                    }
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            rvSearchResultList.visibility = View.VISIBLE
        } else {
            rvSearchResultList.visibility = View.GONE
        }

    }

}