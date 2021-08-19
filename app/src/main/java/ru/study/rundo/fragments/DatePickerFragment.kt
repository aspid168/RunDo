package ru.study.rundo.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import ru.study.rundo.models.Notification
import ru.study.rundo.models.NotificationDate
import ru.study.rundo.models.NotificationTime
import java.util.*

class DatePickerFragment(private val currentDate: NotificationDate, private val getDate: (notificationDate: NotificationDate) -> Unit) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year =  currentDate.year
        val month = currentDate.month - 1
        val day =  currentDate.day
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    override fun onDateSet(
        view: DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        getDate(NotificationDate(year, month + 1, dayOfMonth))
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        getDate(currentDate)
    }
}
