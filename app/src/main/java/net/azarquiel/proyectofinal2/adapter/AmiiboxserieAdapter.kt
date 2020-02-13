package net.azarquiel.proyectofinal2.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowamiibosxserie.view.*
import net.azarquiel.proyectofinal2.model.Serie
import net.azarquiel.proyectofinal2.model.Amiibo

class AmiiboxserieAdapter(val context: Context,
                          val layout: Int
) : RecyclerView.Adapter<AmiiboxserieAdapter.ViewHolder>() {

    private var dataList: List<Amiibo> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setAmiibos(amiibos: List<Amiibo>) {
        this.dataList = amiibos
        notifyDataSetChanged()
    }

    internal fun setTodosAmiibos(todosamiibos: List<Amiibo>) {
        this.dataList = todosamiibos
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Amiibo){
            itemView.tvAmiiboxserie.text = dataItem.name
            Picasso.get().load(dataItem.image).into(itemView.ivAmiiboxserie)
            itemView.tag = dataItem
        }

    }
}