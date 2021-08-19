package ru.study.rundo.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import ru.study.rundo.models.NotificationTime
import java.util.*
import kotlin.math.min

class TimePickerFragment(private val currentTime: NotificationTime, private val getTime: (notificationTime: NotificationTime) -> Unit): DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = currentTime.hours
        val minute = currentTime.minutes
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        getTime(NotificationTime(hourOfDay, minute))
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        getTime(currentTime)
    }
}
