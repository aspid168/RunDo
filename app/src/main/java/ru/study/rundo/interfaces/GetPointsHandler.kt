package ru.study.rundo.interfaces

import ru.study.rundo.models.Track

interface GetPointsHandler { // todo rename
    fun onSuccess(tracks: List<Track>)
    fun onError()
}
