package com.example.ssc_user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_first.*

class CreateQr : AppCompatActivity() {
    private var iv: ImageView? = null
    private var text: String? = null

    override fun onBackPressed() {
        startActivity(Intent(this, FirstActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createqr)

        val login = intent.getStringExtra("login")

        iv = findViewById<View>(R.id.qrcode) as ImageView
        text = login
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(login, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            iv!!.setImageBitmap(bitmap)
        } catch (e: Exception) {
        }
    }
}

