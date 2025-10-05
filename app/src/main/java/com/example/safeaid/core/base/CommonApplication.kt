package com.example.safeaid.core.base

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.annotation.CallSuper

abstract class CommonApplication : Application() {

    companion object {
        private lateinit var instance: CommonApplication

        fun getInstance() = instance
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun isDebugMode() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

}

fun appInstance() = CommonApplication.getInstance()