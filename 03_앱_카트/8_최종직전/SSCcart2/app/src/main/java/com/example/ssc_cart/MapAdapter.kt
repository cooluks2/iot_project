package com.example.ssc_cart

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.in_map.view.*

class MapAdapter(var items: MutableList<MapData>): RecyclerView.Adapter<MapAdapter.MainViewHolder>(){
    interface ItemClick
    {
        fun onClick(view: View, position: Int)
    }
    var itemClick: ItemClick? = null

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvMapProduct = itemView.tv_map_product
        val tvMapPrice = itemView.tv_map_price
        val tvMapUrl = itemView.tv_map_url
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.in_map, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                tvMapProduct.text = item.map_name
                tvMapPrice.text = "${item.map_price.toString()}원"
                if (item.map_price == null) {
                    tvMapPrice.text = "${item.map_weith.toString()}원/100g당"
                }
                Glide.with(itemView.context).load(item.map_url.toString()).into(tvMapUrl)
                itemView.setOnClickListener{

                }
            }
        }
        if(itemClick!=null)
        {
            holder?.itemView?.setOnClickListener{ v->
                itemClick?.onClick(v,position)
            }
        }
    }
    fun search(searchWord : String, items : MutableList<MapData>) {
        FirebaseFirestore.getInstance()?.collection("Product")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            // ArrayList 비워줌
            items.clear()

            for (snapshot in querySnapshot!!.documents) {
                if (snapshot.getString("name")!!.contains(searchWord)) {
                    val product_db = snapshot.data?.toMap()
                    items.add(MapData(product_db?.get("name").toString(), product_db?.get("price") as Long?, product_db?.get("url")?.toString(), product_db?.get("weight") as Long?))
                }
            }
            notifyDataSetChanged()
        }
    }
}