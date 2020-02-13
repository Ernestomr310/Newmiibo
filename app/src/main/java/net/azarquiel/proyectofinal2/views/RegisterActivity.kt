package net.azarquiel.proyectofinal2.views

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.view.*
import net.azarquiel.proyectofinal2.model.User
import org.jetbrains.anko.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    var selectedPhotoUri: Uri? = null


    companion object {
        const val TAG="ERNESTO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(net.azarquiel.proyectofinal2.R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        onStart()

        register_button_register.setOnClickListener{
            pb_register.visibility = View.VISIBLE
            register_button_register.isEnabled = false
           hacerRegistro()
                }

        cuenta_existe_text_view.setOnClickListener {
            Log.d("RegisterActivity","Try to show login activity")
            dialoglogin()
        }

        selectphoto_button.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        pb_register.visibility = View.INVISIBLE
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?){
        if (user!=null){
            val intent = Intent(this, MainActivity::class.java)
            //Limpiar anteriores actividades
            intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else{

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity", "La foto fue seleccionada")

            selectedPhotoUri = data.data

            Log.d("RegisterActivity","La foto en el onActivity result es $selectedPhotoUri")

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            Log.d("RegisterActivity","El bitmap en el onActivity result es $bitmap")


            selectphoto_imageview_register.setImageBitmap(bitmap)

            selectphoto_button.alpha = 0f

            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectphoto_button.setBackgroundDrawable(bitmapDrawable)
        }
    }



    private fun hacerRegistro(){
        val username = username_edittext_register.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val imagen = selectphoto_imageview_register.image.toString()

        if ( email.isEmpty() || password.isEmpty() || username.isEmpty()){
            pb_register.visibility = View.INVISIBLE
            register_button_register.isEnabled = true
            Toast.makeText(this,"Por favor introduce un usuario/email/contraseña", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("RegisterActivity","Email: " +email)
        Log.d("RegisterActivity","Password: $password")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    uploadImageToFirebaseStorage()
                    //updateUI(user)
                } else {
                    pb_register.visibility = View.INVISIBLE
                    register_button_register.isEnabled = true
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Fallo al crear el usuario",
                        Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                    //updateUI(null)
                }

                // ...
            }

        // ...
    }

    private fun uploadImageToFirebaseStorage(){
        Log.d("RegisterActivity", "Entra en la función")
        Log.d("RegisterActivity", "la foto es $selectedPhotoUri")
        if(selectedPhotoUri == null){
            selectedPhotoUri = Uri.parse("android.resource://net.azarquiel.proyectofinal2/drawable/avatar")
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")


        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Foto bien insertada")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "Localización del archivo: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity","no inserta foto")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid =  FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {

                Log.d("RegisterActivity", "Finalmente insertamos el usuario en la base de datos")

                val intent = Intent(this, MainActivity::class.java)
                //Limpiar anteriores actividades
                intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }


    private fun login(email:String,password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    //Limpiar anteriores actividades
                    intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Login incorrecto",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                // ...
            }
    }


    private fun dialoglogin() {
        alert {
            title = "LOGIN"
            customView {
                verticalLayout {
                    lparams(width = wrapContent, height = wrapContent)
                    val etEmail = editText {
                        hint = "Email"
                        padding = dip(16)
                        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    }
                    val etPass = editText(){
                        hint= "Contraseña"
                        padding = dip(16)
                        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    positiveButton("Login") {
                        if (etEmail.text.toString().isEmpty() || etPass.text.toString().isEmpty() )
                            toast("Rellena los campos...")
                        else
                            login(etEmail.text.toString(),etPass.text.toString())
                    }
                }
            }
        }.show()
    }


}
