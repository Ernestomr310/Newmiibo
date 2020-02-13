package net.azarquiel.proyectofinal2.views

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import net.azarquiel.proyectofinal2.R

import kotlinx.android.synthetic.main.activity_amiibo_user.*
import kotlinx.android.synthetic.main.content_amiibo_user.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.rowamiibosuser.view.*
import kotlinx.android.synthetic.main.rowmisamiibos.view.*
import net.azarquiel.proyectofinal2.model.Amiibo
import net.azarquiel.proyectofinal2.model.Serie

class AmiiboUserActivity : AppCompatActivity() {

    private lateinit var useruid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amiibo_user)
        setSupportActionBar(toolbar)

        supportActionBar?.setTitle("Amiibos de este usuario")

        useruid = intent.getStringExtra("user")

        fetchAmiibosUsuario()
        //rvAmiibosUser.layoutManager = LinearLayoutManager()
        //rvAmiibosUser.layoutManager = LinearLayoutManager(this)

        Log.d("AmiibouserActivity","ESELOCO QUE ME DICES $useruid")
    }

    /*
    /
    / Esta función lista los amiibos por usuario
    /
     */
    private fun fetchAmiibosUsuario() {
        Log.d("AmiibouserActivity","Entra en fetch")
        val ref = FirebaseDatabase.getInstance().getReference("/users/$useruid/amiiboscreados")
        Log.d("AmiibouserActivity","Referencia $ref")
        val adapteramiibosusuario = GroupAdapter<ViewHolder>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext, "No tiene Amiibos creados", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {


                p0.children.forEach() {
                    Log.d("AmiibouserActivity","Entra en el foreach")
                    val amiibosuser = it.getValue(Amiibo::class.java)
                    if (amiibosuser != null) {
                        Log.d("AmiibouserActivity","Entra en el if, por lo que amiibouser no es null")
                        Log.d("AmiibouserActivity","amiibouser: $amiibosuser")
                        adapteramiibosusuario.add(AmiibosUserItem(amiibosuser))

                    } else {
                        Toast.makeText(
                            baseContext, "No tiene Amiibos creados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                rvAmiibosUser.adapter = adapteramiibosusuario
            }


        })
    }

    //Clase rápida de groupie para meter el viewholder de la row de favoritos que queremos
    class AmiibosUserItem(val amiibosuser: Amiibo): Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {

            viewHolder.itemView.tvamiibouser.text = amiibosuser.name
            Picasso.get().load(amiibosuser.image).into(viewHolder.itemView.ivamiibouser)

        }

        override fun getLayout(): Int {
            return R.layout.rowamiibosuser
        }
    }



}
