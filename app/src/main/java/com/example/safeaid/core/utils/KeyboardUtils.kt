package com.example.safeaid.core.utils

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment

object KeyboardUtils {
    
    /**
     * Hide keyboard when touching outside EditText views
     */
    fun setupHideKeyboardOnTouchOutside(activity: Activity, rootView: View) {
        if (rootView !is EditText) {
            rootView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity)
                }
                false
            }
        }
        
        // If it's a ViewGroup, recursively set up for all children
        if (rootView is ViewGroup) {
            for (i in 0 until rootView.childCount) {
                val child = rootView.getChildAt(i)
                setupHideKeyboardOnTouchOutside(activity, child)
            }
        }
    }
    
    /**
     * Hide keyboard when touching outside EditText views for Fragment
     */
    fun setupHideKeyboardOnTouchOutside(fragment: Fragment, rootView: View) {
        fragment.activity?.let { activity ->
            setupHideKeyboardOnTouchOutside(activity, rootView)
        }
    }
    
    /**
     * Hide the soft keyboard
     */
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            currentFocus.clearFocus()
        }
    }
    
    /**
     * Hide the soft keyboard for Fragment
     */
    fun hideKeyboard(fragment: Fragment) {
        fragment.activity?.let { activity ->
            hideKeyboard(activity)
        }
    }
    
    /**
     * Show the soft keyboard for a specific view
     */
    fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    
    /**
     * Check if touch event is outside EditText bounds
     */
    fun isTouchOutsideEditText(event: MotionEvent, editText: EditText): Boolean {
        val location = IntArray(2)
        editText.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val width = editText.width
        val height = editText.height
        
        return event.rawX < x || event.rawX > x + width || 
               event.rawY < y || event.rawY > y + height
    }
}