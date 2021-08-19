package ru.study.rundo.fragments

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.study.rundo.LocationService
import ru.study.rundo.R
import ru.study.rundo.interfaces.RunActivitySwitcher
import java.util.concurrent.TimeUnit

class RunFragmentFinish : Fragment() {
    companion object {
        const val DISTANCE_EXTRA = "DISTANCE_EXTRA"
        const val TIME_EXTRA = "TIME_EXTRA"
        const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
        const val LOCATION_SERVICE_IS_RUNNING = "LOCATION_SERVICE_IS_RUNNING"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_run_finish, container, false)
    }

    private lateinit var finish: Button
    private lateinit var stopWatch: TextView
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private lateinit var broadcastReceiverReceiveData: BroadcastReceiver
    private lateinit var broadcastReceiverOnError: BroadcastReceiver

    private lateinit var locationService: LocationService

    private lateinit var serviceConnection: ServiceConnection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finish = view.findViewById(R.id.finish)
        stopWatch = view.findViewById(R.id.stopWatch)

        val sharedPreferences = activity?.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
        val locationServiceIsRunning =
            sharedPreferences?.getBoolean(LOCATION_SERVICE_IS_RUNNING, false)
        if (locationServiceIsRunning != null && !locationServiceIsRunning) {
            sharedPreferences.edit().putBoolean(LOCATION_SERVICE_IS_RUNNING, true).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(Intent(context, LocationService::class.java))
            } else {
                activity?.startService(Intent(context, LocationService::class.java))
            }
        }

        var startTime = System.currentTimeMillis()
        var stopTime: Long

        handler = Handler()
        runnable = Runnable {
            if (activity != null) {
                handler.postDelayed(runnable, 10)
                val millis = System.currentTimeMillis() - startTime
                val time = resources.getString(
                    R.string.timer,
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
                    (millis % 1000) / 10
                )
                stopWatch.text = time
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                locationService = (service as LocationService.MyBinder).getService()
                startTime = locationService.startTime
                stopTime = locationService.stopTime

                if (locationService.isGpsEnable) {
                    handler.post(runnable)
                } else {
                    startTime += System.currentTimeMillis() - stopTime
                    val millis = System.currentTimeMillis() - startTime
                    val time = resources.getString(
                        R.string.timer,
                        TimeUnit.MILLISECONDS.toMinutes(millis),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
                        (millis % 1000) / 10
                    )
                    stopWatch.text = time
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }


        broadcastReceiverReceiveData = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent?.getLongExtra(TIME_EXTRA, 0)
                val distance = intent?.getFloatExtra(DISTANCE_EXTRA, 0f)
                val act = activity
                if (act is RunActivitySwitcher && time != null && distance != null) {
                    act.switchToResultFragment(time, distance)
                }
            }
        }

        broadcastReceiverOnError = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isGpsEnabled = intent?.getBooleanExtra(LocationService.IS_GPS_ENABLED, false)
                if (isGpsEnabled == true) {
                    startTime = locationService.startTime
                    handler.post(runnable)
                } else {
                    handler.removeCallbacks(runnable)
                }
            }
        }

        finish.setOnClickListener {
            sharedPreferences?.edit()?.putBoolean(LOCATION_SERVICE_IS_RUNNING, false)?.apply()
            activity?.stopService(Intent(view.context, LocationService::class.java))
            handler.removeCallbacks(runnable)

        }
    }

    override fun onResume() {
        super.onResume()
        activity?.bindService(Intent(context, LocationService::class.java),
            serviceConnection, 0)
        activity?.registerReceiver(broadcastReceiverReceiveData,
            IntentFilter(LocationService.BROADCAST_ACTION_GET_DATA))
        activity?.registerReceiver(broadcastReceiverOnError,
            IntentFilter(LocationService.BROADCAST_ACTION_ON_ERROR))
    }

    override fun onPause() {
        super.onPause()
        activity?.unbindService(serviceConnection)
        activity?.unregisterReceiver(broadcastReceiverReceiveData)
        activity?.unregisterReceiver(broadcastReceiverOnError)
    }
}
