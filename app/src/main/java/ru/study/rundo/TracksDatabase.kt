package ru.study.rundo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.study.rundo.interfaces.SaveTrackResultHandler
import ru.study.rundo.models.*
import java.util.*


class TracksDatabase(private val context: Context) : SQLiteOpenHelper(
    context,
    DB_NAME,
    null,
    DB_VERSION
) {
    private companion object {
        private const val DB_NAME = "Tracks.db"
        private const val DB_VERSION = 1

        private const val TRACKS_TABLE = "tracks"
        private const val COL_1_ID = "id"
        private const val COL_2_SERVER_ID = "serverId"
        private const val COL_3_BEGINS_AT = "beginsAt"
        private const val COL_4_TIME = "time"
        private const val COL_5_DISTANCE = "distance"
        private const val COL_6_POINTS = "points"

        private const val NOTIFICATIONS_TABLE = "notifications"
        private const val COL_1_NOTIFICATION_ID = "id"
        private const val COL_2_NOTIFICATION_DATE_AND_TIME = "dateInMls"
        private const val COL_3_NOTIFICATION_DESCRIPTION = "description"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TRACKS_TABLE ($COL_1_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_2_SERVER_ID INTEGER,$COL_3_BEGINS_AT INTEGER,  $COL_4_TIME INTEGER, $COL_5_DISTANCE REAL, $COL_6_POINTS TEXT)")
        db?.execSQL("CREATE TABLE $NOTIFICATIONS_TABLE ($COL_1_NOTIFICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_2_NOTIFICATION_DATE_AND_TIME INTEGER,$COL_3_NOTIFICATION_DESCRIPTION TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.v("onUpgrade", "onUpgrade")
        db?.execSQL("DROP TABLE IF EXISTS $TRACKS_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $NOTIFICATIONS_TABLE")
        onCreate(db)
    }

    fun addTrack(track: Track) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_2_SERVER_ID, track.serverId)
            put(COL_3_BEGINS_AT, track.beginsAt)
            put(COL_4_TIME, track.time)
            put(COL_5_DISTANCE, track.distance)
            put(COL_6_POINTS, Gson().toJson(track.points).toString())
        }
        db.insert(
            TRACKS_TABLE, COL_2_SERVER_ID +
                    COL_3_BEGINS_AT +
                    COL_4_TIME +
                    COL_5_DISTANCE +
                    COL_6_POINTS, values
        )

    }

    fun refreshData(tracksList: List<Track>) {
        val db = writableDatabase
        val currentTracksList = getTracksList()
        val workWithServer = WorkWithServer(context)
        if (currentTracksList.size < tracksList.size) {
            tracksList.forEach {
                if (!currentTracksList.contains(it)) {
                    addTrack(it)
                }
            }
        } else {
            currentTracksList.forEachIndexed { index, track ->
                if (track.serverId == 0) {
                    workWithServer.addListenerSave(object : SaveTrackResultHandler {
                        override fun onSuccess(serverId: Int?) {
                            val values = ContentValues().apply {
                                put(COL_1_ID, index + 1)
                                put(COL_2_SERVER_ID, serverId)
                                put(COL_3_BEGINS_AT, track.beginsAt)
                                put(COL_4_TIME, track.time)
                                put(COL_5_DISTANCE, track.distance)
                                put(
                                    COL_6_POINTS,
                                    Gson().toJson(track.points).toString()
                                )
                            }
                            db.update(
                                TRACKS_TABLE,
                                values,
                                "$COL_1_ID=" + (index + 1).toString(),
                                null
                            )
                        }

                        override fun onError() {
                            TODO("Not yet implemented")
                        }
                    })
                    workWithServer.save(track)
                }
            }
        }
//        db.close()
    }

    fun getTracksList(): List<Track> {
        val db = writableDatabase
        val trackList = mutableListOf<Track>()
        val cursor = db.rawQuery("select * from $TRACKS_TABLE", null)
        while (cursor.moveToNext()) {
            val serverId = cursor.getInt(cursor.getColumnIndex(COL_2_SERVER_ID))
            val beginsAt = cursor.getLong(cursor.getColumnIndex(COL_3_BEGINS_AT))
            val time = cursor.getInt(cursor.getColumnIndex(COL_4_TIME))
            val distance = cursor.getInt(cursor.getColumnIndex(COL_5_DISTANCE))
            val points = cursor.getString(cursor.getColumnIndex(COL_6_POINTS))
            trackList.add(
                Track(
                    serverId, beginsAt, time.toLong(), distance.toFloat(),
                    Gson().fromJson(points, object : TypeToken<List<Point>>() {}.type)
                )
            )
        }
        trackList.sortByDescending { it.beginsAt }
        cursor.close()
        return trackList
    }

    fun addNotification(notification: Notification) {
        val db = writableDatabase

        val timeInMls = Calendar.getInstance()
        timeInMls.set(Calendar.YEAR, notification.date.year )
        timeInMls.set(Calendar.MONTH, notification.date.month - 1 )
        timeInMls.set(Calendar.DAY_OF_MONTH, notification.date.day)
        timeInMls.set(Calendar.HOUR_OF_DAY, notification.time.hours )
        timeInMls.set(Calendar.MINUTE, notification.time.minutes )
        timeInMls.set(Calendar.SECOND, 0)
        timeInMls.set(Calendar.MILLISECOND, 0)

        Log.v("add", timeInMls.timeInMillis.toString())

        val values = ContentValues().apply {
//            put(COL_1_NOTIFICATION_ID, position)
            put(COL_2_NOTIFICATION_DATE_AND_TIME, timeInMls.timeInMillis)
            put(COL_3_NOTIFICATION_DESCRIPTION, notification.description)
        }
        db.insert(NOTIFICATIONS_TABLE, null, values)
    }

    fun updateNotification(newNotification: Notification, currentNotification: Notification) {
        val db = writableDatabase

        val newNotificationTimeInMls = Calendar.getInstance()
        newNotificationTimeInMls.set(Calendar.YEAR, newNotification.date.year)
        newNotificationTimeInMls.set(Calendar.MONTH, newNotification.date.month - 1)
        newNotificationTimeInMls.set(Calendar.DAY_OF_MONTH, newNotification.date.day)
        newNotificationTimeInMls.set(Calendar.HOUR_OF_DAY, newNotification.time.hours)
        newNotificationTimeInMls.set(Calendar.MINUTE, newNotification.time.minutes)
        newNotificationTimeInMls.set(Calendar.SECOND, 0)
        newNotificationTimeInMls.set(Calendar.MILLISECOND, 0)


        val currentNotificationTimeInMls = Calendar.getInstance()
        currentNotificationTimeInMls.set(Calendar.YEAR, currentNotification.date.year)
        currentNotificationTimeInMls.set(Calendar.MONTH, currentNotification.date.month - 1)
        currentNotificationTimeInMls.set(Calendar.DAY_OF_MONTH, currentNotification.date.day)
        currentNotificationTimeInMls.set(Calendar.HOUR_OF_DAY, currentNotification.time.hours)
        currentNotificationTimeInMls.set(Calendar.MINUTE, currentNotification.time.minutes)
        currentNotificationTimeInMls.set(Calendar.SECOND, 0)
        currentNotificationTimeInMls.set(Calendar.MILLISECOND, 0)

        val values = ContentValues().apply {
            put(COL_2_NOTIFICATION_DATE_AND_TIME, newNotificationTimeInMls.timeInMillis)
            put(COL_3_NOTIFICATION_DESCRIPTION, newNotification.description)
        }

        val item = db.rawQuery("SELECT $COL_1_NOTIFICATION_ID FROM $NOTIFICATIONS_TABLE WHERE $COL_2_NOTIFICATION_DATE_AND_TIME = ${currentNotificationTimeInMls.timeInMillis}", null)
        item.moveToNext()
        db.update(NOTIFICATIONS_TABLE, values, COL_1_NOTIFICATION_ID + "=" + item.getString(item.getColumnIndex(COL_1_NOTIFICATION_ID)), null)
        item.close()
    }

    fun getId(notification: Notification): Int {
        val db = writableDatabase

        val currentNotificationTimeInMls = Calendar.getInstance()
        currentNotificationTimeInMls.set(Calendar.YEAR, notification.date.year)
        currentNotificationTimeInMls.set(Calendar.MONTH, notification.date.month - 1)
        currentNotificationTimeInMls.set(Calendar.DAY_OF_MONTH, notification.date.day)
        currentNotificationTimeInMls.set(Calendar.HOUR_OF_DAY, notification.time.hours)
        currentNotificationTimeInMls.set(Calendar.MINUTE, notification.time.minutes)
        currentNotificationTimeInMls.set(Calendar.SECOND, 0)
        currentNotificationTimeInMls.set(Calendar.MILLISECOND, 0)

        val item = db.rawQuery("SELECT $COL_1_NOTIFICATION_ID FROM $NOTIFICATIONS_TABLE WHERE $COL_2_NOTIFICATION_DATE_AND_TIME = ${currentNotificationTimeInMls.timeInMillis}", null)
        item.moveToNext()
        val id = item.getInt(item.getColumnIndex(COL_1_NOTIFICATION_ID))
        item.close()
        return id
    }

    fun deleteNotification(notification: Notification) {
        val db = writableDatabase

        val timeInMls = Calendar.getInstance()
        timeInMls.set(Calendar.YEAR, notification.date.year)
        timeInMls.set(Calendar.MONTH, notification.date.month - 1)
        timeInMls.set(Calendar.DAY_OF_MONTH, notification.date.day)
        timeInMls.set(Calendar.HOUR_OF_DAY, notification.time.hours)
        timeInMls.set(Calendar.MINUTE, notification.time.minutes)
        timeInMls.set(Calendar.SECOND, 0)
        timeInMls.set(Calendar.MILLISECOND, 0)

        db.delete(NOTIFICATIONS_TABLE, COL_2_NOTIFICATION_DATE_AND_TIME + "=" + timeInMls.timeInMillis, null)
    }

    fun deleteNotificationById(id: Int) {
        val db = writableDatabase
        db.delete(NOTIFICATIONS_TABLE, "$COL_1_NOTIFICATION_ID=$id", null)
    }

    fun getNotificationsList(): List<Notification> {
        val db = writableDatabase
        val calendar = Calendar.getInstance()
        val notificationsList = mutableListOf<Notification>()
        val cursor = db.rawQuery("select * from $NOTIFICATIONS_TABLE", null)
        while (cursor.moveToNext()) {
            val dateAndTime = cursor.getLong(cursor.getColumnIndex(COL_2_NOTIFICATION_DATE_AND_TIME))
            val description = cursor.getString(cursor.getColumnIndex(COL_3_NOTIFICATION_DESCRIPTION))
            calendar.timeInMillis = dateAndTime
            notificationsList.add(
                Notification(NotificationTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)), NotificationDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)), description)
            )
        }
        cursor.close()
        return notificationsList
    }

    fun getAllNotificationsId(): List<Int> {
        val db = writableDatabase
        val idList = mutableListOf<Int>()
        val cursor = db.rawQuery("SELECT $COL_1_NOTIFICATION_ID FROM $NOTIFICATIONS_TABLE", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex(COL_1_NOTIFICATION_ID))
            idList.add(id)
        }
        cursor.close()
        return idList
    }

    fun clear() {
        context.deleteDatabase(DB_NAME)
    }
}
