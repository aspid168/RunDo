package ru.study.rundo.fragments

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.animation.doOnEnd
import ru.study.rundo.R
import ru.study.rundo.activities.AuthorizationActivity
import ru.study.rundo.interfaces.RunActivitySwitcher

class RunFragmentStart : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_run_start, container, false)
    }

    private lateinit var start: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start = view.findViewById(R.id.start)
        start.setOnClickListener {
            val anim = AnimatorInflater.loadAnimator(this.context, R.animator.flip_animation) as ObjectAnimator
            anim.target = it
//            anim.start()
//            anim.doOnEnd {
                val act = activity
                if (act is RunActivitySwitcher) {
                    act.switchToFinishFragment()
                }
//            }
        }
    }
}
