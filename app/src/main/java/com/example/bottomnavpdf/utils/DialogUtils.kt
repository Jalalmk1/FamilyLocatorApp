package com.example.bottomnavpdf.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnDialogPermissionClickListener
import com.example.bottomnavpdf.databinding.DialogPermissionBinding

object DialogUtils {
    fun permissionDialog(
        context: Context,
        listener: OnDialogPermissionClickListener
    ) {
        val mDialog = Dialog(context)
        val dialogBinding: DialogPermissionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_permission, null, false
        )
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(dialogBinding.root)
        mDialog.setCanceledOnTouchOutside(false)
        mDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        mDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val mListener = listener

        dialogBinding.btnCancel.setOnClickListener {
            mDialog.dismiss()
            mListener.onDiscardClick()
        }

        dialogBinding.btnProceed.setOnClickListener {
            mDialog.dismiss()
            mListener.onProceedClick()
        }

        mDialog.show()

    }
}