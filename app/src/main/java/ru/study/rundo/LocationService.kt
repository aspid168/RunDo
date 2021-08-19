package ru.study.rundo

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import ru.study.rundo.interfaces.SaveTrackResultHandler
import ru.study.rundo.models.Point
import ru.study.rundo.models.Track


@SuppressLint("MissingPermission")
class LocationService : Service(), LocationListener {

    companion object {
        const val BROADCAST_ACTION_GET_DATA = "BROADCAST_ACTION_GET_DATA"
        const val BROADCAST_ACTION_ON_ERROR = "BROADCAST_ACTION_ON_ERROR"
        const val DISTANCE_EXTRA = "DISTANCE_EXTRA"
        const val TIME_EXTRA = "TIME_EXTRA"
        const val IS_GPS_ENABLED = "IS_GPS_ENABLED"
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
                1.toString(),
                "Run progress notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(n)
        }
        val notification: Notification = builder.build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        startTime = System.currentTimeMillis()
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000,
            0f,
            this
        )
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
        val workWithServer = WorkWithServer(this)
        workWithServer.addListenerSave(object : SaveTrackResultHandler {
            override fun onSuccess(serverId: Int?) {
                Toast.makeText(this@LocationService, "Track saved", Toast.LENGTH_SHORT).show()
            }

            override fun onError() {
                val tracksDatabase = TracksDatabase(this@LocationService)
                tracksDatabase.addTrack(track)
            }
        })
        workWithServer.save(track)
    }
}
