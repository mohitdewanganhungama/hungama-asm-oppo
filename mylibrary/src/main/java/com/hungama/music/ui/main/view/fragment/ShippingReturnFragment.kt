package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.R
import kotlinx.android.synthetic.main.fragment_shipping_return.*

class ShippingReturnFragment() : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shipping_return, container, false)
    }

    override fun initializeComponent(view: View) {
        tvDetail01?.text = getString(R.string.earn_coin_how_to_use)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

}