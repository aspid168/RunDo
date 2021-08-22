package ru.study.rundo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ru.study.rundo.R
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.activities.RunActivity
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class RunFragmentResult : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_run_result, container, false)
    }

    private lateinit var timeDetails: TextView
    private lateinit var distanceDetails: TextView
    private lateinit var toMainPage: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeDetails = view.findViewById(R.id.timeDetails)
        distanceDetails = view.findViewById(R.id.distanceDetails)
        toMainPage = view.findViewById(R.id.toMainPage)
        val time = arguments?.getLong(RunActivity.TIME_EXTRA)
        val distance = arguments?.getFloat(RunActivity.DISTANCE_EXTRA)
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
            MainActivity.startActivity(requireContext())
            activity?.finish()
        }
    }
}
