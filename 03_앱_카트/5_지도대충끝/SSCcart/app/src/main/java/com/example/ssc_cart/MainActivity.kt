package com.example.ssc_cart


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
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

    // 이메일과 비밀번호
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var email = ""
    private var password = ""

    //view Objects (QR)
    private var buttonScan: Button? = null
    private var textViewID: TextView? = null
    private var textViewPW: TextView? = null
    private var textViewResult: TextView? = null

    private var et_eamil: EditText? = null
    private var et_password: EditText? = null

    //qr code scanner object (QR)
    private var qrScan: IntentIntegrator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.et_eamil)
        editTextPassword = findViewById(R.id.et_password)

        //View Objects (QR)
        buttonScan = findViewById<View>(R.id.buttonScan) as Button
        textViewID = findViewById<View>(R.id.textViewID) as TextView
        textViewPW = findViewById<View>(R.id.textViewPW) as TextView
        textViewResult = findViewById<View>(R.id.textViewResult) as TextView

        // 로그인 정보에 바로 QR 코드에서 받은 값 넣기
        et_eamil = findViewById<View>(R.id.et_eamil) as EditText
        et_password = findViewById<View>(R.id.et_password) as EditText


        //intializing scan object (QR)
        qrScan = IntentIntegrator(this)

        //button onClick (QR)
        buttonScan!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //scan option
                qrScan!!.setPrompt("Scanning...")
                //qrScan.setOrientationLocked(false);
                qrScan!!.initiateScan()
            }
        })
    }

    fun singUp(view: View?) {
        email = editTextEmail!!.text.toString()
        password = editTextPassword!!.text.toString()
        if (isValidEmail() && isValidPasswd()) {
            createUser(email, password)
        }
    }

    fun signIn(view: View?) {
        email = editTextEmail!!.text.toString()
        password = editTextPassword!!.text.toString()
        if (isValidEmail() && isValidPasswd()) {
            loginUser(email, password)
        }
    }

    // 이메일 유효성 검사
    private fun isValidEmail(): Boolean {
        return if (email.isEmpty()) {
            // 이메일 공백
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // 이메일 형식 불일치
            false
        } else {
            true
        }
    }

    // 비밀번호 유효성 검사
    private fun isValidPasswd(): Boolean {
        return if (password.isEmpty()) {
            // 비밀번호 공백
            false
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            false
        } else {
            true
        }
    }

    // 회원가입
    private fun createUser(email: String, password: String) {
        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 회원가입 성공
                        Toast.makeText(this@MainActivity, R.string.success_signup, Toast.LENGTH_SHORT).show()
                    } else {
                        // 회원가입 실패
                        Toast.makeText(this@MainActivity, R.string.failed_signup, Toast.LENGTH_SHORT).show()
                    }
                }
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
                        i.putExtra("ID", textViewID?.text.toString())
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
                    textViewID!!.text = obj.getString("ID")
                    textViewPW!!.text = obj.getString("PW")

                    // 로그인에 바로 QR 정보 넣기
                    et_eamil?.setText(obj.getString("ID"))
                    et_password?.setText(obj.getString("PW"))

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