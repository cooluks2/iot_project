package com.example.ssc_user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn_login.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        btn_join.setOnClickListener{
            startActivity(Intent(this, JoinActivity::class.java))
            finish()
        }

    }
}