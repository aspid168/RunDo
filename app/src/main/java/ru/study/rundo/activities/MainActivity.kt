package ru.study.rundo.activities

import android.Manifest
import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import ru.study.rundo.NotificationEventReceiver
import ru.study.rundo.R
import ru.study.rundo.TracksDatabase
import ru.study.rundo.fragments.*
import ru.study.rundo.interfaces.MapSwitcher
import ru.study.rundo.interfaces.RequestNotificationInfo
import ru.study.rundo.models.Notification
import ru.study.rundo.models.NotificationDate
import ru.study.rundo.models.NotificationTime
import ru.study.rundo.models.Track
import java.util.*


class MainActivity : AppCompatActivity(), MapSwitcher, RequestNotificationInfo {

    companion object {
        private const val TRACK_EXTRA = "TRACK_EXTRA"
        const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
        const val LOCATION_SERVICE_IS_RUNNING = "LOCATION_SERVICE_IS_RUNNING"
        const val CURRENT_FRAGMENT = "CURRENT_FRAGMENT"
        const val TOKEN_EXTRA = "TOKEN_EXTRA"
        const val IS_TOKEN_VALID = "IS_TOKEN_VALID"
        const val IS_SESSION_ACTIVE = "IS_SESSION_ACTIVE"

        fun startActivity(context: Context) {
            context.startActivity(createIntent(context))
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    private lateinit var toolBar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var logOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.nav_view)
        logOut = findViewById(R.id.logOut)

        configureToolbar()
        configureDrawer()

        getPermissionIfNecessary()
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
        val db = TracksDatabase(this)
        val locationServiceIsRunning = sharedPreferences.getBoolean(
            LOCATION_SERVICE_IS_RUNNING,
            false
        )

        logOut.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            deleteAllNotifications()
            db.clear()
            AuthorizationActivity.startActivity(this)
            sharedPreferences.edit().remove(IS_TOKEN_VALID).apply()
            drawerLayout.closeDrawers()
            finish()
        }

        if (locationServiceIsRunning) {
            RunActivity.startActivity(this)
            finish()
        } else {
            if (savedInstanceState != null) {
                switchToLastFragment(savedInstanceState)
            } else {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainContainer, TracksFragment())
                    .commit()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        supportFragmentManager.findFragmentById(R.id.mainContainer)?.let {
            supportFragmentManager.putFragment(outState, CURRENT_FRAGMENT, it)
        }
    }

    private fun switchToLastFragment(savedInstanceState: Bundle) {
        supportFragmentManager
            .getFragment(savedInstanceState, CURRENT_FRAGMENT)
            ?.apply {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainContainer, this)
                    .commit()
            }
    }

    override fun switchToMap(track: Track) {
        val mapFragment = MapFragment()
        val bundle = Bundle()
        bundle.putSerializable(TRACK_EXTRA, track)
        mapFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.mainContainer, mapFragment)
            .commit()
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    }
//    private fun checkPermissions(): Boolean {
//        val isAccessFineLocationGranted = ContextCompat
//            .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
//                PackageManager.PERMISSION_GRANTED
//        val isAccessCoarseLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        return isAccessFineLocationGranted &&
//                isAccessCoarseLocationGranted
//
//    }
//    private fun getPermissionIfNecessary() {
//        if (!checkPermissions()) {
//            ActivityCompat.requestPermissions(
//                this as Activity,
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                1
//            )
//        }
//    }

    private fun getPermissionIfNecessary() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawers()
            return
        }
        super.onBackPressed()
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    private fun configureToolbar() {
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)
    }

    private fun configureDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolBar,
            R.string.distanceDetails,
            R.string.distanceDetails
        )
        drawerLayout.addDrawerListener(drawerToggle)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.toMainPage -> {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.mainContainer, TracksFragment())
                            .commit()
                    }
                    drawerLayout.closeDrawers()
                }
                R.id.toNotifications -> {
                    if (supportFragmentManager.backStackEntryCount < 1) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.mainContainer, NotificationsFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.mainContainer, NotificationsFragment())
                            .commit()
                    }

                    drawerLayout.closeDrawers()
                }
            }
            !drawerLayout.isDrawerVisible(GravityCompat.START)
        }
    }


    override fun requestDate(
        currentDate: NotificationDate,
        getDate: (notificationDate: NotificationDate) -> Unit
    ) {
        val datePickerFragment = DatePickerFragment(currentDate, getDate)
        datePickerFragment.show(supportFragmentManager, null)
    }

    override fun requestTime(
        currentTime: NotificationTime,
        getTime: (notificationTime: NotificationTime) -> Unit
    ) {
        val datePickerFragment = TimePickerFragment(currentTime, getTime)
        datePickerFragment.show(supportFragmentManager, null)
    }

    override fun requestText(currentText: String, getText: (text: String) -> Unit) {
        val inputTextView = EditText(this)
        inputTextView.setText(currentText)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Notification")
        alertDialogBuilder.setMessage("Enter notification text")
        alertDialogBuilder.setView(inputTextView)
        alertDialogBuilder.setPositiveButton(
            "Ok"
        ) { _, _ ->
            getText(inputTextView.text.toString())
        }
        alertDialogBuilder.setOnCancelListener {
            getText(currentText)
        }
        alertDialogBuilder.show()
    }

    override fun save(notification: Notification) {
        val dataBase = TracksDatabase(this)
        dataBase.addNotification(notification)
        val position = dataBase.getId(notification)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, notification.time.hours)
        calendar.set(Calendar.MINUTE, notification.time.minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.YEAR, notification.date.year)
        calendar.set(Calendar.MONTH, notification.date.month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, notification.date.day)

        val intent = Intent(this, NotificationEventReceiver::class.java)
        intent.action = "MY_NOTIFICATION_MESSAGE"

        intent.putExtra("description", notification.description)
        intent.putExtra("position", position)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            position,
            intent,
            0
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (System.currentTimeMillis() < calendar.timeInMillis) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//            val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.time.time, pendingIntent)
//            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        }
    }

    override fun update(newNotification: Notification, currentNotification: Notification) {
        val dataBase = TracksDatabase(this)
        val position = dataBase.getId(currentNotification)
        Log.v("id save", position.toString())
        dataBase.updateNotification(newNotification, currentNotification)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, newNotification.time.hours)
        calendar.set(Calendar.MINUTE, newNotification.time.minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.YEAR, newNotification.date.year)
        calendar.set(Calendar.MONTH, newNotification.date.month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, newNotification.date.day)

        val intent = Intent(this, NotificationEventReceiver::class.java)
        intent.action = "MY_NOTIFICATION_MESSAGE"

        intent.putExtra("description", newNotification.description)
        intent.putExtra("position", position)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            position,
            intent,
            FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (System.currentTimeMillis() < calendar.timeInMillis) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    override fun delete(notification: Notification) {

        val dataBase = TracksDatabase(this)
        val id = dataBase.getId(notification)
        dataBase.deleteNotification(notification)

        val intent = Intent(this, NotificationEventReceiver::class.java)
        intent.action = "MY_NOTIFICATION_MESSAGE"

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            id,
            intent,
            0
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun deleteAllNotifications() {
        val db = TracksDatabase(this)
        val idList = db.getAllNotificationsId()
        Log.v("qwe", idList.toString())
        idList.forEach {
            val intent = Intent(this, NotificationEventReceiver::class.java)
            intent.action = "MY_NOTIFICATION_MESSAGE"

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                it,
                intent,
                0
            )

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
