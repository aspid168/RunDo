package ru.study.rundo.interfaces

interface Handler<E> {
    fun onSuccess(result: E)
    fun onError(error: String = "Error")
}
