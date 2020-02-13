package net.azarquiel.proyectofinal2.api

import kotlinx.coroutines.Deferred
import net.azarquiel.proyectofinal2.model.Respuesta
import net.azarquiel.proyectofinal2.model.RespuestaAmibos
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AmiiboService {

    @GET("amiiboseries")
    fun getDataSeries(): Deferred<Response<Respuesta>>

    @GET("amiibo/?")
    fun getDataAmiibos(@Query("amiiboSeries") key: String): Deferred<Response<RespuestaAmibos>>
    //fun getDataAmiibos(@Path("key") key: String): Deferred<Response<RespuestaAmibos>>

    @GET("amiibo")
    fun getDataTodosAmiibos(): Deferred<Response<RespuestaAmibos>>

}