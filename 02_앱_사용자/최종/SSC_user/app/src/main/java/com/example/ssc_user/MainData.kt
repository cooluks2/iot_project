package com.example.ssc_user

data class MainData(var user_id : String?=null, var datetime : String?=null, var total_price : Int?=null, val each_product : ArrayList<ProductData>?=null)