package net.azarquiel.proyectofinal2.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowtodosamiibos.view.*
import kotlinx.android.synthetic.main.rowuser.view.*
import net.azarquiel.proyectofinal2.R
import net.azarquiel.proyectofinal2.model.Amiibo
import net.azarquiel.proyectofinal2.model.Favorito

class TodosAmiibosAdapter(val context: Context,
                          val layout: Int
) : RecyclerView.Adapter<TodosAmiibosAdapter.ViewHolder>() {

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

    internal fun setTodosAmiibos(todosamiibos: List<Amiibo>) {
        this.dataList = todosamiibos
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Amiibo) {
            itemView.tvfav.text = dataItem.name
            itemView.tv_id_todosview.text = dataItem.tail
            itemView.tv_id_todosview.visibility = View.INVISIBLE

            val uid = FirebaseAuth.getInstance().uid
            val ref2 = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos")
            val refcomparacion = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos/${dataItem.tail}")

            ///CAMBIAR EL BOTÓN DE ESTRELLA
            ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("Boton","Cancelado")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.mapNotNull {
                        Log.d("Boton", "Cambiado")
                        if (p0.hasChild("${dataItem.tail}")){
                            itemView.boton_fav.setImageResource(R.drawable.ic_star_on)
                        }
                        else{
                            itemView.boton_fav.setImageResource(R.drawable.ic_star_off)
                        }
                    }

                }

            })



            itemView.boton_fav.setOnClickListener {
                //val uid = FirebaseAuth.getInstance().uid
                val id = dataItem.tail
                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos/$id")


                val tail = dataItem.tail
                val imagen = dataItem.image
                val name = itemView.tvfav.text.toString()

                Log.d("Boton", "$id, $ref, $name")

                val favorito = Favorito(id, name, imagen, tail)

                ref.setValue(favorito)
                    .addOnSuccessListener {
                        Log.d("Boton", "Finalmente insertamos el fav en la base de datos")
                    }

                ///CAMBIAR EL BOTÓN DE ESTRELLA
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("Boton","Cancelado")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.mapNotNull {
                            Log.d("Boton","Cambiado")
                            if (p0.hasChild("${dataItem.tail}")) {
                                itemView.boton_fav.setImageResource(R.drawable.ic_star_on)


                            }
                            else{
                                itemView.boton_fav.setImageResource(R.drawable.ic_star_off)
                            }

                        }

                    }

                })

            }

            // }
            Picasso.get().load(dataItem.image).into(itemView.ivfav)
            itemView.tag = dataItem
        }

    }




}
