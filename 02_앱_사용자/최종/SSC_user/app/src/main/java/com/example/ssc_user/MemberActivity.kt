package com.example.ssc_user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_member.*
import kotlinx.android.synthetic.main.item_receipt.view.*
import java.util.*


class MemberActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    var user = mAuth.currentUser?.email.toString()
    private lateinit var mDatabase: DatabaseReference
    var firestore: FirebaseFirestore? = null

    override fun onBackPressed() {
        startActivity(Intent(this, FirstActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)
        mDatabase = FirebaseDatabase.getInstance().getReference("User")

        edit_button.setOnClickListener{
            performEdit()
        }
        edit_photo_btn.setOnClickListener {
            Log.d("Member", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent, 0)
        }

        val uid = mAuth.currentUser!!.uid
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef : DatabaseReference = database.getReference("users/$uid/username")
        val myUri : DatabaseReference = database.getReference("users/$uid/profileImageUrl")

        firestore = FirebaseFirestore.getInstance()
    }

    private fun performEdit(){

        val editname = edit_name.text.toString()
        var intent = Intent(this, FirstActivity::class.java)

        uploadImageToFirebaseStorage()
        startActivity(intent)
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("Member", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            editPhotoView.setImageBitmap(bitmap)
            edit_photo_btn.alpha=0f

        }
    }

    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Member", "Successfully upload image")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Member", "File Location: $it")

                    saveUerToFireDatabase(it.toString())
                }
            }
            .addOnFailureListener {

            }
    }

    private fun saveUerToFireDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, edit_name.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Member", "Finally saved the user to Firebase Database")
            }
    }



}