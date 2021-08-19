package ru.study.rundo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.study.rundo.R
import ru.study.rundo.TracksDatabase
import ru.study.rundo.adapters.NotificationsAdapter
import ru.study.rundo.interfaces.RequestNotificationInfo
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

        val db = TracksDatabase(requireContext())

        val listNotifications = db.getNotificationsList()  as MutableList<Notification>

        notificationsRecyclerViewAdapter =
            NotificationsAdapter(listNotifications, activity as RequestNotificationInfo)

        notificationsRecyclerView.layoutManager = LinearLayoutManager(view.context)
        notificationsRecyclerView.adapter = notificationsRecyclerViewAdapter

//        add.setOnClickListener {
//            val calendar = Calendar.getInstance()
//            val time = NotificationTime(12,
//                calendar.get(Calendar.MINUTE))
//            val date = NotificationDate(
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH) + 1,
//                calendar.get(Calendar.DAY_OF_MONTH) + 1
//            )

        add.setOnClickListener {
            val calendar = Calendar.getInstance()
            val time = NotificationTime(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE) + 1)
            val date = NotificationDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            notificationsRecyclerViewAdapter.add(Notification(time, date, "you need run now"))
//
//            Toast.makeText(requireContext(), "please set notification data", Toast.LENGTH_LONG)
//                .show()
        }
    }
}
