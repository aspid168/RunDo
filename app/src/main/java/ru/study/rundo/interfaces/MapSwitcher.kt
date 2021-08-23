package ru.study.rundo.interfaces

import ru.study.rundo.models.Track

interface MapSwitcher {
    fun switchToMap(track: Track)
}
