package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.PurchasesEventsAdapter
import com.hungama.music.data.model.PurchasesEventsModel
import com.hungama.music.data.model.RentedMovieRespModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.fragment_ticket_details_under_purchases.*
import kotlinx.coroutines.launch


class TicketDetailsUnderPurchasesFragment : BaseFragment() {
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    private lateinit var eventPurchasesAdapter : PurchasesEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ticket_details_under_purchases, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)

//        setUpViewModel()
        rvEventPurchases.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)



        btnExplore.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            displayEmpty(true)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }



    private fun setUpViewModel() {
        userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)

        getRentedMovieList()
    }

    fun getRentedMovieList(){
        baseMainScope.launch {
            if (ConnectionUtil(requireContext()).isOnline) {
                userSubscriptionViewModel?.getRentedMovie(requireContext())?.observe(this@TicketDetailsUnderPurchasesFragment,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null && it?.data?.success!!) {
                                    setLog(TAG, "isViewLoading $it")
                                    fillUI(it?.data)
                                }else{
                                    Utils.showSnakbar(requireContext(),
                                        requireView(),
                                        false,
                                        it?.message!!
                                    )
                                }
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                            }
                        }
                    })
            }else {
                val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }

    private fun fillUI(data: RentedMovieRespModel) {
        if(data.data.movie.size>0){
            eventPurchasesAdapter = PurchasesEventsAdapter(requireContext(),object :PurchasesEventsAdapter.OnMovieItemClick{
                override fun onItemClick(childPosition: Int) {

                }

            })
            rvEventPurchases.adapter = eventPurchasesAdapter

            eventPurchasesAdapter.setEventList(data.data.movie)
            displayEmpty(false)
        }else{
            displayEmpty(true)
        }


    }

    fun displayEmpty(status:Boolean){
        if(status){
            clExplore.visibility = View.VISIBLE
            clEventPurchases.visibility = View.GONE
        }else{
            clExplore.visibility = View.GONE
            clEventPurchases.visibility = View.VISIBLE
        }
    }
}
