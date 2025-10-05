package com.example.safeaid.core.base

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.ui.ConfirmRequest
import com.example.safeaid.core.utils.DataResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<ViewState, Event> : ViewModel() {

    private val _viewState = MutableStateFlow<DataResult<ViewState>?>(null)
    val viewState = _viewState.asStateFlow()

    private val mConfirmEvent = sharedEventFlow<ConfirmRequest>()

    protected lateinit var arguments: Bundle

    fun updateState(viewState: DataResult<ViewState>?) {
        _viewState.value = viewState
    }

    abstract fun onTriggerEvent(event: Event)

    fun getConfirmEvent(): SharedFlow<ConfirmRequest> = mConfirmEvent

    protected open fun requestConfirm(request: ConfirmRequest) {
        mConfirmEvent.emitOnViewModelScope(request)
    }

    protected fun <T> MutableSharedFlow<T>.emitOnViewModelScope(value: T) {
        viewModelScope.launch {
            this@emitOnViewModelScope.emit(value)
        }
    }

}
fun <T> sharedEventFlow(buffer: Int = 1, strategy: BufferOverflow = BufferOverflow.DROP_OLDEST) =
    MutableSharedFlow<T>(extraBufferCapacity = buffer, onBufferOverflow = strategy)