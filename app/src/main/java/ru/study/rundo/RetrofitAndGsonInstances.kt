package ru.study.rundo

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.study.rundo.interfaces.ServerApi

object RetrofitAndGsonInstances {
    val retrofit: ServerApi
    val gson: Gson
    private const val URL = "https://pub.zame-dev.org/senla-training-addition/"

    init  {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        this.retrofit = retrofit.create(ServerApi::class.java)
        this.gson = Gson()
    }
}
