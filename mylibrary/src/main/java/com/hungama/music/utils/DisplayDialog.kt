package com.hungama.music.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.hungama.music.R

class DisplayDialog {
    companion object {
        private var ourInstance: DisplayDialog? = null
        var mDialog: Dialog? = null
        var mContext: Context?=null
        fun getInstance(mContext: Context): DisplayDialog {
            this.mContext=mContext
            if (ourInstance == null) {
                ourInstance = DisplayDialog()
            }
            return ourInstance!!
        }

    }

    fun showProgressDialog(mContext: Context, isCancelable: Boolean) {
        DisplayDialog.mContext=mContext
        if (DisplayDialog.mContext != null) {
//            if (mDialog == null) {
                mDialog = Dialog(DisplayDialog.mContext!!)
                val inflate = LayoutInflater.from(mContext).inflate(R.layout.layout_progress, null)
                mDialog!!.setContentView(inflate)
                mDialog!!.setCancelable(true)
                mDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog!!.setCancelable(isCancelable)
                mDialog!!.setCanceledOnTouchOutside(true)
//            }
            if (mDialog != null) {
                try {
                    mDialog?.show()
                }catch (e:Exception){

                }
            }
        }

    }

    fun dismissProgressDialog() {
        if (mDialog != null && mDialog?.isShowing!!) {
            try {
                mDialog?.cancel()
            }catch (e:Exception){

            }
        }
    }
}