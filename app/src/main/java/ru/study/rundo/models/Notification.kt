package ru.study.rundo.models

data class Notification(
    var time: NotificationTime,
    var date: NotificationDate,
    var description: String
)

