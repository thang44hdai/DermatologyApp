package com.example.safeaid.core.utils

import androidx.recyclerview.widget.DiffUtil

abstract class BaseDiffUtilCallBack<IR>: DiffUtil.Callback() {
    abstract val oldList: List<IR>
    abstract val newList: List<IR>
    abstract fun areItemsTheSame(oldItem: IR, newItem: IR): Boolean

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): IR = newList[newItemPosition]
}