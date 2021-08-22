package ru.study.rundo.fragments

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.study.rundo.R
import ru.study.rundo.TracksAndNotificationsDatabase
import ru.study.rundo.adapters.NotificationsAdapter
import ru.study.rundo.interfaces.NotificationHandler
import ru.study.rundo.models.Notification
import ru.study.rundo.models.NotificationDate
import ru.study.rundo.models.NotificationTime
import java.util.*

class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var add: FloatingActionButton
    private lateinit var notificationsRecyclerViewAdapter: NotificationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        add = view.findViewById(R.id.add)

        val db = TracksAndNotificationsDatabase(requireContext())
        val listNotifications = db.getNotificationsList()  as MutableList<Notification>
        db.close()

        notificationsRecyclerViewAdapter =
            NotificationsAdapter(listNotifications, activity as NotificationHandler)

        notificationsRecyclerView.layoutManager = LinearLayoutManager(view.context)
        notificationsRecyclerView.adapter = notificationsRecyclerViewAdapter


        add.setOnClickListener {
            val calendar = Calendar.getInstance()
            val time = NotificationTime(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))
            val date = NotificationDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH) + 1
            )

            notificationsRecyclerViewAdapter.add(Notification(time, date, "you need run now"))
        }
    }

    override fun onStop() {
        super.onStop()
        if (activity != null) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }

    override fun onStart() {
        super.onStart()
        if (activity != null) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }
}
