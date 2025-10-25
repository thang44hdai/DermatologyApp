package com.example.safeaid.core.ui

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.dermatology.R
import com.example.safeaid.core.utils.setOnDebounceClick

class BaseDialog(val context: Context) {
    private val builder = AlertDialog.Builder(context)
    private val inflater = LayoutInflater.from(context)
    private val dialogView = inflater.inflate(R.layout.dialog_base, null)

    private val dialog: AlertDialog = builder.setView(dialogView).create()

    fun setView(
        title: String,
        message: String,
        onClickPositive: () -> Unit,
        onClickNegative: (() -> Unit)?
    ) {
        val dialogTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val negativeBtn = dialogView.findViewById<Button>(R.id.negative)
        val positiveBtn = dialogView.findViewById<Button>(R.id.positive)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogTitle.text = title
        dialogMessage.text = message

        negativeBtn.isVisible = onClickNegative != null

        negativeBtn.setOnDebounceClick {
            onClickNegative?.invoke()
            dialog.dismiss()
        }

        positiveBtn.setOnDebounceClick {
            onClickPositive()
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.show()
    }
}
