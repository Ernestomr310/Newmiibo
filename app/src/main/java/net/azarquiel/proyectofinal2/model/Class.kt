package net.azarquiel.proyectofinal2.model

import com.xwray.groupie.ViewHolder
import net.azarquiel.proyectofinal2.R
import net.azarquiel.proyectofinal2.adapter.AmiiboxserieAdapter
import java.io.Serializable
import java.lang.reflect.Constructor

data class Serie (
    var key: String,
    var name: String
): Serializable

data class Amiibo (
    var character: String,
    var image: String,
    var name: String,
    var tail: String
):Serializable{
    constructor() : this("","","","")
}

data class Respuesta (
    val amiibo: List<Serie>
)

data class RespuestaAmibos (
    val amiibo:List<Amiibo>
)

data class Favorito(val id: String, val name: String, val favImageUrl: String, val tail: String){
    constructor() : this("","","","")
}


data class User(val uid: String, val username: String, val profileImageUrl: String){
    constructor() : this("","","")
}


