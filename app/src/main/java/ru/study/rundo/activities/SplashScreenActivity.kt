package ru.study.rundo.activities

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import ru.study.rundo.R
import ru.study.rundo.TracksAndNotificationsDatabase
import ru.study.rundo.WorkWithServer
import ru.study.rundo.interfaces.ServerHandler
import ru.study.rundo.models.TracksList

class SplashScreenActivity : AppCompatActivity(), ServerHandler<TracksList> {

    companion object {
        private const val ANIMATION_PLAY_TIME = "ANIMATION_PLAY_TIME"
    }

    private lateinit var logo: ImageView
    private lateinit var animation: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        logo = findViewById(R.id.logo)
        animation = AnimatorInflater.loadAnimator(this, R.animator.flip_animation) as ObjectAnimator
        animation.target = logo
        val token = getToken()
        if (savedInstanceState != null) {
            val animationPlayTime = savedInstanceState.getLong(ANIMATION_PLAY_TIME)
            animation.currentPlayTime = animationPlayTime
        } else {
            if (token == null) {
                animation.doOnEnd {
                    AuthorizationActivity.startActivity(this)
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        WorkWithServer.addListenerGetTracks(this)
        if (WorkWithServer.isGetTracksFinished == null) {
            val token = getToken()
            token?.let { WorkWithServer.getTracks(it) }
        }
        if (animation.currentPlayTime >= 3000) {
            animation.currentPlayTime = 0
        }
        animation.start()
    }

    override fun onStop() {
        super.onStop()
        WorkWithServer.addListenerGetTracks(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ANIMATION_PLAY_TIME, animation.currentPlayTime)
    }

    private fun getToken(): String? {
        return getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
            .getString(MainActivity.TOKEN_EXTRA, null)
    }


    override fun onSuccess(result: TracksList) {
        val token = getToken()
        if (token != null) {
            val db = TracksAndNotificationsDatabase(this)
            db.refreshData(result.tracks, token)
            db.close()
        }
        startMainActivity()
    }

    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        if (error == WorkWithServer.TOKEN_ERROR) {
            getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
                .edit().putBoolean(MainActivity.IS_TOKEN_VALID, false).apply()
        }
        startMainActivity()
    }

    private fun startMainActivity() {
        MainActivity.startActivity(this)
        finish()
    }
}
