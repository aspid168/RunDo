package ru.study.rundo.interfaces

interface RunActivitySwitcher {
    fun switchToStartFragment()
    fun switchToFinishFragment()
    fun switchToResultFragment(time: Long, distance: Float)
}
