package com.example.treasurehunter

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView

class DialogManager private constructor(){
    companion object {
        val instance : DialogManager by lazy { DialogManager() }
    }

    private var dialog: Dialog? = null

    fun showMessage(activity: Activity, message: String): TextView? {
        if (!activity.isDestroyed) {
            dialog?.dismiss()
            dialog = createDialog(activity)

            val view = View.inflate(activity, R.layout.dialog_message, null)
            val tv_message = view.findViewById<TextView>(R.id.tv_message)
            tv_message.text = message

            val btn_ok = view.findViewById<Button>(R.id.btn_ok)
            btn_ok.setOnClickListener { dialog?.dismiss() }
            dialog?.setContentView(view)
            return btn_ok
        }
        return null
    }

    fun showCorrectMsg(activity: Activity, message: String): TextView? {
        if (!activity.isDestroyed) {
            dialog?.dismiss()
            dialog = createDialog(activity)

            val btn_next = View.inflate(activity, R.layout.dialog_correct, null).run {
                dialog?.setContentView(this)
                findViewById<TextView>(R.id.tv_message).text = message

                findViewById<Button>(R.id.btn_next).apply {
                    setOnClickListener { dialog?.dismiss() }
                }
            }

            return btn_next
        }
        return null
    }

    fun showWrongMsg(activity: Activity): TextView? {
        if (!activity.isDestroyed) {
            dialog?.dismiss()
            dialog = createDialog(activity)

            val btn_again = View.inflate(activity, R.layout.dialog_wrong, null).run {
                dialog?.setContentView(this)

                findViewById<Button>(R.id.btn_again).apply {
                    setOnClickListener { dialog?.dismiss() }
                }
            }

            return btn_again
        }
        return null
    }

    private fun createDialog(activity: Activity): AlertDialog {
        return AlertDialog.Builder(activity).create().apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(false)
            show()
        }
    }

    fun cancelDialog() = dialog?.dismiss()
}