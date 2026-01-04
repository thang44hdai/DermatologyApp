package com.example.safeaid.core.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.dermatology.R

fun View.setOnDebounceClick(function: (View?) -> Unit) {
    setOnClickListener(object : DebounceOnClickListener() {
        override fun onClickImpl(v: View?) {
            function.invoke(v)
        }
    })
}

fun View.setOnDebounceClickWithDuration(function: (View?) -> Unit, duration: Long = 400) {
    setOnClickListener(object : DebounceOnClickListener(duration) {
        override fun onClickImpl(v: View?) {
            function.invoke(v)
        }
    })
}

fun View.hello() {
    println("hello world")
}

fun Context.showImageZoom(imageUrl: String?) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_image_zoom)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT
    )

    val imageView = dialog.findViewById<ImageView>(R.id.img_zoom)
    val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)

    Glide.with(this)
        .load(imageUrl)
        .into(imageView)

    btnClose.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}