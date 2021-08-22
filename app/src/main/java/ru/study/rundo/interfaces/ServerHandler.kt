package ru.study.rundo.interfaces

interface ServerHandler<E> {
    fun onSuccess(result: E)
    fun onError(error: String = "internet connection error")
}
