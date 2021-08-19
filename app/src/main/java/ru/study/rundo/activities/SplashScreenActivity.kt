package ru.study.rundo.activities

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import ru.study.rundo.R

class SplashScreenActivity : AppCompatActivity() {
    lateinit var logo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        logo = findViewById(R.id.logo)

        val token = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE).getString(
            MainActivity.TOKEN_EXTRA,
            null
        )
        val anim = AnimatorInflater.loadAnimator(this, R.animator.flip_animation) as ObjectAnimator

        anim.target = logo
        anim.start()

        anim.doOnEnd {
            if (token != null) {
                MainActivity.startActivity(this)
                finish()
            } else {
                AuthorizationActivity.startActivity(this)
            }
            finish()
        }
    }
}
