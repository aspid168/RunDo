package ru.study.rundo

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.interfaces.SaveTrackResultHandler
import ru.study.rundo.models.Point
import ru.study.rundo.models.Track
import kotlin.math.roundToInt


@SuppressLint("MissingPermission")
class LocationService : Service(), LocationListener {

    companion object {
        const val BROADCAST_ACTION_GET_DATA = "BROADCAST_ACTION_GET_DATA"
        const val BROADCAST_ACTION_ON_ERROR = "BROADCAST_ACTION_ON_ERROR"
        const val DISTANCE_EXTRA = "DISTANCE_EXTRA"
        const val TIME_EXTRA = "TIME_EXTRA"
        const val IS_GPS_ENABLED = "IS_GPS_ENABLED"
        const val LOCATION_SERVICE_IS_RUNNING = "LOCATION_SERVICE_IS_RUNNING"
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    private var distance: Float = 0f
    private val pointsList = mutableListOf<Point>()
    var startTime = System.currentTimeMillis()
    var stopTime: Long = 0
    private val binder: IBinder = MyBinder()
    var isGpsEnable = true

    override fun onCreate() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Running")
            .setContentText("Run in progress")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val n = NotificationChannel(
                "1",
                "Run progress notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(n)
        }
        val notification = builder.build()
        startTime = System.currentTimeMillis()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            5f,
            this
        )
        val sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(LOCATION_SERVICE_IS_RUNNING, true).apply()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location) {
        addLocation(location)
        lastLocation = location
    }


    override fun onProviderDisabled(provider: String) {
        stopTime = System.currentTimeMillis()
        isGpsEnable = false
        sendBroadcast(Intent(BROADCAST_ACTION_ON_ERROR).putExtra(IS_GPS_ENABLED, false))
        sendNotification(
            NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Error")
                .setContentText("Please Enable GPS")
        )
    }

    override fun onProviderEnabled(provider: String) {
        startTime += System.currentTimeMillis() - stopTime
        isGpsEnable = true
        sendBroadcast(Intent(BROADCAST_ACTION_ON_ERROR).putExtra(IS_GPS_ENABLED, true))
        sendNotification(
            NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Running")
                .setContentText("Run in progress")
        )
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    private fun addLocation(location: Location?) {
        location?.let {
            pointsList.add(
                Point(it.latitude, it.longitude)
            )
            calculateDistance(location)
        }
    }

    private fun calculateDistance(location: Location) {
        lastLocation?.let {
            distance = distance.plus(location.distanceTo(lastLocation))
        }
    }

    private fun sendNotification(builder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification = builder.build()
        notificationManager.notify(1, notification)
    }

    inner class MyBinder : Binder() {
        fun getService(): LocationService {
            return this@LocationService
        }
    }

    override fun onDestroy() {
        addLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
        locationManager.removeUpdates(this)
        getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE).edit()
            .putBoolean(LOCATION_SERVICE_IS_RUNNING, false)
            .apply()

        val track = createTrack()
        saveTrack(track)
        sendBroadcast(
            Intent(BROADCAST_ACTION_GET_DATA).putExtra(DISTANCE_EXTRA, track.distance).putExtra(
                TIME_EXTRA, track.time
            )
        )
        super.onDestroy()
    }

    private fun createTrack(): Track {
        if (!isGpsEnable) {
            startTime += System.currentTimeMillis() - stopTime
        }
        val time = System.currentTimeMillis() - startTime
        return Track(null, startTime, time, distance, pointsList)
    }

    private fun saveTrack(track: Track) {
        val db = TracksAndNotificationsDatabase(this@LocationService)
        val token = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
            .getString(MainActivity.TOKEN_EXTRA, null)
        WorkWithServer.addListenerSave(object : SaveTrackResultHandler {
            override fun onSuccess(serverId: Int?) {

            }

            override fun onError() {
                track.distance = track.distance.roundToInt().toFloat()
                db.addTrack(track)
                db.close()
            }
        })
        token?.let { WorkWithServer.save(track, token) }
    }
}
