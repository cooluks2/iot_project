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
//    open var dayString = ""
    var map_items: MutableList<MapData> = mutableListOf()

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
                    }
                    Log.d("접근", "${map_items}")
                    val adapter = MapAdapter(map_items)
                    adapter.itemClick = object : MapAdapter.ItemClick{
                        override fun onClick(view: View, position: Int) {
                            Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
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

