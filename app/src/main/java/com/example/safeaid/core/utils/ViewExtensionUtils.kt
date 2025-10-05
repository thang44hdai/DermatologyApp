package com.example.safeaid.core.utils

import android.view.View

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