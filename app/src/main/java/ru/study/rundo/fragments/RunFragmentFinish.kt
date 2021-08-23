package ru.study.rundo.fragments

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import ru.study.rundo.LocationService
import ru.study.rundo.R
import ru.study.rundo.activities.RunActivity
import ru.study.rundo.interfaces.RunActivitySwitcher
import java.util.concurrent.TimeUnit

class RunFragmentFinish : Fragment() {

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
    private lateinit var animation: ObjectAnimator
    private var resultTime: Long? = null
    private var resultDistance: Float? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finish = view.findViewById(R.id.finish)
        stopWatch = view.findViewById(R.id.stopWatch)
        var startTime = System.currentTimeMillis()
        handler = Handler()

        animation = AnimatorInflater.loadAnimator(
            this.context,
            R.animator.flip_animation
        ) as ObjectAnimator
        animation.target = finish

        animation.doOnEnd {
            val act = activity
            if (act is RunActivitySwitcher && resultTime != null && resultDistance != null) {
                act.switchToResultFragment(resultTime!!, resultDistance!!)
            }
        }

        if (savedInstanceState != null) {
            animation.currentPlayTime = savedInstanceState.getLong(RunActivity.ANIMATION_PLAY_TIME)
            if (animation.currentPlayTime > 0) {
                animation.start()
            }
            resultTime = savedInstanceState.getLong(RunActivity.TIME_EXTRA)
            resultDistance = savedInstanceState.getFloat(RunActivity.DISTANCE_EXTRA)
            resultTime.let {stopWatch.text = it.toString()}
        }

        if(!animation.isRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(Intent(context, LocationService::class.java))
            } else {
                activity?.startService(Intent(context, LocationService::class.java))
            }
        }

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
                val stopTime = locationService.stopTime

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
                resultTime = intent?.getLongExtra(RunActivity.TIME_EXTRA, 0)
                resultDistance = intent?.getFloatExtra(RunActivity.DISTANCE_EXTRA, 0f)
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
            if (animation.currentPlayTime < 1) {
                animation.start()
            }
            activity?.stopService(Intent(view.context, LocationService::class.java))
            handler.removeCallbacks(runnable)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unbindService(serviceConnection)
        activity?.unregisterReceiver(broadcastReceiverReceiveData)
        activity?.unregisterReceiver(broadcastReceiverOnError)
        handler.removeCallbacks(runnable)
    }

    override fun onStart() {
        super.onStart()
        activity?.bindService(
            Intent(context, LocationService::class.java),
            serviceConnection, 0
        )
        activity?.registerReceiver(
            broadcastReceiverReceiveData,
            IntentFilter(LocationService.BROADCAST_ACTION_GET_DATA)
        )
        activity?.registerReceiver(
            broadcastReceiverOnError,
            IntentFilter(LocationService.BROADCAST_ACTION_ON_ERROR)
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(RunActivity.ANIMATION_PLAY_TIME, animation.currentPlayTime)
        resultDistance?.let { outState.putFloat(RunActivity.DISTANCE_EXTRA, it) }
        resultTime?.let { outState.putLong(RunActivity.TIME_EXTRA, it) }
    }
}
