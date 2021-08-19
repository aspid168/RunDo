package ru.study.rundo.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import java.io.Serializable

data class Track(
    @SerializedName("id")
    val serverId: Int?,
    var beginsAt: Long,
    var time: Long,
    var distance: Float,
    var points: MutableList<Point>
): Serializable
