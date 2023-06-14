package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.PurchaseTicketAdapter
import kotlinx.android.synthetic.main.fragment_event.*

class EventFragment(val tabNo:Int) : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun initializeComponent(view: View) {
        if (tabNo == 1){
            rlNoHistory.visibility = View.GONE
            rlPurchaseHistory.visibility = View.VISIBLE
            setupTicket()
        }else{
            rlPurchaseHistory.visibility = View.GONE
            rlNoHistory.visibility = View.VISIBLE
        }
    }

    private fun setupTicket() {
        rvPurchaseHistory.apply {
            layoutManager =
                GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
            adapter = PurchaseTicketAdapter(context, ArrayList(),
                object : PurchaseTicketAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {

                    }
                })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

}

