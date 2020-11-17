package com.example.ssc_user

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class JoinActivity : AppCompatActivity() {
    var thread = PassChk()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        thread.start()
        register_button.setOnClickListener{
            performRegister()
        }

        already_have_account_text_view.setOnClickListener{
            Log.d("JoinActivity", "Try to show login activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        select_photo_btn.setOnClickListener {
            Log.d("Join", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("Join", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            circleImageView.setImageBitmap(bitmap)
            select_photo_btn.alpha=0f

        }
    }

    private fun performRegister(){

        val email = join_id.text.toString()
        val password = join_pw.text.toString()
        var intent = Intent(this, FirstActivity::class.java)

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }
        if (!email.contains("@")&&email.length<6){
            var toast = Toast.makeText(this,"이메일 형식이 맞지 않습니다",Toast.LENGTH_SHORT)
            toast.show()
        }

        Log.d("JoinActivity", "Email is: "+ email)
        Log.d("JoinActivity", "Password:  $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener
                //else if succesful
                Log.d("Join", "Successfully created user")

                uploadImageToFirebaseStorage()
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("Join", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Join", "Successfully upload image")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Join", "File Location: $it")

                    saveUerToFireDatabase(it.toString())
                }
            }
            .addOnFailureListener {

            }
    }
    private fun saveUerToFireDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, join_name.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Join", "Finally saved the user to Firebase Database")
            }
    }

    inner class PassChk:Thread(){
        override fun run() {
            while (true){
                SystemClock.sleep(1000)
                var pass1:String = join_pw.text.toString()
                var pass2:String = join_pw2.text.toString()
                if (pass1.equals(pass2)){
                    runOnUiThread{
                        register_button.setEnabled(true)
                    }
                }else{
                    runOnUiThread{
                        tvError.setText("")
                        register_button.setEnabled(false)
                    }
                }
                if (pass1.length<6){
                    runOnUiThread {
                        tvError.setText("비밀번호는 6자 이상이여야 합니다")
                    }
                }
            }
        }
    }
}
class User(val uid: String, val username: String, val profileImageUrl: String)