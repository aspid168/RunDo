package ru.study.rundo.interfaces

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import ru.study.rundo.models.*

interface ServerApi {
    @POST("lesson-26.php?method=register")
    fun register(@Body body: RequestBody): Call<TokenInfo>

    @POST("lesson-26.php?method=login")
    fun login(@Body body: RequestBody): Call<TokenInfo>

    @POST("lesson-26.php?method=tracks")
    fun getTracks(@Body body: RequestBody): Call<TracksList>

    @POST("lesson-26.php?method=points")
    fun getPoints(@Body body: RequestBody): Call<PointsList>

    @POST("lesson-26.php?method=save")
    fun save(@Body track: RequestBody): Call<Track>
}
