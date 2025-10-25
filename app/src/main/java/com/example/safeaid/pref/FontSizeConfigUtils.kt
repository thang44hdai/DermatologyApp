package com.xronosinc.pref

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

object FontSizeConfigUtils {
    private const val CURRENT_SIZE: String = "CURRENT_SIZE"
    private var preference: SharedPreferences? = null

    fun saveFontSize(context: Context, size: Float) {
        if (preference == null)
            preference = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        preference?.run {
            edit().putFloat(CURRENT_SIZE, size).commit()
        }
    }

    fun getCurrentFontSize(context: Context): Float {
        if (preference == null)
            preference = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val systemScale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
        val enumStr = preference?.run {
            getFloat(CURRENT_SIZE, systemScale)
        } ?: systemScale
        return enumStr
    }

}