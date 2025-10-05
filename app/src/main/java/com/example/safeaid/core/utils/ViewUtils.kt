package com.example.safeaid.core.utils

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.animation.doOnEnd
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import kotlin.math.max

class ViewUtils {
    companion object {

        fun doOnGlobalLayout(
            view: View?,
            onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener?
        ) {
            if (view != null && onGlobalLayoutListener != null) {
                val viewTreeObserver = view.viewTreeObserver
                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (view.measuredWidth > 0 && view.measuredHeight > 0) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                } else {
                                    view.viewTreeObserver.removeGlobalOnLayoutListener(this)
                                }
                                onGlobalLayoutListener.onGlobalLayout()
                            }
                        }
                    })
                }
            }
        }

        @JvmStatic
        fun showKeyboard(context: Context, view: View) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.apply {
                showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun hideKeyboardFrom(context: Context, view: View) {
            val imm =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getStatusBarHeight(context: Context): Int {
            var result = 0
            val resourceId =
                context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

        fun getNavigationBarHeight(context: Context): Int {
            val resources: Resources = context.resources
            val resourceId: Int =
                resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }

        fun hasNavBar(context: Context): Boolean {
            val hasMenuKey: Boolean = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)

            if (!hasMenuKey && !hasBackKey) {
                return true
            }
            return false
        }

        fun getScreenHeight(context: Activity): Int {
            val display: Display = context.windowManager.getDefaultDisplay()
            var realHeight: Int

            if (Build.VERSION.SDK_INT >= 17) {
                //new pleasant way to get real metrics
                val realMetrics = DisplayMetrics()
                display.getRealMetrics(realMetrics)
                realHeight = realMetrics.heightPixels
            } else if (Build.VERSION.SDK_INT >= 14) {
                //reflection for this weird in-between time
                try {
                    val mGetRawH: Method = Display::class.java.getMethod("getRawHeight")
                    val mGetRawW: Method = Display::class.java.getMethod("getRawWidth")
                    realHeight = mGetRawH.invoke(display) as Int
                } catch (e: Exception) {
                    //this may not be 100% accurate, but it's all we've got
                    realHeight = display.height
                }
            } else {
                //This should be close, as lower API devices should not have window navigation bars
                realHeight = display.height
            }
            return realHeight
        }

        fun getActionBarSize(context: Context): Int {
            val value = TypedValue()
            context.theme.resolveAttribute(R.attr.actionBarSize, value, true)
            val data = TypedValue.complexToDimensionPixelSize(
                value.data, context
                    .resources.displayMetrics
            )

            return data
        }

        fun TextView.afterTextChanged(afterTextChanged: (String) -> Unit) {
            this.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    afterTextChanged.invoke(editable.toString())
                }
            })
        }

        fun runRippleEffectOn(view: View) {
            val viewWeak = WeakReference(view)
            val cx: Int = view.width / 2
            val cy: Int = view.height / 2
            viewWeak.get()?.let {
                it.visibility = View.VISIBLE
                val finalRadius: Int = max(view.width, view.height)
                val anim =
                    ViewAnimationUtils.createCircularReveal(it, cx, cy, 0f, finalRadius.toFloat())


                //Interpolator for giving effect to animation
                anim.interpolator = AccelerateInterpolator()
                // Duration of the animation
                // Duration of the animation
                anim.duration = 200
                anim.start()
                anim.doOnEnd {
                    viewWeak.get()?.visibility = View.INVISIBLE
                }
            }
        }
    }
}

object Utils {

    fun <T> listToJsonString(list: List<T>?): String {
        if (!list.isNullOrEmpty()) {
            val gson = Gson()
            return gson.toJson(list)
        }
        return ""
    }

    inline fun <reified T> String?.fromJsonToList(): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        return Gson().fromJson(this, type)  ?: arrayListOf()
    }


    inline fun TextView.setFontWeight(fontWeight: Int) {
        val currentTypeface = this.typeface

        val newTypeface = Typeface.create(currentTypeface, fontWeight)

        this.typeface = newTypeface

    }
}