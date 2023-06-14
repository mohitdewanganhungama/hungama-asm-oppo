package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_payment_conformation.*

class PaymentFailFragment : BaseFragment() {
    override fun initializeComponent(view: View) {
        btnStartExploring.setOnClickListener {
            addFragment(R.id.fl_container,this, TicketConformationFragment(),false)
        }
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
        return inflater.inflate(R.layout.fragment_payment_fail, container, false)
    }

}