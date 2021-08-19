package ru.study.rundo.interfaces

interface SetNotificationInfo {
    fun setDate(position: Int): Int
    fun setTime(position: Int): Int
    fun setText(position: Int): String
}
