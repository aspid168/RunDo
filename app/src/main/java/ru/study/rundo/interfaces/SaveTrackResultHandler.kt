package ru.study.rundo.interfaces

interface SaveTrackResultHandler {
    fun onSuccess(serverId: Int?)
    fun onError()
}
