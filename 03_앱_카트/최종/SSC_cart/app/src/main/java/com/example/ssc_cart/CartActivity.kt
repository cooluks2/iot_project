package com.example.ssc_cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

const val SUB_TOPIC = "iot/and"
const val PUB_TOPIC = "iot/app"
const val SERVER_URI = "tcp://192.168.0.127:1883"

class CartActivity : AppCompatActivity() {

    // MQTT
    val TAG = "MqttActivity"
    lateinit var mqttClient: Mqtt
    // MQTT 끝끝


    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var mDatabase: DatabaseReference

    val currentDateTime = Calendar.getInstance().time
    val temp_date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

    val db = FirebaseFirestore.getInstance()

    // 리싸이클러뷰에 들어갈 아이템 객체 생성
    var items: MutableList<CartData> = mutableListOf()

    // 최종 금액
    var all_total: Long? = 0

    // 무게 체크
    var weight_1: Int? = -1000
    var weight_2: Int? = -1000
    var weight_3: Int? = -1000
    var weight_name: String? = ""
    var abc : Long = 0

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
    ) { fun getname(): String? { return name }
        fun getprice(): Long? { return price }
        fun getweight(): Long? { return weight }
        fun getlocation(): String? { return location }
        fun getcategory(): String? { return category }
        fun geturl(): String? { return url }
    }

    // 모든 제품 정보 담김 해시맵 생성
    var all_product_db: HashMap<String, ALL_PRODUCT> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // MQTT
        mqttClient = Mqtt(this, SERVER_URI)
        try {
            // mqttClient.setCallback { topic, message ->}
            mqttClient.setCallback(::onReceived)
            mqttClient.connect(arrayOf<String>(SUB_TOPIC))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // MQTT 끝


        // 품목 찾기
        // 품목 찾기
        button_map.setOnClickListener {
//            val dlg = MapDialog(this, this)
//            dlg.setOnDismissListener {
//                Toast.makeText(this,"위치 검색을 종료합니다.", Toast.LENGTH_LONG).show()
//            }
//            dlg.show()

            val nextIntent = Intent(this, MapDialogActivity::class.java)
            startActivity(nextIntent)
            Log.d("넘어가라", "MapDialog")
        }

        button_weight.setOnClickListener {
            val dlg2 = MapDialog(this, this)
            dlg2.setOnDismissListener {
//                Toast.makeText(this, "위치 검색을 종료합니다.", Toast.LENGTH_LONG).show()
            }
            dlg2.show()
        }

    }


    override fun onStart() {
        super.onStart()

        mDatabase = FirebaseDatabase.getInstance().getReference("User")
        val uid = mAuth.currentUser!!.uid
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef : DatabaseReference = database.getReference("users/$uid/username")
        val myUri : DatabaseReference = database.getReference("users/$uid/profileImageUrl")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot?.value
                tv_cart.setText("${value} 님 환영합니다.")
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value")
            }
        })

        myUri.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot?.value

                Glide.with(this@CartActivity).load("$value")
                        .into(circleProfileView)
            }
            override fun onCancelled(p0: DatabaseError) {
                println("Failed to read value")
            }
        })

        // 디비 전체 가져오기
        db.collection("Product")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val product_db = document.data.toMap()
                        map_items.add(MapData(product_db?.get("name").toString(), product_db?.get("price") as Long?, product_db?.get("url")?.toString(), product_db?.get("weight") as Long?))
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


//        timer(period = 500, initialDelay = 500){mqttClient.publish(PUB_TOPIC, "barcode")}


        // 바코드 예시
        button_ex1.setOnClickListener { addCart(8801056154011) }
        button_ex2.setOnClickListener { addCart(8801382124849) }
        button_ex3.setOnClickListener { addCart(1567866545655) }
        button_ex4.setOnClickListener { addCart(1567866545654) }




        // 버튼 클릭시 디비 보내주기
        btn_godb.setOnClickListener {
            ankoAlertDialog()
        }





    }


    private fun addCart(temp_barcode: Long): Boolean {

        Log.d("길이","${temp_barcode.toString().length}")
        if (temp_barcode.toString().length != 13) {
            // Toast.makeText(this, "등록된 바코드가 없습니다.", Toast.LENGTH_LONG).show()
            return false
        }

        // 바코드로 제품명, 가격 가져오기
        db.collection("Product")
                .document(temp_barcode.toString())
                .get().addOnSuccessListener { documentSnapshot ->
                    val listlsit = documentSnapshot.data?.toMap()

                    // 품목 갱신
                    val Purchase_name = listlsit?.get("name")?.toString()
                    Toast.makeText(this, "${Purchase_name} 추가", Toast.LENGTH_LONG).show()
                    val Purchase_each_price = listlsit?.get("price") as Long?
                    val Purchase_weight = listlsit?.get("weight") as Long?
                    val Purchase_url = listlsit?.get("url")?.toString()

                    if (Purchase[Purchase_name] == null) {
                        if (Purchase_each_price != null) {
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
                        if (Purchase_each_price != null) {
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

                    // 카트에 담긴 품목 RecylerView 활성화
                    tv_all_total.text = "최종 금액 ${all_total} 원"

                    val adapter = CartAdapter(items)
                    adapter.itemLongClick = object : CartAdapter.ItemLongClick {
                        override fun onClick(view: View, position: Int): Boolean {
                            alert("확인 버튼을 클릭하면 상품이 제외됩니다.", "선택한 상품을 장바구니에서 제외하겠습니까?") {
                                yesButton {
                                    Toast.makeText(
                                        this@CartActivity,
                                        "${items[position].getname()}를 장바구니에서 제외합니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    all_total = all_total?.minus(items[position].total()!!)
                                    tv_all_total.text = "최종 금액 ${all_total} 원"
                                    Purchase.remove(items[position].getname()!!)
                                    items.removeAt(position)
                                    adapter.notifyDataSetChanged()
                                }
                                noButton {}
                            }.show()
                            return false
                        }
                    }
                    rv_main_list.adapter = adapter
                    rv_main_list.layoutManager = LinearLayoutManager(this)


                    // 추천 상품의 영역
                    var recom_products: MutableList<MapData> = mutableListOf()
                    var cate = all_product_db[Purchase_name]?.getcategory()
                    var cate_name = all_product_db[Purchase_name]?.getname()
                    for (all_pro in all_product_db.values) {
                        if (all_pro.getcategory() == cate) {
                            if (all_pro.getname() != cate_name){
                            recom_products.add(MapData(all_pro?.getname(), all_pro?.getprice(), all_pro?.geturl(), all_pro?.getweight()))}
                        }
                    }
                    rv_sub_list.adapter = RecomAdapter(recom_products)
                    rv_sub_list.layoutManager = GridLayoutManager(this,2)


                }
        return false


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
                                total = total + b
                                db.collection("Purchase").document(temp_date).update("total_price", total)
                                db.collection("Purchase").document(temp_date).update("each_product", FieldValue.arrayUnion(PRODUCT(document.data.toMap().get("name") as String?, b, 1, document.data.toMap().get("weight"))))
                            }
                        }
                    }
        }
    }

    // MQTT
    fun onReceived(topic: String, message: MqttMessage) {
        // 토픽 수신 처리
        val msg = String(message.payload)
        Log.d("엠큐티티", "${msg}")
        Log.d("엠큐티티3", "${(msg == "ready") as Boolean}")
        if (msg == "ready") {
            Log.d("엠큐티티2", "${msg}")
            Timer().schedule(1000) {
                mqttClient.publish(PUB_TOPIC, "barcode")
            }
        }
        if (msg.length == 13) {
            addCart(msg.toLong()!!)
        } else if (weight_1 == -1000) {
            weight_1 = msg.toInt()!!
        } else if (weight_2 == -1000){
            weight_2 = msg.toInt()!!
            weight_3 = weight_1?.let { weight_2!!.minus(it) }
            addCartWeight(weight_3!!, weight_name)
            weight_1 = -1000
            weight_2 = -1000
        } else {}
    }
    // MQTT 끝

    private fun ankoAlertDialog() {
        alert("SSC를 이용해주셔서 감사합니다.", "계산을 완료하시겠습니까?") {
            yesButton {
                goDB()
                toast("계산이 완료되었습니다.")

                val nextIntent2 = Intent(this@CartActivity, MainActivity::class.java)
                startActivity(nextIntent2)
            }
            noButton {}
        }.show()
    }

    fun ankoWeightAlertDialog(name: String?) {
        weight_name = name
        alert("확인 버튼을 누르고 제품을 올려주세요.", "${name} 무게를 측정하겠습니다.") {
            yesButton {
                Timer().schedule(1000) {
                    mqttClient.publish(PUB_TOPIC, "weight")
                }
                alert("확인 버튼을 누르면 무게 측정이 완료됩니다..", "${name} 무게 측정중입니다.") {
                    yesButton {
                        Timer().schedule(1000) {
                            mqttClient.publish(PUB_TOPIC, "weight2")
                        }
                        toast("무게 측정이 완료되었습니다.")}
                    noButton {}
                }.show()

                toast("무게를 측정합니다.")}
            noButton {}
        }.show()
    }



     private fun addCartWeight(weight_result: Int, weight_name:String?) : Boolean {



         if (Purchase[weight_name] == null) {
                 // 무게를 재는 물품일 때
                 items.add(CartData(weight_name, all_product_db[weight_name]?.getweight()!!, weight_result?.toLong(), ((all_product_db[weight_name]?.getweight()
                     ?.times(weight_result?.toLong()))?.div(100)), all_product_db[weight_name]?.geturl()!!))
                 Log.d("엠큐티티 첫 아이템", "${CartData(weight_name, all_product_db[weight_name]?.getweight()!!, weight_result?.toLong(), ((all_product_db[weight_name]?.getweight()
                 ?.times(weight_result?.toLong()))?.div(100)), all_product_db[weight_name]?.geturl()!!)}")
                 toast("${weight_result}g이 추가되었습니다.")
                 Purchase.put(weight_name.toString(), ((all_product_db[weight_name]?.getweight()?.times(weight_result?.toLong()))?.div(100))!!)
                 abc = weight_result?.toLong()
                 all_total = all_total?.plus((all_product_db[weight_name]?.getweight()?.times(weight_result?.toLong()))?.div(100)!! as Long)
         } else {
                 // 무게를 재는 물품일 때
                 var remove_data = CartData(weight_name, all_product_db[weight_name]?.getweight()!!, abc, Purchase[weight_name], all_product_db[weight_name]?.geturl()!!)
                 Log.d("엠큐티티 두번째 이후 아이템", "${remove_data}")
                 items.remove(remove_data)
                 abc = abc?.plus(weight_result)
                 toast("${weight_result}g이 추가되었습니다.")
                 Purchase.set(weight_name.toString(), all_product_db[weight_name]?.getweight()?.times(abc)?.div(100)!!)
                 items.add(CartData(weight_name, all_product_db[weight_name]?.getweight()!!, abc, all_product_db[weight_name]?.getweight()?.times(abc)?.div(100)!!, all_product_db[weight_name]?.geturl()!!))
                 all_total = all_total?.plus((all_product_db[weight_name]?.getweight()?.times(weight_result?.toLong()))?.div(100)!!)

         }


         // 카트에 담긴 품목 RecylerView 활성화
         tv_all_total.text = "최종 금액 ${all_total} 원"


//         val adapter = CartAdapter(items)
//         adapter.itemLongClick = object : CartAdapter.ItemLongClick {
//             override fun onClick(view: View, position: Int): Boolean {
//                 alert("확인 버튼을 클릭하면 상품이 제외됩니다.", "선택한 상품을 장바구니에서 제외하겠습니까?") {
//                     yesButton {
//                         Toast.makeText(
//                                 this@CartActivity,
//                                 "${items[position].getname()}를 장바구니에서 제외합니다.",
//                                 Toast.LENGTH_SHORT
//                         ).show()
//                         all_total = all_total?.minus(items[position].total()!!)
//                         tv_all_total.text = "최종 금액 ${all_total} 원"
//                         Purchase.remove(items[position].getname()!!)
//                         items.removeAt(position)
//                         adapter.notifyDataSetChanged()
//                     }
//                     noButton {}
//                 }.show()
//                 return false
//             }
//         }
//         rv_main_list.adapter = adapter
//         rv_main_list.layoutManager = LinearLayoutManager(this)
//
         val adapter = CartAdapter(items)
         rv_main_list.adapter = adapter
         rv_main_list.layoutManager = LinearLayoutManager(this)

         // 추천 상품의 영역
         var recom_products: MutableList<MapData> = mutableListOf()
         var cate = all_product_db[weight_name]?.getcategory()
         var cate_name = all_product_db[weight_name]?.getname()
         for (all_pro in all_product_db.values) {
             if (all_pro.getcategory() == cate) {
                 if (all_pro.getname() != cate_name){
                     recom_products.add(MapData(all_pro?.getname(), all_pro?.getprice(), all_pro?.geturl(), all_pro?.getweight()))}
             }
         }
         rv_sub_list.adapter = RecomAdapter(recom_products)
         rv_sub_list.layoutManager = GridLayoutManager(this,2)


         return false
    }

}