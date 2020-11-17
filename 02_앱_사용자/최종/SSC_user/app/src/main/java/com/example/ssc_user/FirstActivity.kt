package com.example.ssc_user

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.TextKeyListener.clear
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
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.item_receipt.view.*
import java.lang.reflect.Member
import android.text.method.TextKeyListener.clear
import android.util.Base64
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.kakao.util.helper.Utility.getPackageInfo
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class FirstActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    var user = mAuth.currentUser?.email.toString()

    private lateinit var mDatabase: DatabaseReference

    var firestore: FirebaseFirestore? = null

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        mDatabase = FirebaseDatabase.getInstance().getReference("User")
        auth = FirebaseAuth.getInstance()

        val uid = mAuth.currentUser!!.uid
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef : DatabaseReference = database.getReference("users/$uid/username")
        val myUri : DatabaseReference = database.getReference("users/$uid/profileImageUrl")

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot?.value
                first_name.setText("${value} 님")
            }

            override fun onCancelled(p0: DatabaseError) {
                println("Failed to read value")
            }
        })

        myUri.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot?.value

                if (this@FirstActivity.isFinishing())
                    return

                Glide.with(this@FirstActivity).load("$value")
                    .into(circleProfileView)
            }
            override fun onCancelled(p0: DatabaseError) {
                println("Failed to read value")
            }
        })
        first_name.setOnClickListener{
            startActivity(Intent(this, MemberActivity::class.java))
            finish()
        }
        first_qr.setOnClickListener{
            startActivity(Intent(this, CreateQr::class.java))
            finish()

            val id_test = intent.getStringExtra("email")
            val pw_test = intent.getStringExtra("password")
            val login = ("{\"ID\":\"$id_test\",\"PW\":\"$pw_test\"}")

            val nextIntent = Intent(this, CreateQr::class.java)
            nextIntent.putExtra("login", login)
            startActivity(nextIntent)
        }

        btn_logout.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            auth.signOut()
            //로그인 활성화 - 이걸 더 효율적으로 하는 방법이 있을것 같은데 일일히 적어 줘야 해?


            finish()

//            bt_create.isEnabled = true
        }

        firestore = FirebaseFirestore.getInstance()
        recyclerview.adapter = RecyclerViewAdapter()
        recyclerview.layoutManager = LinearLayoutManager(this)

        fun click(){
            startActivity(Intent(this, JoinActivity::class.java))
            finish()
        }


        var searchOption = "datetime"
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (spinner.getItemAtPosition(position)) {
                    "날짜" -> {
                        searchOption = "datetime"
                    }
                }
            }
        }
        searchBtn.setOnClickListener {
            (recyclerview.adapter as RecyclerViewAdapter).search(search_word.text.toString(),searchOption)
            Log.d("버튼", search_word.text.toString())
            Log.d("버튼", searchOption)

        }
    }


    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var Purchase : ArrayList<MainData> = arrayListOf()

        init {
            firestore?.collection("Purchase")
                    ?.whereEqualTo("user_id", "$user")
                    ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        Purchase.clear()

                        for (snapshot in querySnapshot!!.documents) {
                            var item = snapshot.toObject(MainData::class.java)
                            Purchase.add(item!!)
                            Log.d("이거", "{$item")
                            var item1 = snapshot.data?.get("each_product")
                            Log.d("each", "${item1}")
                        }
                        notifyDataSetChanged()
                    }
        }

        // xml파일을 inflate하여 ViewHolder를 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_receipt, parent, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as ViewHolder).itemView
            val click = Purchase[position].datetime.toString()
            val total = Purchase[position].total_price.toString()
            Log.d("click", "$click")
            viewHolder.price.text = Purchase[position].datetime


            viewHolder.setOnClickListener(View.OnClickListener() {
                Log.d("click", "$click")
                val intent = Intent(this@FirstActivity, ReceiptActivity::class.java)
                intent.putExtra("datetime", click)
                intent.putExtra("total", total)
                startActivity(intent)
            })

        }

        // 리사이클러뷰의 아이템 총 개수 반환
        override fun getItemCount(): Int {
            return Purchase.size
        }


        fun search(searchWord : String, option : String) {
            firestore?.collection("Purchase")
                    ?.whereEqualTo("user_id", "$user")
                    ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                Purchase.clear()

                for (snapshot in querySnapshot!!.documents) {
                    if (snapshot.getString(option)!!.contains(searchWord)) {
                        var item = snapshot.toObject(MainData::class.java)
                        Log.d("스피너", "$item")
                        Purchase.add(item!!)
                    }
                }
                notifyDataSetChanged()
            }
        }
    }
}