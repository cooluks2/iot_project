package com.example.ssc_cart

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.in_recom.view.*

class RecomAdapter(var items: MutableList<MapData>): RecyclerView.Adapter<RecomAdapter.MainViewHolder>(){
    interface ItemClick
    {
        fun onClick(view: View, position: Int)
    }
    var itemClick: ItemClick? = null

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvMapProduct = itemView.tv_map_product2
        val tvMapPrice = itemView.tv_map_price2
        val tvMapUrl = itemView.tv_map_url2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.in_recom, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                tvMapProduct.text = item.map_name
                tvMapPrice.text = "${item.map_price.toString()}원"
                if (item.map_price == null) {
                    tvMapPrice.text = "${item.map_weith.toString()}원 / 100g당"
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
//    fun search(searchWord : String, items : MutableList<MapData>) {
//        firestore?.collection("telephoneBook")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//            // ArrayList 비워줌
//            telephoneBook.clear()
//
//            for (snapshot in querySnapshot!!.documents) {
//                if (snapshot.getString(option)!!.contains(searchWord)) {
//                    var item = snapshot.toObject(Person::class.java)
//                    telephoneBook.add(item!!)
//                }
//            }
//            notifyDataSetChanged()
//        }
//    }
}