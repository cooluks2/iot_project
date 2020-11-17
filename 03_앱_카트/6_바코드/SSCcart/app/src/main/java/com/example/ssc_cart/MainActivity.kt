package com.example.ssc_cart


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    companion object {
        val ID = "ID"

        // 비밀번호 정규식
        private val PASSWORD_PATTERN: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$")
    }

    // 파이어베이스 인증 객체 생성
    private var firebaseAuth: FirebaseAuth? = null



    //view Objects (QR)
    private var btn_login: Button? = null


    //qr code scanner object (QR)
    private var qrScan: IntentIntegrator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance()


        //View Objects (QR)
        btn_login = findViewById<View>(R.id.btn_login) as Button



        //intializing scan object (QR)
        qrScan = IntentIntegrator(this)

        //button onClick (QR)
        btn_login!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //scan option
                qrScan!!.setCameraId(1)
                qrScan!!.setPrompt("Scanning...")
                //qrScan.setOrientationLocked(false);
                qrScan!!.initiateScan()
            }
        })
    }


    // 로그인
    private fun loginUser(email: String, password: String) {
        firebaseAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공
                        Toast.makeText(this@MainActivity, R.string.success_login, Toast.LENGTH_SHORT).show()
                        // 로그인 성공시 CartActivity로 이동 & ID 넘겨줌
                        val i = Intent(this, CartActivity::class.java)
                        i.putExtra("ID", email)
                        Log.d("아이디", "${email}")
                        startActivityForResult(i, 0)
                    } else {
                        // 로그인 실패
                        Toast.makeText(this@MainActivity, R.string.failed_login, Toast.LENGTH_SHORT).show()
                    }
                }
    }

    //Getting the scan results (QR)
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            //qrcode 가 없으면
            if (result.contents == null) {
                Toast.makeText(this@MainActivity, "취소!", Toast.LENGTH_SHORT).show()
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(this@MainActivity, "스캔완료!", Toast.LENGTH_SHORT).show()
                try {
                    //data를 json으로 변환
                    val obj = JSONObject(result.contents)

                    loginUser(obj.getString("ID"),obj.getString("PW"))



                } catch (e: JSONException) {
                    e.printStackTrace()
                    //Toast.makeText(MainActivity.this, result.getContents(), Toast.LENGTH_LONG).show();
                    textViewResult!!.text = result.contents
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}