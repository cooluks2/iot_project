package com.example.ssc_cart

data class MapData (val map_name: String?, val map_price: Long?, val map_url: String?, val map_weith: Long?) {
    fun getname() : String? {return map_name}
}