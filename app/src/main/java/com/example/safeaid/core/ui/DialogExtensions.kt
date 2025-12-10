package com.example.safeaid.core.ui

import android.content.Context
import com.example.dermatology.R


// Extension functions for common dialog types
fun Context.showInfoDialog(
    title: String = "Thông báo",
    message: String,
    onConfirm: () -> Unit = {}
) {
    val dialog = BaseDialog(this)
    dialog.setView(
        title = title,
        message = message,
        onClickPositive = onConfirm,
        onClickNegative = null,
        positiveText = "OK"
    )
    dialog.show()
}

fun Context.showWarningDialog(
    title: String = "Cảnh báo",
    message: String,
    onConfirm: () -> Unit = {},
    onCancel: (() -> Unit)? = null
) {
    val dialog = BaseDialog(this)
    dialog.setView(
        title = title,
        message = message,
        onClickPositive = onConfirm,
        onClickNegative = onCancel,
        positiveText = "Tiếp tục",
        negativeText = "Hủy"
    )
    dialog.show()
}

fun Context.showErrorDialog(
    title: String = "Lỗi",
    message: String,
    onConfirm: () -> Unit = {}
) {
    val dialog = BaseDialog(this)
    dialog.setView(
        title = title,
        message = message,
        onClickPositive = onConfirm,
        onClickNegative = null,
        positiveText = "OK"
    )
    dialog.show()
}

fun Context.showSuccessDialog(
    title: String = "Thành công",
    message: String,
    onConfirm: () -> Unit = {}
) {
    val dialog = BaseDialog(this)
    dialog.setView(
        title = title,
        message = message,
        onClickPositive = onConfirm,
        onClickNegative = null,
        positiveText = "OK"
    )
    dialog.show()
}

fun Context.showConfirmDialog(
    title: String = "Xác nhận",
    message: String,
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null,
    confirmText: String = "Xác nhận",
    cancelText: String = "Hủy"
) {
    val dialog = BaseDialog(this)
    dialog.setView(
        title = title,
        message = message,
        onClickPositive = onConfirm,
        onClickNegative = onCancel,
        positiveText = confirmText,
        negativeText = cancelText
    )
    dialog.show()
}