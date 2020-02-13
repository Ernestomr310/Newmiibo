package net.azarquiel.proyectofinal2.views

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.robertlevonyan.components.picker.ItemModel
import com.robertlevonyan.components.picker.PickerDialog
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.rowamiibosuser.view.*
import kotlinx.android.synthetic.main.rowfavoritos.view.*
import kotlinx.android.synthetic.main.rowmisamiibos.view.*
import kotlinx.android.synthetic.main.rowuser.*
import kotlinx.android.synthetic.main.rowuser.view.*
import net.azarquiel.proyectofinal2.R
import net.azarquiel.proyectofinal2.adapter.CustomAdapter
import net.azarquiel.proyectofinal2.adapter.TodosAmiibosAdapter
import net.azarquiel.proyectofinal2.model.Amiibo
import net.azarquiel.proyectofinal2.model.Favorito
import net.azarquiel.proyectofinal2.model.Serie
import net.azarquiel.proyectofinal2.model.User
import net.azarquiel.proyectofinal2.viewmodel.MainViewModel
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, android.widget.SearchView.OnQueryTextListener,
    SearchView.OnQueryTextListener {

    private lateinit var adapter: CustomAdapter
    private lateinit var adapter2: TodosAmiibosAdapter
    private lateinit var pickerDialog: PickerDialog
    private lateinit var todosamiibos: List<Amiibo>
    //var userselec: String = ""
    private lateinit var viewModel: MainViewModel
    //private lateinit var adapter3:GroupAdapter<ViewHolder>
    var selectedPhotoUri: Uri? = null
    private lateinit var nickAvatar: TextView
    private lateinit var ivAvatar: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        listenfavoritos()
        listenMisAmiibos()

        //COPRUEBA SI EL USUARIO ESTÁEN FIREBASE Y SI NO EJECUTA LA VISTA DE REGISTRO
        verificarusuariologeado()

        getTodosAmiibos()
        showTodosAmiibos()
        getUserinfo()


        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //A continuación accedemos a los elementos de la header asi:
        ivAvatar = nav_view.getHeaderView(0).ivAvatar
        nickAvatar = nav_view.getHeaderView(0).nickAvatar
        ivAvatar.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        supportActionBar?.setTitle("Todos los Amiibos")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity", "La foto fue seleccionada")

            selectedPhotoUri = data.data

            //Log.d("RegisterActivity","La foto en el onActivity result es $selectedPhotoUri")

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            //Log.d("RegisterActivity","El bitmap en el onActivity result es $bitmap")

            ivAvatar.setImageBitmap(bitmap)

            uploadImageToFirebaseStorage()
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectphoto_button.setBackgroundDrawable(bitmapDrawable)
        }
    }

    /*
    /
    / Sube foto a FireBase
    /
     */
    private fun uploadImageToFirebaseStorage(){
        Log.d("RegisterActivity", "Entra en la función")
        Log.d("RegisterActivity", "la foto es $selectedPhotoUri")
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")


        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Foto bien insertada")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "Localización del archivo: $it")

                    Modificarimagen(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity","no inserta foto")
            }
    }

    /*
    /
    / Esta función come la referencia de la imagen de perfil y la modifica
    /
     */
    private fun Modificarimagen(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profileImageUrl/")


        ref.setValue(profileImageUrl)
            .addOnSuccessListener {
                //Log.d("RegisterActivity", "Finalmente insertamos el usuario en la base de datos")
                Toast.makeText(baseContext, "Imagen modificada correctamente",
                    Toast.LENGTH_SHORT).show()
                //val intent = Intent(this, MainActivity::class.java)
                //Limpiar anteriores actividades
                //intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                //startActivity(intent)
            }
    }


    /*
    /
    / Esta función verifica que el usuario está logeado
    /
     */
    private fun verificarusuariologeado(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /*
    /
    / Esta función Lista los Favoritos con el Item insertado
    /
     */
    private fun fetchFavs(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos")
        val adapterfav = GroupAdapter<ViewHolder>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext, "No tienes Amiibos favoritos", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {


                p0.children.forEach() {
                    val fav = it.getValue(Favorito::class.java)
                    if (fav != null) {
                        adapterfav.add(FavItem(fav))

                    }
                    else{
                        Toast.makeText(baseContext, "No tienes Amiibos favoritos",
                            Toast.LENGTH_SHORT).show()
                    }

                }

                rvSeries.adapter = adapterfav
            }



        })
    }

    /*
    /
    / Esta función está pendiente de los cambios en favoritos y lista los datos otra vez
    /
     */
    private fun listenfavoritos(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos")


        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {


                    fetchFavs()

            }

        })
    }

    /*
    /
    / Esta función está pendiente de los cambios en mis amiibos creados y lista los datos otra vez
    /
     */
    private fun listenMisAmiibos(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/amiiboscreados")


        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                fetchCreaciones()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                //fetchCreaciones()
            }

            override fun onChildRemoved(p0: DataSnapshot) {


                fetchCreaciones()

            }

        })
    }

    //Clase rápida de groupie para meter el viewholder de la row de favoritos que queremos
    class FavItem(val fav: Favorito): Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {
            Log.d("Nombres","$fav.name")
            Log.d("Nombres","${fav.favImageUrl}")
            viewHolder.itemView.tvfav.text = fav.name
            Picasso.get().load(fav.favImageUrl).into(viewHolder.itemView.ivfav)


            viewHolder.itemView.boton_fav_vf.setOnClickListener {
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos/${fav.id}")
                //val ref2 = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos")

                ref.removeValue()

            }
        }

        override fun getLayout(): Int {
            return R.layout.rowfavoritos
        }
    }

    /*
    /
    / Esta función coge los datos del usuario para mostrarlos
    /
     */
    private fun getUserinfo(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        //val foto = FirebaseAuth.getInstance().currentUser?.photoUrl

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.mapNotNull {
                    val user = p0.getValue<User>(User::class.java)
                    nickAvatar.text = user?.username

                    Picasso.get().load(user?.profileImageUrl).into(ivAvatar)

                }

            }

        })

        //ivAvatar.setImageURI(foto)
        //nickAvatar.text = name
    }

    /*
    /
    / Esta función obtiene todos los amiibos de la api
    /
     */
    private fun getTodosAmiibos() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.getDataTodosAmiibos().observe(this, Observer {
            //adapter.setBares(it!!) // with nullable
            //it?.let{adapter.setBares(it)} // unwrap nullable it
            it?.let{
                todosamiibos = it
                adapter2.setTodosAmiibos(it)
            }  // to lambda
        })
    }

    /*
    /
    / Esta función muestra todos los amiibos
    /
     */
    private fun showTodosAmiibos() {
        adapter2 = TodosAmiibosAdapter(this, R.layout.rowtodosamiibos)
        rvSeries.layoutManager = LinearLayoutManager(this)
        rvSeries.adapter = adapter2
    }

    /*
        /
        / Esta función obtiene todos las series de juegos de la api
        /
         */
    private fun getSeries() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.getDataSeries().observe(this, Observer {
            //adapter.setBares(it!!) // with nullable
            //it?.let{adapter.setBares(it)} // unwrap nullable it
            it?.let(adapter::setSeries)  // to lambda
        })
    }

    /*
    /
    / Esta función muestra todas las series de los juegos
    /
     */
    private fun showSeries() {
        adapter = CustomAdapter(this, R.layout.rowserie)
        rvSeries.layoutManager = LinearLayoutManager(this)
        rvSeries.adapter = adapter
    }

    /*
    /
    / Esta función pasa la key de la serie a la actividad Amiiboxserie por intent para mostrar los amiibos de esa serie
    /
     */
    fun pulsadaSerie(view: View){
        val serie = view.tag as Serie
        val intent = Intent(this, Amiiboxserie::class.java)
        intent.putExtra("serie", serie)
        //intent.putExtra("user", user)
        startActivity(intent)
    }



    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /*
    /
    / Esta función crea el buscador para filtrar los datos
    /
     */
    private lateinit var searchView: SearchView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        // ***** <Filtro> ****
        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView
        searchView.setQueryHint("Buscar...")
        searchView.setOnQueryTextListener(this)
        // ***** </Filtro> ****
        return true
    }
    override fun onQueryTextChange(query: String): Boolean {
        adapter2.setTodosAmiibos(todosamiibos.filter { amiibo -> amiibo.name.toLowerCase().contains(query.toLowerCase()) })
        return false
    }
    override fun onQueryTextSubmit(text: String): Boolean {
        return false
    }

    /*
    /
    / Esta función muestra el dialog de añadir el nombre del amiibo
    /
     */
    private fun dialognuevoamiibo() {
        alert {
            title = "Nombre del amiibo nuevo"
            customView {
                verticalLayout {
                    lparams(width = wrapContent, height = wrapContent)
                    val etNombre = editText {
                        hint = "Nombre"
                        padding = dip(16)
                        inputType = InputType.TYPE_CLASS_TEXT
                    }

                    positiveButton("Crear") {
                        if (etNombre.text.toString().isEmpty() )
                            toast("Rellena los campos...")
                        else
                            subeFotoNuevoAmiibo(etNombre.text.toString())
                    }
                }
            }
        }.show()
    }

    /*
      /
      / Esta función sube la foto del amiibo nuevo a insertar del picker ya sea de la galería o de la foto
      /
     */
    private fun subeFotoNuevoAmiibo(nombre: String){
        Log.d("RegisterActivity", "Entra en la función")
        Log.d("RegisterActivity", "la foto es $selectedPhotoUri")
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Foto bien insertada")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "Localización del archivo: $it")

                    guardarNuevoAmiibo(it.toString(),nombre)
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity","no inserta foto")
            }
    }

    /*
    /
    / Esta función insertael nuevo amiibo en la referencia
    /
     */
    private fun guardarNuevoAmiibo(profileImageUrl: String, nombre: String){
        val uid =  FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/amiiboscreados/$nombre")

        val amiibonuevo = Amiibo(nombre,profileImageUrl,nombre,"")

        ref.setValue(amiibonuevo)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finalmente insertamos el usuario en la base de datos")
                Toast.makeText(baseContext, "Amiibo Nuevo Guardado",
                    Toast.LENGTH_SHORT).show()
            }
    }

    /*
        /
        / Esta función muestra la pantalla para elegir foto
        /
         */
    private fun picker() {
        val itemModelc = ItemModel(ItemModel.ITEM_CAMERA)
        val itemModelg = ItemModel(ItemModel.ITEM_GALLERY)
        pickerDialog = PickerDialog.Builder(this)
            .setListType(PickerDialog.TYPE_GRID)
            .setItems(arrayListOf(itemModelg, itemModelc))
            .setDialogStyle(PickerDialog.DIALOG_MATERIAL)
            .create()

        pickerDialog.setPickerCloseListener { type, uri ->
            when (type) {
                ItemModel.ITEM_CAMERA -> {
                    selectedPhotoUri = uri
                    Log.d("RegisterActivity", "la foto es $selectedPhotoUri")
                }
                ItemModel.ITEM_GALLERY -> {
                    selectedPhotoUri = uri
                }
            }
        }

        pickerDialog.show(supportFragmentManager, "")
    }

    /*
    /
    / Esta función lista los amiibos creados mediante la inserccióndel item por groupie
    /
     */
    private fun fetchCreaciones() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/amiiboscreados")
        val adaptercreaciones = GroupAdapter<ViewHolder>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext, "No tienes Amiibos creados", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {


                p0.children.forEach() {
                    val amiiboc = it.getValue(Amiibo::class.java)
                    if (amiiboc != null) {
                        adaptercreaciones.add(AmiiboCreadoItem(amiiboc))

                    } else {
                        Toast.makeText(
                            baseContext, "No tienes Amiibos creados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                rvSeries.adapter = adaptercreaciones
            }


        })
    }

    //Clase rápida de groupie para meter el viewholder de la row de favoritos que queremos
    class AmiiboCreadoItem(val amiiboc: Amiibo): Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {
            Log.d("Nombres nombre","$amiiboc.name")
            Log.d("Nombres imagen","${amiiboc.image}")
            viewHolder.itemView.tvamiiboc
            viewHolder.itemView.tvamiiboc.text = amiiboc.name
            Picasso.get().load(amiiboc.image).into(viewHolder.itemView.ivamiiboc)


            viewHolder.itemView.boton_borrar_amiiboc.setOnClickListener {
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/amiiboscreados/${amiiboc.name}")
                //val ref2 = FirebaseDatabase.getInstance().getReference("/users/$uid/favoritos")

                ref.removeValue()

            }
        }

        override fun getLayout(): Int {
            return R.layout.rowmisamiibos
        }
    }


    /*
    /
    / Esta función lista los usuarios y le añade a la row el intent para ir a la actividad AmiiboUserActivity
    /
     */
    private fun fetchUsuarios() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        val adapterusuarios = GroupAdapter<ViewHolder>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext, "No tienes Amiibos creados", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {


                p0.children.forEach() {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapterusuarios.add(UsuarioItem(user))

                    } else {
                        Toast.makeText(
                            baseContext, "No tienes Amiibos creados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                adapterusuarios.setOnItemClickListener{
                        item,view->

                    val intent = Intent(view.context, AmiiboUserActivity::class.java)
                    val user = item as UsuarioItem
                    var usuario = user.user.uid
                    intent.putExtra("user", usuario)
                    startActivity(intent)
                }

                rvSeries.adapter = adapterusuarios
            }
        })
    }

    //Clase rápida de groupie para meter el viewholder de la row de favoritos que queremos
    class UsuarioItem(val user: User): Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {

            viewHolder.itemView.tvusername_u.text = user.username
            viewHolder.itemView.tv_uid.text = user.uid
            Picasso.get().load(user?.profileImageUrl).into(viewHolder.itemView.ivuser_u)
            viewHolder.itemView.tv_uid.visibility = View.INVISIBLE

        }

        override fun getLayout(): Int {
            return R.layout.rowuser
        }
    }


    /*
    /
    / Esta función es el menú donde hacemos las diferentes funciones depende de el icono que clickemos
    /
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                picker()
                searchView.setVisibility(View.GONE);
                dialognuevoamiibo()
            }
            R.id.nav_todos -> {
                getTodosAmiibos()
                showTodosAmiibos()
                supportActionBar?.setTitle("Todos los Amiibos")
                searchView.setVisibility(View.VISIBLE);
            }
            R.id.nav_categorias -> {
                getSeries()
                searchView.setVisibility(View.GONE);
                showSeries()
                supportActionBar?.setTitle("Game Series")
            }
            R.id.nav_fav -> {
                fetchFavs()
                searchView.setVisibility(View.GONE);
                supportActionBar?.setTitle("Favoritos")
            }
            R.id.nav_comunity -> {
                fetchUsuarios()
                searchView.setVisibility(View.GONE);
                supportActionBar?.setTitle("Comunidad")

            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.nav_send -> {
                fetchCreaciones()
                searchView.setVisibility(View.GONE);
                supportActionBar?.setTitle("Mis Amiibos")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }





}
