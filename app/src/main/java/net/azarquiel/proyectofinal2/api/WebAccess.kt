package net.azarquiel.proyectofinal2.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import net.azarquiel.proyectofinal2.api.AmiiboService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WebAccess {
    val amiiboService : AmiiboService by lazy{
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl("https://www.amiiboapi.com/api/")
            .build()

        return@lazy retrofit.create(AmiiboService::class.java)
    }
}