package net.azarquiel.proyectofinal2.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.azarquiel.proyectofinal2.api.MainRepository
import net.azarquiel.proyectofinal2.model.Amiibo
import net.azarquiel.proyectofinal2.model.Serie

class MainViewModel: ViewModel() {
    private var repository: MainRepository = MainRepository()

    fun getDataSeries(): MutableLiveData<List<Serie>> {
        val result = MutableLiveData<List<Serie>>()
        GlobalScope.launch(Dispatchers.Main) {
            result.value = repository.getDataSeries()
        }
        return result
    }

    fun getDataAmiibos(key: String): MutableLiveData<List<Amiibo>> {
        val result = MutableLiveData<List<Amiibo>>()
        GlobalScope.launch(Dispatchers.Main) {
            result.value = repository.getDataAmiibos(key)
        }
        return result
    }

    fun getDataTodosAmiibos(): MutableLiveData<List<Amiibo>> {
        val result = MutableLiveData<List<Amiibo>>()
        GlobalScope.launch(Dispatchers.Main) {
            result.value = repository.getDataTodosAmiibos()
        }
        return result
    }

}