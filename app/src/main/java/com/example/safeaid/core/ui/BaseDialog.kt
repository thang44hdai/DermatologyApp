package com.example.safeaid.core.ui

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
        onClickNegative: (() -> Unit)? = null,
        positiveText: String = "OK",
        negativeText: String = "Hủy"
    ) {
        val dialogTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val negativeBtn = dialogView.findViewById<Button>(R.id.negative)
        val positiveBtn = dialogView.findViewById<Button>(R.id.positive)
        val singleBtn = dialogView.findViewById<Button>(R.id.btn_single)
        val buttonLayout = dialogView.findViewById<LinearLayout>(R.id.layout_button)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set content
        dialogTitle.text = title
        dialogMessage.text = message

        // Handle button layout
        if (onClickNegative != null) {
            // Two buttons layout
            buttonLayout.isVisible = true
            singleBtn.isVisible = false
            negativeBtn.isVisible = true
            
            negativeBtn.text = negativeText
            positiveBtn.text = positiveText
            
            negativeBtn.setOnDebounceClick {
                onClickNegative.invoke()
                dialog.dismiss()
            }
            
            positiveBtn.setOnDebounceClick {
                onClickPositive()
                dialog.dismiss()
            }
        } else {
            // Single button layout
            buttonLayout.isVisible = false
            singleBtn.isVisible = true
            
            singleBtn.text = positiveText
            
            singleBtn.setOnDebounceClick {
                onClickPositive()
                dialog.dismiss()
            }
        }
    }

    // Convenience method for simple dialogs
    fun setView(
        title: String,
        message: String,
        onClickPositive: () -> Unit,
        onClickNegative: (() -> Unit)?
    ) {
        setView(
            title = title,
            message = message,
            onClickPositive = onClickPositive,
            onClickNegative = onClickNegative,
            positiveText = "OK",
            negativeText = "Hủy"
        )
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}
