package com.example.ssc_cart

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.dialog_map.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap



class CartActivity : AppCompatActivity() {
    //    private var temp_barcode: Long? = 8801382124849
    private var temp_name: String? = null
    private var temp_price: Long? = null
    val currentDateTime = Calendar.getInstance().time
    val temp_date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

    val db = FirebaseFirestore.getInstance()

    // 리싸이클러뷰에 들어갈 아이템 객체 생성
    var items: MutableList<CartData> = mutableListOf()

    // 최종 금액
    var all_total: Long? = 0

    // 영수증에 보낼 해시맵 생성 key = name, value = information
    var Purchase: HashMap<String, Long> = hashMapOf()
    var map_items: MutableList<MapData> = mutableListOf()

    data class ALL_PRODUCT(
            val price: Long? = null,
            val name: String? = null,
            val weight: Long? = null,
            val location: String? = null,
            val category: String? = null,
            val url: String? = null
    ) { fun getname(name2: String): String? { return name }
        fun getprice(name2: String): Long? { return price }
        fun getweight(name2: String): Long? { return weight }
        fun getlocation(name2: String): String? { return location }
        fun getcategory(name2: String): String? { return category }
        fun geturl(name2: String): String? { return url }
    }

    // 모든 제품 정보 담김 해시맵 생성
    var all_product_db: HashMap<String, ALL_PRODUCT> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // 품목 찾기
        button_map.setOnClickListener {
            val dlg = MapDialog(this, this)
            dlg.setOnDismissListener {
                Toast.makeText(this,"되나요", Toast.LENGTH_LONG).show()
            }
            dlg.show()
        }
    }


    override fun onStart() {
        super.onStart()
        // 값이 없으면 리턴
        val i = intent ?: return // 호출에 사용된 Intent
        val sID = i.getStringExtra(MainActivity.ID)
        tv_cart.text = "${sID} 님 안녕하세요."

        // 디비 전체 가져오기
        db.collection("Product")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val product_db = document.data.toMap()
                        map_items.add(MapData(product_db?.get("name").toString(), product_db?.get("price") as Long?, product_db?.get("url")?.toString()))
                        all_product_db.put(product_db?.get("name")?.toString()!!, CartActivity.ALL_PRODUCT(
                                product_db?.get("price") as Long?,
                                product_db?.get("name")?.toString(),
                                product_db?.get("weight") as Long?,
                                product_db?.get("location")?.toString(),
                                product_db?.get("category")?.toString(),
                                product_db?.get("url")?.toString()
                        ))
                    }


                }
                .addOnFailureListener { exception ->
                    Log.d("product DB 전체 가져오기 실패!", "Error getting documents: ", exception)
                }


        // 바코드 예시
        button_ex1.setOnClickListener { addCart(8801056154011) }
        button_ex2.setOnClickListener { addCart(8801382124849) }
        button_ex3.setOnClickListener { addCart(1567866545655) }
        button_ex4.setOnClickListener { addCart(1567866545654) }




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
                    val Purchase_each_price = listlsit?.get("price") as Long?
                    val Purchase_weight = listlsit?.get("weight") as Long?
                    val Purchase_url = listlsit?.get("url")?.toString()

                    if (Purchase[Purchase_name] == null) {
                        if(Purchase_each_price != null) {
                            // 무게를 재는 물품이 아닐 때
                            items.add(CartData(Purchase_name, Purchase_each_price, 1, Purchase_each_price, Purchase_url))
                            Purchase.put(Purchase_name!!, 1)
                            all_total = all_total?.plus(Purchase_each_price!!)
                        } else {
                            // 무게를 재는 물품일 때
                            items.add(CartData(Purchase_name, Purchase_weight, 1, Purchase_weight, Purchase_url))
                            Purchase.put(Purchase_name!!, 1)
                            all_total = all_total?.plus(Purchase_weight!!)
                        }
                    } else {
                        if(Purchase_each_price != null) {
                            // 무게를 재는 물품이 아닐 때
                            var remove_data = CartData(Purchase_name, Purchase_each_price, Purchase[Purchase_name], Purchase[Purchase_name]!!.times(Purchase_each_price), Purchase_url)
                            items.remove(remove_data)
                            Purchase.set(Purchase_name!!, Purchase[Purchase_name]?.plus(1) as Long)
                            items.add(CartData(Purchase_name, Purchase_each_price, Purchase[Purchase_name], Purchase[Purchase_name]!!.times(Purchase_each_price), Purchase_url))
                            all_total = all_total?.plus(Purchase_each_price!!)
                        } else {
                            // 무게를 재는 물품일 때
                            var remove_data = CartData(Purchase_name, Purchase_weight, Purchase[Purchase_name], Purchase[Purchase_name]!!.times(Purchase_weight!!), Purchase_url)
                            items.remove(remove_data)
                            Purchase.set(Purchase_name!!, Purchase[Purchase_name]?.plus(1) as Long)
                            items.add(CartData(Purchase_name, Purchase_weight, Purchase[Purchase_name], Purchase[Purchase_name]!!.times(Purchase_weight!!), Purchase_url))
                            all_total = all_total?.plus(Purchase_weight!!)
                        }

                    }
//                    Log.d("품목","${all_total}")
//                    Log.d("품목","${Purchase}")
                    // 카트에 담긴 품목 RecylerView 활성화
                    tv_all_total.text = "최종 금액 ${all_total}"
                    rv_main_list.adapter = CartAdapter(items)
//                    rv_main_list.adapter = MapAdapter(map_items)
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
                            if (document.data.toMap().get("price") != null){
                                total = total + b * document.data.toMap().get("price") as Long
                                db.collection("Purchase").document(temp_date).update("total_price", total)
                                db.collection("Purchase").document(temp_date).update("each_product", FieldValue.arrayUnion(PRODUCT(document.data.toMap().get("name") as String?, document.data.toMap().get("price") as Long?, b, null)))
                            } else {
                                total = total + b * document.data.toMap().get("weight") as Long
                                db.collection("Purchase").document(temp_date).update("total_price", total)
                                db.collection("Purchase").document(temp_date).update("each_product", FieldValue.arrayUnion(PRODUCT(document.data.toMap().get("name") as String?, document.data.toMap().get("weight") as Long?, null, b*100)))
                            }
                        }
                    }
        }
    }






}