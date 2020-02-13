package net.azarquiel.proyectofinal2.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import net.azarquiel.proyectofinal2.R

import kotlinx.android.synthetic.main.activity_amiiboxserie.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_amiiboxserie.*
import net.azarquiel.proyectofinal2.adapter.AmiiboxserieAdapter
import net.azarquiel.proyectofinal2.model.Amiibo
import net.azarquiel.proyectofinal2.model.Serie
import net.azarquiel.proyectofinal2.viewmodel.MainViewModel

class Amiiboxserie : AppCompatActivity() {

    private lateinit var adapter: AmiiboxserieAdapter

    private lateinit var amiibos: List<Amiibo>

    private lateinit var viewModel: MainViewModel

    private lateinit var serie: Serie


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amiiboxserie)

        serie = intent.getSerializableExtra("serie") as Serie
        //supportActionBar?.setTitle("Amiibo de esta serie")

        getAmiibos()
        showAmiibos()
    }

    private fun getAmiibos() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.getDataAmiibos(serie.key).observe(this, Observer {
            //adapter.setBares(it!!) // with nullable
            //it?.let{adapter.setBares(it)} // unwrap nullable it
            it?.let{
                amiibos = it
                adapter.setAmiibos(it)
            }
        })
    }

    private fun showAmiibos() {
        adapter = AmiiboxserieAdapter(this, R.layout.rowamiibosxserie)
        rvAmiibosxserie.layoutManager = LinearLayoutManager(this)
        rvAmiibosxserie.adapter = adapter
    }

}