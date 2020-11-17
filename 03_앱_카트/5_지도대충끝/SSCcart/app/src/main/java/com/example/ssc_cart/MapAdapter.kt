package com.example.ssc_cart

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
                tvMapPrice.text = item.map_price.toString()
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
}