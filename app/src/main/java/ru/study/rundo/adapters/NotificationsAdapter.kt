package ru.study.rundo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.study.rundo.R
import ru.study.rundo.interfaces.RequestNotificationInfo
import ru.study.rundo.models.Notification
import ru.study.rundo.models.NotificationDate
import ru.study.rundo.models.NotificationTime
import kotlin.coroutines.coroutineContext
import kotlin.system.measureNanoTime

class NotificationsAdapter(
    private val notificationsList: MutableList<Notification>,
    private val requestNotificationInfo: RequestNotificationInfo
) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    class ViewHolder(
        private val view: View,
        private val requestNotificationInfo: RequestNotificationInfo
    ) : RecyclerView.ViewHolder(view) {

        private val date = view.findViewById<TextView>(R.id.date)
        private val time = view.findViewById<TextView>(R.id.time)
        private val text = view.findViewById<TextView>(R.id.text)
        private val delete = view.findViewById<Button>(R.id.delete)
        private val edit = view.findViewById<Button>(R.id.edit)

        fun bind(
            position: Int,
            notification: Notification,
            click: (notification: Any) -> Unit,
            deleteElement: () -> Unit
        ) {
            changeColor(position)
            date.text = notification.let {
                "${it.date.day}.${it.date.month}.${it.date.year}"
            }
            time.text = notification.let {
                "${it.time.hours}:${it.time.minutes}"
            }
            text.text = notification.description

            edit.setOnClickListener {
                requestNotificationInfo.requestDate(notification.date) {
                    click(it)
                }
                requestNotificationInfo.requestTime(notification.time) {
                    click(it)
                }
                requestNotificationInfo.requestText(notification.description) {
                    click(it)
                }
            }
            delete.setOnClickListener {
                deleteElement()
            }
        }

        private fun changeColor(position: Int) {
            if (position % 2 == 0) {
                view.setBackgroundColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.trackItemBackground
                    )
                )
            } else {
                view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notofications_adapter_element, parent, false)
        return ViewHolder(view, requestNotificationInfo)
    }

    private var newNotification: Notification = Notification(
        time = NotificationTime(1, 1),
        date = NotificationDate(1, 1, 1),
        description = "null"
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, notificationsList[position], {
            when (it) {
                is NotificationDate -> {
                    newNotification.date = it
                }
                is NotificationTime -> {
                    newNotification.time = it
                }
                is String -> {
                    var isDuplicated = false
                    newNotification.description = it
                    notificationsList.forEach { element ->
                        if (element.time == newNotification.time && element.date == newNotification.date) {
                            Log.v("elem", element.toString())
                            Log.v("new", newNotification.toString())
                            isDuplicated = true
                        }
                    }
                    if (!isDuplicated) {
                        requestNotificationInfo.update(
                            newNotification,
                            notificationsList[position]
                        )
                        notificationsList[position] = newNotification
                        newNotification = Notification(
                        time = NotificationTime(1, 1),
                        date = NotificationDate(1, 1, 1),
                        description = "null"
                        )
                    }
                }
            }
            notifyItemChanged(position)
        }, {
            requestNotificationInfo.delete(notificationsList[position])
            notificationsList.removeAt(position)
            notifyDataSetChanged()
        })
    }

    override fun getItemCount(): Int {
        return notificationsList.size
    }

    fun add(notification: Notification) {
        var isDuplicated = false
        notificationsList.forEach {
            if (it.time == notification.time && it.date == notification.date) {
                isDuplicated = true
                return
            }
        }
        if (!isDuplicated) {
            notificationsList.add(notification)
            requestNotificationInfo.save(notification)
            notifyItemInserted(notificationsList.size)
        }
    }
}
