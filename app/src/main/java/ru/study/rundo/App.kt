package ru.study.rundo

import android.app.Application
import android.util.Log
import ru.study.rundo.activities.MainActivity


class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.v("app", "app")
        val token = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
            .getString(MainActivity.TOKEN_EXTRA, null)
        if (token != null) {
            val workWithServer = WorkWithServer(this)
            workWithServer.task(this)
        }
    }
}
