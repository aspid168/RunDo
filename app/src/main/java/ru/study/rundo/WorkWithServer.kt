package ru.study.rundo

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import bolts.CancellationToken
import bolts.CancellationTokenSource
import bolts.Task
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.interfaces.*
import ru.study.rundo.models.*

class WorkWithServer(context: Context) {

    companion object{
        private const val TOKEN_ERROR = "Login again please"
    }

    //"token":"117:c2ef0587602023c3175fc4750a6e4493cad48ebf"
    //"aspid168@gmail.com", "aspid", "aspid", "aspid168"

    // test@gmail.com, test, test, test
    // token = 159:1332ccecd5cf651af95a3b76ad977e50e1a0c8ae

    // workWithServer.registration("qwe@gmail.com", "qwe", "qwe", "qwe")
    // token = "161:7ddd265adfe81a608e0708d9dd29cf9f9036d39c"


    private val sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)

    private val token = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
        .getString(MainActivity.TOKEN_EXTRA, null)

    private var listenerSave: SaveTrackResultHandler? = null


    fun addListenerSave(saveTrackResultHandler: SaveTrackResultHandler) {
        listenerSave = saveTrackResultHandler
    }

    private var listenerLogin: Handler<TokenInfo>? = null
    fun addListenerLogin(handler: Handler<TokenInfo>) {
        listenerLogin = handler
    }

    private var listenerRegistration: Handler<TokenInfo>? = null
    fun addListenerRegistration(handler: Handler<TokenInfo>) {
        listenerRegistration = handler
    }

    var listenerGetTracks: Handler<TracksList>? = null
    fun addListenerGetTracks(handler: Handler<TracksList>) {
        listenerGetTracks = handler
    }

//    private var listenerGetPoints: GetPointsHandler? = null
//    fun addListenerGetPoints(getTracksResultHandler: GetPointsHandler) {
//        listenerGetPoints = getTracksResultHandler
//    }

    fun registration(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): Task<TokenInfo> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            val json = JSONObject()
                .put("email", email)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("password", password).toString().toRequestBody()
            retrofit.register(json).execute().body()
        }.continueWith {
            if (!it.isFaulted) {
                when (it.result?.status) {
                    "ok" -> it.result?.let { token -> listenerRegistration?.onSuccess(token) }
                    "error" -> it.result?.code?.let { code -> listenerRegistration?.onError(code) }
                }
            } else {
                listenerRegistration?.onError()
            }
            null
        }
    }

    fun login(email: String, password: String): Task<TokenInfo> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            val json = JSONObject()
                .put("email", email)
                .put("password", password).toString().toRequestBody()
            retrofit.login(json).execute().body()
        }.continueWith({
            if (!it.isFaulted) {
                when (it.result?.status) {
                    "ok" -> it.result?.let { token -> listenerLogin?.onSuccess(token) }
                    "error" -> it.result?.code?.let { code -> listenerLogin?.onError(code) }
                }
            } else {
                listenerLogin?.onError()
            }
            null
        }, Task.UI_THREAD_EXECUTOR)
    }


    fun save(track: Track): Task<Int> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            val json = JSONObject(SingletonClass.gson.toJson(track))
                .put("token", token)
                .toString()
                .toRequestBody()
            retrofit.save(json).execute().body()?.serverId
        }.continueWith {
            if (!it.isFaulted) {
                listenerSave?.onSuccess(it.result)
            } else {
                listenerSave?.onError()
            }
            null
        }
    }

    fun getTracks(): Task<TracksList> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            val json = JSONObject()
                .put("token", token)
                .toString()
                .toRequestBody()
            retrofit.getTracks(json).execute().body()
        }.onSuccess {
            if (it.result?.status == "ok") {
                it.result?.tracks?.forEachIndexed { index, track ->
                    val json = JSONObject()
                        .put("token", token)
                        .put("id", track.serverId)
                        .toString()
                        .toRequestBody()
                    val q = getPoint(json)
                    q.waitForCompletion()
                    if (q.isCompleted) {
                        it.result!!.tracks[index].points = q.result.points as MutableList<Point>
                    }

                }
            }
            it.result
        }.continueWith({
            if (!it.isFaulted) {
                when (it.result?.status) {
                    "ok" -> it.result?.let { result -> listenerGetTracks?.onSuccess(result) }
                    "error" -> listenerGetTracks?.onError(TOKEN_ERROR)
                }
            } else {
                listenerGetTracks?.onError("Internet connection problems")
            }
            null
        }, Task.UI_THREAD_EXECUTOR)
    }

    private fun getPoint(json: RequestBody): Task<PointsList> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            retrofit.getPoints(json).execute().body()
        }
    }

// qwe

    fun task(context: Context): Task<Void>? {
        return Task.callInBackground {
            addListenerGetTracks(object : Handler<TracksList>  {
                override fun onSuccess(result: TracksList) {
                    val db = TracksDatabase(context)
                    db.refreshData(result.tracks)
                }

                override fun onError(error: String) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    if (error == TOKEN_ERROR) {
                        sharedPreferences.edit().putBoolean(MainActivity.IS_TOKEN_VALID, false).apply()
                    }
                }
            })
            getTracks()
            null
        }
//            , Task.UI_THREAD_EXECUTOR, CancellationTokenSource().token)
    }
}
