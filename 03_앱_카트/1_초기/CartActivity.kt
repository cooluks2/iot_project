package com.example.ssc_cart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*



class CartActivity : AppCompatActivity() {

    private var temp_barcode: Long? = 8801382124849
    private var temp_name: String? = null
    private var temp_price : Long? = null
    val currentDateTime = Calendar.getInstance().time
    val temp_date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

    val db = FirebaseFirestore.getInstance()

    var items: MutableList<CartData> = mutableListOf(
            CartData("밀키스", "1"),
            CartData("콜라", "2"),
            CartData("사이다", "4"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // 카트에 담긴 품목 RecylerView 활성화
        rv_main_list.adapter = CartAdapter(items)
        rv_main_list.layoutManager = LinearLayoutManager(this)
    }
    override fun onStart() {
        super.onStart()
        // 값이 없으면 리턴
        val i = intent ?: return // 호출에 사용된 Intent
        val sID = i.getStringExtra(MainActivity.ID)
        tv_cart.text = "아이디: ${sID}"


        // 버튼 클릭시 디비 보내주기
        btn_godb.setOnClickListener {addCart()}
    }

    private fun addAdaLovelaceex() {
        val i = intent ?: return // 호출에 사용된 Intent
        val sID = i.getStringExtra(MainActivity.ID)
        val godbgodb = hashMapOf(
            "ID" to "${sID}",
            "제품명" to "${tv_productname.text}",
            "수량" to "${tv_productnum.text}"
        )

        // Add a new document with a generated ID
        db.collection("users")
            .add(godbgodb)
        // [END add_alan_turing]

        Toast.makeText(this, "넘어가라", Toast.LENGTH_SHORT).show()
    }

    private fun addCart() {
        val j = intent ?: return // 호출에 사용된 Intent
        val addCartID = j.getStringExtra(MainActivity.ID)

        // 바코드로 제품명 가져오기
        db.collection("Product")
                .whereEqualTo("barcode", temp_barcode)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Log.d("제발", "${document.data.toMutableMap()["name"]} => ${document.data}")
                        temp_name = document.data.toMutableMap()["name"].toString()
                        temp_price = document.data.toMutableMap()["price"] as Long
                    }
                }
                .addOnFailureListener { exception ->
                    // Log.d("정말", "Error getting documents: ", exception)
                    Toast.makeText(this, "DB서 가져오기 오류", Toast.LENGTH_SHORT).show()
                }

        // 바코드 신호가 들어오면 DB Cart에 저장 !!!!!!!!!!!!!!!!!!!!!! 신호 받으면 바꿔야한다!!!!!!!!!
        data class GoCART(var name:String? = null, var user_id:String? = null, var price:Long? =0, var date:String? = null)

        if (temp_barcode != null){ // 바꿔야함!!!
            var goCART = GoCART(name = temp_name, user_id = addCartID, price = temp_price, date = temp_date)
            db.collection("Cart").document().set(goCART)
        }



        // PRODUCT
        var getBARCODE : Int = 0
        var getPRODUCT : String? = ""
        var getNAME : String? = ""
        var getCATEGORY : String? = ""
        var getLOCATION : String? = ""
        var getPRICE : Int = 0
        var getWEIGHT : Int = 0
        var getIMAGEURL : String? = ""
//
//
//        var addCartQUANTITY = 0
//        val addCartPRICE = 0 // 바코드값을 읽어서
//
//
//        var addCartTOTAL = 0
//        var addCartWEIGHT = 0
//
//        var toggleSignalRS = 0



    }

}