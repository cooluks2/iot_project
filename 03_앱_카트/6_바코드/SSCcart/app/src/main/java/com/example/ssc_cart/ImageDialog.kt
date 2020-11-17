package com.example.ssc_cart


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.in_image.*

class ImageDialog(ctx : Context, val location_map: String) : Dialog( ctx ) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_image)

        if (location_map == 'A'.toString()) {img_id.setImageResource(R.drawable.a)}
        else if (location_map == 'B'.toString()) {img_id.setImageResource(R.drawable.b)}
        else if (location_map == 'C'.toString()) {img_id.setImageResource(R.drawable.c)}
        else if (location_map == 'D'.toString()) {img_id.setImageResource(R.drawable.d)}
        else if (location_map == 'E'.toString()) {img_id.setImageResource(R.drawable.e)}
        else if (location_map == 'F'.toString()) {img_id.setImageResource(R.drawable.f)}
        else if (location_map == 'G'.toString()) {img_id.setImageResource(R.drawable.g)}
        else {img_id.setImageResource(R.drawable.defaultt)}

    }
}