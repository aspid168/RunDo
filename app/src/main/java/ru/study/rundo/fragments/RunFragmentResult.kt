package ru.study.rundo.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ru.study.rundo.R
import ru.study.rundo.activities.MainActivity
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class RunFragmentResult : Fragment() {
    companion object {
        const val DISTANCE_EXTRA = "DISTANCE_EXTRA"
        const val TIME_EXTRA = "TIME_EXTRA"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_run_result, container, false)
    }

    lateinit var timeDetails: TextView
    lateinit var distanceDetails: TextView
    lateinit var toMainPage: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeDetails = view.findViewById(R.id.timeDetails)
        distanceDetails = view.findViewById(R.id.distanceDetails)
        toMainPage = view.findViewById(R.id.toMainPage)
        val time = arguments?.getLong(TIME_EXTRA)
        val distance = arguments?.getFloat(DISTANCE_EXTRA)
        distance?.let {
            distanceDetails.text =
                resources.getString(R.string.distanceDetails, distance.roundToInt())
        }
        time?.let {
            timeDetails.text = resources.getString(
                R.string.timeDetails,
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
            )
        }
        toMainPage.setOnClickListener {
            MainActivity.startActivity(activity as Context) //TODO XZ
            activity?.finish()
        }
    }
}
