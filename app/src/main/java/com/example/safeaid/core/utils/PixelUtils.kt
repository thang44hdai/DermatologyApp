package com.example.safeaid.core.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log

class PixelUtils {
    companion object {
        fun dpToPx(context: Context, dp: Int): Int {
            return Math.round(
                dp * getPixelScaleFactor(
                    context
                )
            )
        }

        fun pxToDp(context: Context, px: Int): Int {
            return Math.round(
                px / getPixelScaleFactor(
                    context
                )
            )
        }

        private fun getPixelScaleFactor(context: Context): Float {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT
        }
    }
}

fun String.formatPrice(): String {
    return try {
        val cleanString = this.substring(0, this.length - 2)

        // Nếu rỗng hoặc không phải số → trả về 0 VND
        if (cleanString.isEmpty()) return "0 VND"

        val price = cleanString.toLong()
        val s = price.toString()

        // Xây dựng chuỗi từ phải sang trái
        val result = StringBuilder()
        for (i in s.indices.reversed()) {
            result.append(s[i])
            // Mỗi 3 ký tự (tính từ phải) và chưa phải vị trí đầu tiên thì thêm dấu chấm
            if ((s.length - i) % 3 == 0 && i > 0) {
                result.append('.')
            }
        }

        // Đảo ngược lại và thêm " VND"
        result.reverse()
        "$result VND"
    } catch (e: Exception) {
        "0 VND"
    }
}

fun String.formatPrice2(): String {
    return try {
        val cleanString = this
        // Nếu rỗng hoặc không phải số → trả về 0 VND
        if (cleanString.isEmpty()) return "0 VND"

        val price = cleanString.toLong()
        val s = price.toString()

        // Xây dựng chuỗi từ phải sang trái
        val result = StringBuilder()
        for (i in s.indices.reversed()) {
            result.append(s[i])
            // Mỗi 3 ký tự (tính từ phải) và chưa phải vị trí đầu tiên thì thêm dấu chấm
            if ((s.length - i) % 3 == 0 && i > 0) {
                result.append('.')
            }
        }

        // Đảo ngược lại và thêm " VND"
        result.reverse()
        "$result VND"
    } catch (e: Exception) {
        "0 VND"
    }
}