package com.example.ssc_cart

data class CartData(val product_name: String?, val product_price: Long?, val product_num: Long?, val product_total: Long?, val product_url: String?){
    fun getname() : String? {return product_name}
    fun total() : Long? {return product_total}
}