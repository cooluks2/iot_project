package com.example.ssc_cart

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.in_cart.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class CartActivity : AppCompatActivity() {

    //    private var temp_barcode: Long? = 8801382124849
    private var temp_name: String? = null
    private var temp_price: Long? = null
    val currentDateTime = Calendar.getInstance().time
    val temp_date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

    val db = FirebaseFirestore.getInstance()

    // 리싸이클러뷰에 들어갈 아이템 객체 생성
    var items: MutableList<CartData> = mutableListOf()

    // 카테고리 DB서 받기
    var category: HashMap<String, String> = hashMapOf()

    // 영수증에 보낼 해시맵 생성
    var Purchase: HashMap<String, Long> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

    }

    override fun onStart() {
        super.onStart()
        // 값이 없으면 리턴
        val i = intent ?: return // 호출에 사용된 Intent
        val sID = i.getStringExtra(MainActivity.ID)
        tv_cart.text = "${sID} 님 안녕하세요."

        // 바코드 예시
        button_ex1.setOnClickListener { addCart(8801056154011) }
        button_ex2.setOnClickListener { addCart(8801382124849) }

        // 버튼 클릭시 디비 보내주기
        btn_godb.setOnClickListener { goDB() }
    }


    private fun addCart(temp_barcode: Long) {
        val j = intent ?: return // 호출에 사용된 Intent
        val addCartID = j.getStringExtra(MainActivity.ID)

        // 바코드로 제품명, 가격 가져오기
        db.collection("Product")
            .document(temp_barcode.toString())
            .get().addOnSuccessListener { documentSnapshot ->
                    val listlsit = documentSnapshot.data?.toMap()

                    // 품목 갱신
                    val Purchase_name = listlsit?.get("name")?.toString()
                    val Purchase_each_price = listlsit?.get("price") as Long
                    val Purchase_url = listlsit?.get("url")?.toString()

                    if (Purchase[Purchase_name] == null) {
                        items.add(CartData(Purchase_name, Purchase_each_price, 1, Purchase_each_price,Purchase_url))
                        Purchase.put(Purchase_name!!, 1)
                    } else {
                        var remove_data = CartData(Purchase_name, Purchase_each_price, Purchase[Purchase_name], Purchase[Purchase_name]!!.times(Purchase_each_price),Purchase_url)
                        items.remove(remove_data)
                        Purchase.set(Purchase_name!!, Purchase[Purchase_name]?.plus(1) as Long)
                        items.add(CartData(Purchase_name, Purchase_each_price, Purchase[Purchase_name], Purchase[Purchase_name]!!.times(Purchase_each_price),Purchase_url))
                    }
                    Log.d("품목","${Purchase}")
                    // 카트에 담긴 품목 RecylerView 활성화
                    rv_main_list.adapter = CartAdapter(items)
                    rv_main_list.layoutManager = LinearLayoutManager(this)
                }

    }


    data class PRODUCT(
            val product: String? = null,
            val price: Long? = null,
            val quantity: Long? = null,
            val weight: Long? = null
    )

    data class CART(
            val user_id: String? = null,
            val total_price: Long? = null,
            val datetime: String? = null,
            val each_product: List<PRODUCT>? = null
    )

    private fun goDB() {
        val i = intent ?: return // 호출에 사용된 Intent
        val sID = i.getStringExtra(MainActivity.ID)
        var total : Long = 0

        Log.d("영수증","${Purchase}")

        val cart = CART(sID, total, temp_date, null)
        db.collection("Purchase").document(temp_date).set(cart)

        for((a,b) in Purchase){
            db.collection("Product").whereEqualTo("name", a)
                    .get().addOnSuccessListener { documents ->
                        for (document in documents) {
                            total = total + b*document.data.toMap().get("price") as Long

                            db.collection("Purchase").document(temp_date).update("total_price", total)
                            db.collection("Purchase").document(temp_date).update("each_product", FieldValue.arrayUnion(PRODUCT(document.data.toMap().get("name") as String?, document.data.toMap().get("price") as Long?, b , document.data.toMap().get("quantity") as Long?)))
                        }
                    }
        }
    }
}