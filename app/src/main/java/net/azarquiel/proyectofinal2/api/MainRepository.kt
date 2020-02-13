package net.azarquiel.proyectofinal2.api

import android.util.Log
import net.azarquiel.proyectofinal2.api.WebAccess

import net.azarquiel.proyectofinal2.model.Amiibo
import net.azarquiel.proyectofinal2.model.Serie

class MainRepository {
    val service = WebAccess.amiiboService

    suspend fun getDataSeries(): List<Serie> {
        val webResponse = service.getDataSeries().await()
        if (webResponse.isSuccessful) {
            return webResponse.body()!!.amiibo
        }
        return emptyList()
    }

    suspend fun getDataAmiibos(key: String): List<Amiibo> {
        val webResponse = service.getDataAmiibos(key).await()
        if (webResponse.isSuccessful) {
            return webResponse.body()!!.amiibo
        }
        return emptyList()
    }

    suspend fun getDataTodosAmiibos(): List<Amiibo> {
        val webResponse = service.getDataTodosAmiibos().await()
        if (webResponse.isSuccessful) {
            return webResponse.body()!!.amiibo
        }
        return emptyList()
    }
}