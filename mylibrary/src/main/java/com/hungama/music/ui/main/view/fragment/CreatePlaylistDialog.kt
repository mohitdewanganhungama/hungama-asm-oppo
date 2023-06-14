package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.CreatedPlaylistEvent
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.dialog_create_playlist.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreatePlaylistDialog constructor(private val playlistListener: createPlayListListener,
val defaultPlaylistName: String):BottomSheetDialogFragment() {

    var userViewModel: UserViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_create_playlist,container,false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etCreatePlaylist?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (TextUtils.isEmpty(etCreatePlaylist?.text)){
                    vBtnCreate?.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius_18_bg_alpha_white)
                }else{
                    //vBtnCreate?.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius_18_bg_blue)
                    applyButtonTheme(requireContext(), vBtnCreate)
                }
            }

        })
        etCreatePlaylist?.setText(defaultPlaylistName)
        etCreatePlaylist.isEnabled = true
        etCreatePlaylist.requestFocus()
        etCreatePlaylist.setSelection(etCreatePlaylist.text.toString().length)
        /*dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.parent.parent.requestLayout()
            }
        }*/
        setupUserViewModel()
        vBtnCreate.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), vBtnCreate!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if(isValidForm()){
                callCreatePlaylist()

            }
        }
    }

    private fun callCreatePlaylist() {
        try {
            val mainJson = JSONObject()

            mainJson.put("name", etCreatePlaylist?.text?.trim()?.toString())
            mainJson.put("public", switchicon?.isChecked == true)

            userViewModel?.createPlayList(requireContext(), mainJson)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null && !TextUtils.isEmpty(it?.data?.data?.id)) {

                                setLog("TAG", "callCreatePlaylist: it?.data:${it?.data}")

                                dismiss()

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String,String>()
                                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,"Audio")
                                    hashMap.put(EventConstant.PLAYLISTNAME_EPROPERTY,it.data.data.title)
                                    if(MainActivity.lastBottomItemPosClicked==4){
                                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,"Library")
                                    }else {
                                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,"Player_3 Dot Menu")
                                    }

                                    EventManager.getInstance().sendEvent(CreatedPlaylistEvent(hashMap))
                                }


                                if(playlistListener!=null){
                                    playlistListener?.playListCreatedSuccessfull(it.data.data.id)
                                }
                            }else if (!TextUtils.isEmpty(it?.data?.message)){
                                val messageModel = MessageModel(it?.data?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(requireContext(), messageModel)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(requireContext(),
                requireView(),
                false,
                getString(R.string.discover_str_2)
            )
        }
    }

    private fun isValidForm(): Boolean {
        if(TextUtils.isEmpty(etCreatePlaylist?.text?.trim()?.toString())){
            etCreatePlaylist?.requestFocus()
            val messageModel = MessageModel("Enter Playlist Name", MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
            return false
        }
        return true
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_350)
        dialog.behavior.isDraggable = false
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
    }


    private fun setProgressBarVisible(it: Boolean) {
        if(it){
            progressBar?.rootView?.visibility=View.VISIBLE
        }else{
            progressBar?.rootView?.visibility=View.GONE
        }
    }

    interface createPlayListListener{
        fun playListCreatedSuccessfull(id:String)
    }
}