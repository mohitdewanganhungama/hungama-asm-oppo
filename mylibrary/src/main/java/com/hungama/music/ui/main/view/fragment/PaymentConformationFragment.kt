package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.PaymentConformationAdapter
import com.hungama.music.data.model.PaymentConformationModel
import kotlinx.android.synthetic.main.fragment_payment_conformation.*

class PaymentConformationFragment : BaseFragment() {
    lateinit var conformationadapter : PaymentConformationAdapter
    var offerList = mutableListOf<PaymentConformationModel>()
    override fun initializeComponent(view: View) {
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        rvOfferForYou.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        conformationadapter = PaymentConformationAdapter(requireContext())
        rvOfferForYou.adapter = conformationadapter
        offerList.add(PaymentConformationModel(R.drawable.bg_gradient_placeholder,"Refar a Friend"))
        offerList.add(PaymentConformationModel(R.drawable.bg_gradient_placeholder,"Win Gift Vouc...."))
        offerList.add(PaymentConformationModel(R.drawable.bg_gradient_placeholder,"Win cashback"))
        conformationadapter.setOfferList(offerList)
        btnStartExploring.setOnClickListener {
            addFragment(R.id.fl_container,this, PaymentFailFragment(),false)
        }
        vButtonMore.setOnClickListener {
            clViewMoreCard.visibility = View.GONE
            clViewLessCard.visibility = View.VISIBLE
            tvMyOrderTitle.visibility = View.VISIBLE
        }
        vButton.setOnClickListener {
            clViewLessCard.visibility = View.GONE
            clViewMoreCard.visibility = View.VISIBLE
            tvMyOrderTitle.visibility = View.GONE
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
        return inflater.inflate(R.layout.fragment_payment_conformation, container, false)
    }
}