package com.example.safeaid.core.utils

import android.os.SystemClock
import android.view.View

abstract class DebounceOnClickListener(time: Long = 300) : Debounceable(time),
    View.OnClickListener {
    final override fun onClick(v: View?) {
        if (!debounce()) {
            onClickImpl(v)
        }
    }

    abstract fun onClickImpl(v: View?)
}

open class Debounceable(val debounceTime: Long) {
    private var lastClickedTimestamp: Long = 0

    protected fun debounce(): Boolean {
        val currentTimestamp = SystemClock.elapsedRealtime()
        val shouldDebounce = currentTimestamp - lastClickedTimestamp < debounceTime
        if (!shouldDebounce) {
            lastClickedTimestamp = currentTimestamp
        }
        return shouldDebounce
    }
}