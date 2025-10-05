package com.example.safeaid.core.utils

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val error: ErrorResponse) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}

fun Throwable.toErrorDefault(
    type: String? = null,
    message: String? = null,
    code: Int? = null
): DataResult.Error {
    return toError()
}

fun Throwable.toError(
    type: String = "Exception",
    message: String? = null,
    code: Int = 9999
): DataResult.Error {
    return DataResult.Error(ErrorResponse(type, message ?: toString(), code))
}

@Keep
data class ErrorResponse @JvmOverloads constructor(
    @SerializedName("errorType")
    val errorType: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("errorCode")
    val errorCode: Int = 0
){
    @SerializedName("title")
    var title: String? = null
}

inline fun <reified T> DataResult<T>.doIfFailure(callback: (error: ErrorResponse) -> Unit) {
    if (this is DataResult.Error) {
        callback(error)
    }
}

inline fun <reified T> DataResult<T>.doIfSuccess(callback: (value: T) -> Unit) {
    if (this is DataResult.Success) {
        callback(data)
    }
}

inline fun <reified T> DataResult<T>.onLoading(callback: () -> Unit) {
    if (this is DataResult.Loading) {
        callback()
    }
}

inline fun <reified T> Flow<DataResult<List<T>>>.filterData(crossinline callback: (T) -> Boolean): Flow<DataResult<List<T>>> {
    return flatMapConcat {
        flow {
            if (it is DataResult.Success) {
                emit(DataResult.Success(it.data.filter { item ->
                    callback(item)
                }))
            } else {
                emit(it)
            }
        }
    }
}

inline fun <reified T, V> Flow<DataResult<List<T>>>.mapData(crossinline callback: (T) -> V): Flow<DataResult<List<V>>> {
    return flatMapConcat {
        flow {
            if (it is DataResult.Success) {
                emit(DataResult.Success(it.data.map { item ->
                    callback(item)
                }))
            } else if (it is DataResult.Loading) {
                emit(it)
            } else if (it is DataResult.Error) {
                emit(it)
            }
        }
    }
}

inline fun <T, R : Comparable<R>> Flow<DataResult<List<T>>>.sortedByDescending(crossinline callback: (T) -> R): Flow<DataResult<List<T>>> {
    return flatMapConcat {
        flow {
            if (it is DataResult.Success) {
                emit(DataResult.Success(it.data.sortedByDescending {
                    callback(it)
                }))
            } else {
                emit(it)
            }
        }
    }
}

inline fun <T, R : Comparable<R>> Flow<DataResult<List<T>>>.sortedByAsc(crossinline callback: (T) -> R): Flow<DataResult<List<T>>> {
    return flatMapConcat {
        flow {
            if (it is DataResult.Success) {
                emit(DataResult.Success(it.data.sortedBy {
                    callback(it)
                }))
            } else {
                emit(it)
            }
        }
    }
}


inline fun <In, Out> Flow<DataResult<In>>.mapResult(crossinline mapper: (In) -> Out): Flow<DataResult<Out>> = map {
    try {
        it.mapResult(mapper)
    } catch (e: Exception) {
        e.toError()
    }
}
inline fun <In, Out> DataResult<In>.mapResult(mapper: (In) -> Out): DataResult<Out> {
    return when (this) {
        is DataResult.Loading -> DataResult.Loading
        is DataResult.Error -> DataResult.Error(error)
        is DataResult.Success -> DataResult.Success(mapper.invoke(data))
    }
}

