package com.example.ssc_cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.in_cart.view.*

class CartAdapter(var items: MutableList<CartData>): RecyclerView.Adapter<CartAdapter.MainViewHolder>(){


    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvCartProduct = itemView.tv_cart_product
        val tvCartNum = itemView.tv_cart_num
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.in_cart, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                tvCartProduct.text = item.product_name
                tvCartNum.text = item.product_num
            }
        }
    }
}