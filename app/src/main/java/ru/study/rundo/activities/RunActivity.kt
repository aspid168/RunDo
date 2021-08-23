package ru.study.rundo.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ru.study.rundo.LocationService
import ru.study.rundo.R
import ru.study.rundo.fragments.RunFragmentFinish
import ru.study.rundo.fragments.RunFragmentResult
import ru.study.rundo.fragments.RunFragmentStart
import ru.study.rundo.interfaces.RunActivitySwitcher
import java.util.Objects.hashCode

class RunActivity : AppCompatActivity(), RunActivitySwitcher {

    companion object {
        const val CURRENT_FRAGMENT = "CURRENT_FRAGMENT"
        const val DISTANCE_EXTRA = "DISTANCE_EXTRA"
        const val TIME_EXTRA = "TIME_EXTRA"
        const val RUN_FRAGMENT_FINISH_TAG = "RUN_FRAGMENT_FINISH_TAG"
        const val ANIMATION_PLAY_TIME = "ANIMATION_PLAY_TIME"
        fun startActivity(context: Context) {
            context.startActivity(createIntent(context))
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, RunActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)
        val sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
        val locationServiceIsRunning =
            sharedPreferences.getBoolean(LocationService.LOCATION_SERVICE_IS_RUNNING, false)

        if (savedInstanceState != null) {
            switchToLastFragment(savedInstanceState)
        } else {
            if (locationServiceIsRunning) {
                switchToFinishFragment()
            } else {
                switchToStartFragment()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.runContainer) !=
            supportFragmentManager.findFragmentByTag(RUN_FRAGMENT_FINISH_TAG)
        ) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Finish first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun switchToStartFragment() {
        val runFragmentStart = RunFragmentStart()
        supportFragmentManager.beginTransaction()
            .replace(R.id.runContainer, runFragmentStart).commit()
    }

    override fun switchToFinishFragment() {
        val runFragmentFinish = RunFragmentFinish()
        supportFragmentManager.beginTransaction()
            .replace(R.id.runContainer, runFragmentFinish, RUN_FRAGMENT_FINISH_TAG).commitAllowingStateLoss()
    }

    override fun switchToResultFragment(time: Long, distance: Float) {
        val runFragmentResult = RunFragmentResult()
        val bundle = Bundle()
        bundle.putLong(TIME_EXTRA, time)
        bundle.putFloat(DISTANCE_EXTRA, distance)
        runFragmentResult.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.runContainer, runFragmentResult)
            .commitAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        supportFragmentManager.findFragmentById(R.id.mainContainer)?.let {
            supportFragmentManager.putFragment(outState, CURRENT_FRAGMENT, it)
        }
    }

    private fun switchToLastFragment(savedInstanceState: Bundle) {
        supportFragmentManager.getFragment(savedInstanceState, CURRENT_FRAGMENT)?.apply {
            supportFragmentManager.beginTransaction().replace(R.id.mainContainer, this)
                .commit()
        }
    }
}
