package ru.study.rundo

import bolts.Task
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.study.rundo.interfaces.*
import ru.study.rundo.models.*

object WorkWithServer {
    const val TOKEN_ERROR = "Login again please"

    private var listenerSave: SaveTrackResultHandler? = null
    fun addListenerSave(saveTrackResultHandler: SaveTrackResultHandler) {
        listenerSave = saveTrackResultHandler
    }

    private var listenerLogin: ServerHandler<TokenInfo>? = null
    var isLoginFinished: Boolean? = null
    fun addListenerLogin(serverHandler: ServerHandler<TokenInfo>?) {
        listenerLogin = serverHandler
    }

    private var listenerRegistration: ServerHandler<TokenInfo>? = null
    var isRegistrationFinished: Boolean? = null
    fun addListenerRegistration(serverHandler: ServerHandler<TokenInfo>?) {
        listenerRegistration = serverHandler
    }

    var listenerGetTracks: ServerHandler<TracksList>? = null
    var isGetTracksFinished: Boolean? = null
    fun addListenerGetTracks(serverHandler: ServerHandler<TracksList>?) {
        listenerGetTracks = serverHandler
    }

    fun registration(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): Task<TokenInfo> {
        isRegistrationFinished = false
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            val json = JSONObject()
                .put("email", email)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("password", password).toString().toRequestBody()
            retrofit.register(json).execute().body()
        }.continueWith({
            if (listenerRegistration != null) {
                if (!it.isFaulted) {
                    when (it.result?.status) {
                        "ok" -> it.result?.let { token -> listenerRegistration?.onSuccess(token) }
                        "error" -> it.result?.code?.let { code -> listenerRegistration?.onError(code) }
                    }
                } else {
                    listenerRegistration?.onError()
                }
            }
            isRegistrationFinished = null
            null
        }, Task.UI_THREAD_EXECUTOR)
    }

    fun login(email: String, password: String): Task<TokenInfo> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            isLoginFinished = false
            val json = JSONObject()
                .put("email", email)
                .put("password", password).toString().toRequestBody()
            retrofit.login(json).execute().body()
        }.continueWith({
            if (listenerLogin != null) {
                if (!it.isFaulted) {
                    when (it.result?.status) {
                        "ok" -> it.result?.let { token -> listenerLogin?.onSuccess(token) }
                        "error" -> it.result?.code?.let { code -> listenerLogin?.onError(code) }
                    }
                } else {
                    listenerLogin?.onError()
                }
            }
            isLoginFinished = null
            null
        }, Task.UI_THREAD_EXECUTOR)
    }


    fun save(track: Track, token: String): Task<Int> {
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

    fun getTracks(token: String): Task<TracksList> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            isGetTracksFinished = false
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
            if (listenerGetTracks != null) {
                if (!it.isFaulted) {
                    when (it.result?.status) {
                        "ok" -> it.result?.let { result -> listenerGetTracks?.onSuccess(result) }
                        "error" -> listenerGetTracks?.onError(TOKEN_ERROR)
                    }
                } else {
                    listenerGetTracks?.onError("Internet connection problems")
                }
            }
            isGetTracksFinished = null
            null
        }, Task.UI_THREAD_EXECUTOR)
    }

    private fun getPoint(json: RequestBody): Task<PointsList> {
        val retrofit = SingletonClass.retrofit
        return Task.callInBackground {
            retrofit.getPoints(json).execute().body()
        }
    }
}
