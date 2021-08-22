package ru.study.rundo.interfaces

import android.widget.TextView
import ru.study.rundo.fragments.NotificationsFragment
import ru.study.rundo.models.Notification
import ru.study.rundo.models.NotificationDate
import ru.study.rundo.models.NotificationTime


interface NotificationHandler {
    fun requestDate(currentDate: NotificationDate, getDate:(notificationDate: NotificationDate) -> Unit)
    fun requestTime(currentTime: NotificationTime, getTime:(notificationTime: NotificationTime) -> Unit)
    fun requestText(currentText: String, getText:(text: String) -> Unit)
    fun update(newNotification: Notification, currentNotification: Notification)
    fun save(notification: Notification)
    fun delete(notification: Notification)
}
