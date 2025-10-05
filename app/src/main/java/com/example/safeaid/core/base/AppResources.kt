package com.example.safeaid.core.base

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.google.android.material.color.MaterialColors
import java.util.*


@Suppress("unused")
object AppResources {

    fun getResources(): Resources {
        return appInstance().resources
    }

    fun getString(@StringRes stringResId: Int): String {
        return appInstance().getString(stringResId)
    }

    fun getString(@StringRes stringResId: Int, vararg formatArgs: Any?): String {
        return appInstance().getString(stringResId, *formatArgs)
    }

    fun getQuantityString(@PluralsRes stringResId: Int, quantity: Int): String {
        return getResources().getQuantityString(stringResId, quantity)
    }

    fun getQuantityString(
        @PluralsRes stringResId: Int, quantity: Int,
        vararg formatArgs: Any?
    ): String {
        return getResources().getQuantityString(stringResId, quantity, *formatArgs)
    }

    @ColorInt
    fun getColor(@ColorRes colorResId: Int): Int {
        return ResourcesCompat.getColor(getResources(), colorResId, appInstance().theme)
    }

    fun getColorStateList(@ColorRes colorResId: Int): ColorStateList? {
        return ResourcesCompat.getColorStateList(getResources(), colorResId, appInstance().theme)
    }

    @ColorInt
    fun getThemeColor(
        context: Context = appInstance(),
        @AttrRes colorAttrResId: Int,
        @ColorInt fallbackColor: Int = Color.TRANSPARENT
    ): Int {
        return MaterialColors.getColor(context, colorAttrResId, fallbackColor)
    }

    @ColorInt
    fun getThemeColorFallbackResource(
        @AttrRes colorAttrResId: Int,
        @ColorRes fallbackColorResId: Int
    ): Int {
        return MaterialColors.getColor(appInstance(), colorAttrResId, getColor(fallbackColorResId))
    }

    @ColorInt
    fun getColor(
        typedArray: TypedArray,
        @StyleableRes attrResId: Int,
        @ColorInt defaultColorInt: Int
    ): Int {
        return typedArray.getColor(
            attrResId,
            defaultColorInt
        )
    }

    fun getColorAsHexString(@ColorRes colorResId: Int): String {
        val color = getColor(colorResId)
        return String.format("#%06X", 0xFFFFFF and color)
    }

    fun getDimensionPixelSize(@DimenRes dimesResId: Int): Int {
        return getResources().getDimensionPixelSize(dimesResId)
    }

    fun getStringArray(@ArrayRes arrayResId: Int): Array<String?> {
        return getResources().getStringArray(arrayResId)
    }

    fun getDrawable(@DrawableRes drawableResId: Int): Drawable? {
        return ResourcesCompat.getDrawable(
            getResources(),
            drawableResId,
            appInstance().theme
        )
    }

    fun setBackgroundTint(view: View, @ColorRes color: Int) {
        ViewCompat.setBackgroundTintList(
            view,
            ContextCompat.getColorStateList(view.context, color)
        )
    }

    fun getDimensionPixelSize(
        typedArray: TypedArray,
        @StyleableRes attrResId: Int,
        @DimenRes defaultDimenResId: Int
    ): Int {
        return typedArray.getDimensionPixelSize(
            attrResId,
            getResources().getDimensionPixelSize(defaultDimenResId)
        )
    }

    fun getFont(@FontRes fontId: Int): Typeface? {
        return ResourcesCompat.getFont(appInstance(), fontId)
    }
}

fun Context.updateLocale(locale: Locale): Context {
    Locale.setDefault(locale)
    val resources = resources
    val config: Configuration = resources.configuration
    config.setLocale(locale)
    return createConfigurationContext(config)
}