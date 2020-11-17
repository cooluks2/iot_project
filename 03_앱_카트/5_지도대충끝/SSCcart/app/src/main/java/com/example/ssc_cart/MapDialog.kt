package com.example.ssc_cart

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.dialog_map.*
import com.example.ssc_cart.CartActivity

class MapDialog(ctx : Context, val a : Context) : Dialog( ctx ) {

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
        setContentView(R.layout.dialog_map)


        // 디비 전체 가져오기

        val db = FirebaseFirestore.getInstance()
        db.collection("Product")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val product_db = document.data.toMap()
                        map_items.add(MapData(product_db?.get("name").toString(), product_db?.get("price") as Long?, product_db?.get("url")?.toString()))
                        all_product_db.put(product_db?.get("name")?.toString()!!, MapDialog.ALL_PRODUCT(
                                product_db?.get("price") as Long?,
                                product_db?.get("name")?.toString(),
                                product_db?.get("weight") as Long?,
                                product_db?.get("location")?.toString(),
                                product_db?.get("category")?.toString(),
                                product_db?.get("url")?.toString()
                        ))
                    }
                    Log.d("접근", "${map_items}")
                    val adapter = MapAdapter(map_items)
                    adapter.itemClick = object : MapAdapter.ItemClick{
                        override fun onClick(view: View, position: Int) {
//                            Toast.makeText(context, map_items[position].getname(), Toast.LENGTH_SHORT).show()
                            val location_map = all_product_db[map_items[position].getname()]?.getlocation().toString()
                            val dlg = ImageDialog(a, location_map)
                            Toast.makeText(context, "${location_map} 구역 ${all_product_db[map_items[position].getname()]?.getcategory().toString()} 칸에 있습니다.", Toast.LENGTH_SHORT).show()
                            dlg.show()
                        }
                    }
                    rv_map.adapter = adapter
                    rv_map.layoutManager = GridLayoutManager(a, 5)

                }
                .addOnFailureListener { exception ->
                    Log.d("product DB 전체 가져오기 실패!", "Error getting documents: ", exception)
                }
        tv_test.text = "제품을 선택해주세요."


    }
}

