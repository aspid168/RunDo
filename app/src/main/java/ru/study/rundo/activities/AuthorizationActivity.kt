package ru.study.rundo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.study.rundo.R
import ru.study.rundo.TracksDatabase
import ru.study.rundo.fragments.LoginFragment
import ru.study.rundo.fragments.RegistrationFragment
import ru.study.rundo.interfaces.AuthorizationActivityNavigator

class AuthorizationActivity : AppCompatActivity(), AuthorizationActivityNavigator {

    companion object {
        const val CURRENT_FRAGMENT = "current fragment"
        fun startActivity(context: Context) {
            context.startActivity(createIntent(context))
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, AuthorizationActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        if (savedInstanceState != null) {
            supportFragmentManager.getFragment(savedInstanceState, CURRENT_FRAGMENT)
                ?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.authorizationContainer, it)
                        .commit()
                }
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.authorizationContainer, LoginFragment())
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.findFragmentById(R.id.authorizationContainer)?.let {
            supportFragmentManager.putFragment(outState, CURRENT_FRAGMENT, it)
        }
    }

    override fun switchToLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authorizationContainer, LoginFragment())
            .commit()
    }

    override fun switchToRegistrationFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authorizationContainer, RegistrationFragment())
            .commit()
    }

    override fun goToMainActivity(token: String) {
        val sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
        sharedPreferences.edit().putString(MainActivity.TOKEN_EXTRA, token).apply()
        MainActivity.startActivity(this)
        finish()
    }
}
