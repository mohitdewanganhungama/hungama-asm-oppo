package com.hungama.music.ui.main.view.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.dialog_setting_delete.*


class DeleteAccountBottomsheet : SuperBottomSheetFragment(), View.OnClickListener {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_setting_delete, container, false)
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true

    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_500).toInt()

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
//        dialog?.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_500)
//        dialog?.behavior.isDraggable = false
//        dialog?.behavior
//        dialog?.setCancelable(false)
//        dialog?.setCanceledOnTouchOutside(true)
//        return dialog
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDelete.setOnClickListener(this)
        btnCancel.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v == btnDelete){
            deleteUserAccount()
        }else if (v == btnCancel){

            dismiss()
        }
    }

    fun deleteUserAccount() {
        val userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(context).isOnline) {

            userViewModel.deleteAccount(requireContext()).observe(this,
                Observer {

                    setLog("deleteAccount", "deleteUserAccount:${it}")
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it?.data?.code?.equals(""+WSConstants.STATUS_200)!!) {
                                SharedPrefHelper.getInstance().logOut()
                                movieToScreen()
                            }else{
                                if(requireView()!=null){
                                    val messageModel = MessageModel(it?.data?.message!!, MessageType.NEGATIVE, false)
                                    CommonUtils.showToast(requireContext(), messageModel)
                                }

                            }

                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {
                            val messageModel = MessageModel(it?.message!!, MessageType.NEGATIVE, false)
                            CommonUtils.showToast(requireContext(), messageModel)
                        }
                    }
                })
        }

    }

    private fun movieToScreen(){
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        dismiss()
    }
}