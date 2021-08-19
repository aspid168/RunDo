package ru.study.rundo

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_NOTIFICATION
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.RingtoneManager.getDefaultUri
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.activities.RunActivity
import ru.study.rundo.adapters.NotificationsAdapter
import java.util.*


class NotificationEventReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val notificationSound: Uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+ context.packageName + "/raw/notification_sound")//R.raw.notification_sound)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val n = NotificationChannel(
                100.toString(),
                "reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(n)
        }

        Log.v("here", "HEEEREEEEE")
        val repeatingIntent = Intent(context, RunActivity::class.java)
        val position = intent.getIntExtra("position", -1)
        val text = intent.getStringExtra("description")

        val pendingIntent = PendingIntent.getActivity(context, position, repeatingIntent, FLAG_UPDATE_CURRENT)



        val builder = NotificationCompat.Builder(context, "100")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder!")
            .setContentText(text)
            .setAutoCancel(true)

        val notification = builder.build()


        Log.v("action", intent.action.toString())
        Log.v("id", position.toString())

        if (intent.action ==  "MY_NOTIFICATION_MESSAGE") {
            notificationManager.notify(position, notification)
        }
    }
}
