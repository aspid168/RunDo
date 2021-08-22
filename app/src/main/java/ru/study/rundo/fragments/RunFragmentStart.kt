package ru.study.rundo.fragments

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.animation.doOnEnd
import ru.study.rundo.LocationService
import ru.study.rundo.R
import ru.study.rundo.activities.AuthorizationActivity
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.activities.RunActivity
import ru.study.rundo.activities.SplashScreenActivity
import ru.study.rundo.interfaces.RunActivitySwitcher

class RunFragmentStart : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_run_start, container, false)
    }

    private lateinit var start: Button
    private lateinit var animation: ObjectAnimator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start = view.findViewById(R.id.start)
        animation = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.flip_animation
        ) as ObjectAnimator
        animation.target = start
        animation.doOnEnd {
            val act = activity
            if (act is RunActivitySwitcher) {
                act.switchToFinishFragment()
            }
        }
        if (savedInstanceState != null) {
            animation.currentPlayTime = savedInstanceState.getLong(RunActivity.ANIMATION_PLAY_TIME)
            if (animation.currentPlayTime > 0) {
                animation.start()
            }
        }
        start.setOnClickListener {
            if (!animation.isRunning) {
                animation.start()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(RunActivity.ANIMATION_PLAY_TIME, animation.currentPlayTime)
    }
}
