package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.fragment_ticket_confirmation.*


/**
 * A simple [Fragment] subclass.
 * Use the [DynamicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TicketConfirmationFragment : BaseFragment() {
    var selectedContentId: String? = null
    var liveEventUrl:String = ""
    var screenMode:String = ""
    var artImageUrl:String = ""
    var name:String = ""
    var date:String = ""
    var ticketCost:String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ticket_confirmation, container, false)
    }

    override fun initializeComponent(view: View) {
        hideBottomNavigationAndMiniplayer()

        name = requireArguments().getString("name", "").toString()
        ticketCost = requireArguments().getString("ticketCost", "").toString()
        date = requireArguments().getString("date", "").toString()
        artImageUrl = requireArguments().getString("image", "").toString()
        selectedContentId = requireArguments().getString("id", "").toString()
        liveEventUrl = requireArguments().getString("liveEventUrl", "").toString()
        screenMode = requireArguments().getString("screenmode", "").toString()
        /*
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { view -> backPress() }
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back_right_arrow)
        */
        ivBack?.setOnClickListener { backPress() }
        tvActionBarHeading.text = getString(R.string.live_events_str_4)

        llShare?.setOnClickListener(this)
        ivShare?.setOnClickListener(this)
        clBooked?.setOnClickListener(this)


        val date = DateUtils.convertDate(
            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
            DateUtils.DATE_FORMAT_DD_MMMM_HH_MM,
            date
        )
        tvEventTime?.setText(""+date)
        tvEventAmount?.setText("₹ "+ticketCost)
        tvEventTitle?.setText(""+name)
        ImageLoader.loadImage(requireActivity(),ivEvent,artImageUrl,R.drawable.bg_gradient_placeholder)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
//        if (hidden){
//            showBottomNavigationAndMiniplayer()
//        }else{
        hideBottomNavigationAndMiniplayer()
//        }
    }

    override fun onDestroy() {
//        showBottomNavigationAndMiniplayer()
        super.onDestroy()
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v==clBooked){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llShare!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val fragment= EventPlayerFragment()
            val bundle=Bundle()
            bundle.putString("eventName",name)
            bundle.putString("id",selectedContentId)
            bundle.putString("liveEventUrl",liveEventUrl)
            bundle.putString("screenmode",screenMode)
            fragment.arguments=bundle
            addFragment(R.id.fl_container, this, fragment, false)
        }else if(v==ivShare || v==llShare){
            val shareurl=getString(R.string.music_player_str_18)+" "+ EventDetailFragment.liveEventDetailRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(),shareurl)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.event_detail_menu, menu)
        return onCreateOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.event_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_share_event ->{

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return onPrepareOptionsMenu(menu)
    }


}

