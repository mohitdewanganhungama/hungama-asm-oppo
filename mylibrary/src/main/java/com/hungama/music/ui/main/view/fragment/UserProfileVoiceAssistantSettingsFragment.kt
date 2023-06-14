package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.hungama.music.R
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_voice_assistant_settings.*
import android.widget.Toast

import android.widget.CompoundButton
import com.hungama.music.utils.CommonUtils.setLog


class UserProfileVoiceAssistantSettingsFragment : BaseFragment(),
    BaseActivity.OnLocalBroadcastEventCallBack {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_voice_assistant_settings, container, false)
    }

    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.general_setting_str_38)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "Profile", "Settings_general_voice assistants & apps",
            "")
        loadData()
        rlAmazonAlexa.setOnClickListener {
            if (hidden_chat_view.getVisibility() === View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    rlAmazonAlexa,
                    AutoTransition()
                )
                hidden_chat_view.setVisibility(View.GONE)
            } else {
                TransitionManager.beginDelayedTransition(
                    rlAmazonAlexa,
                    AutoTransition()
                )
                hidden_chat_view.setVisibility(View.VISIBLE)
            }
        }
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        svAmazonAlexa.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                editor.putBoolean("svAmazonAlexa", true).commit()
            } else {
                editor.putBoolean("svAmazonAlexa", false).commit()
            }
        })
        svHungamaDefault.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                editor.putBoolean("svHungamaDefault", true).commit()
            } else {
                editor.putBoolean("svHungamaDefault", false).commit()
            }
        })

    }

    fun loadData(){
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val amazonAlexaBoolean : Boolean = sharedPref.getBoolean("svAmazonAlexa",false)
        val hungamaDefaultBoolean : Boolean = sharedPref.getBoolean("svHungamaDefault",false)
        setLog(TAG, "loadData: saveBoolean "+amazonAlexaBoolean)
        svAmazonAlexa.isChecked = amazonAlexaBoolean
        svHungamaDefault.isChecked = hungamaDefaultBoolean
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                /*CommonUtils.setPageBottomSpacing(, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)*/
            }
        }
    }
}