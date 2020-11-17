package com.example.ssc_cart

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import kotlinx.android.synthetic.main.dialog_map.*

class MapDialogActivity : AppCompatActivity() {

    val TAG = "Kakao"
    val RECORD_REQUEST_CODE = 0
    val STORAGE_REQUEST_CODE =0

    var map_items: MutableList<MapData> = mutableListOf()

    data class ALL_PRODUCT(
        val price: Long? = null,
        val name: String? = null,
        val weight: Long? = null,
        val location: String? = null,
        val category: String? = null,
        val url: String? = null
    ) {
        fun getname(): String? {
            return name
        }

        fun getprice(): Long? {
            return price
        }

        fun getweight(): Long? {
            return weight
        }

        fun getlocation(): String? {
            return location
        }

        fun getcategory(): String? {
            return category
        }

        fun geturl(): String? {
            return url
        }
    }

    // 모든 제품 정보 담김 해시맵 생성
    var all_product_db: HashMap<String, ALL_PRODUCT> = hashMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_map)


        // 디비 전체 가져오기

        val db = FirebaseFirestore.getInstance()
        db.collection("Product")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val product_db = document.data.toMap()
                    map_items.add(
                        MapData(
                            product_db?.get("name").toString(),
                            product_db?.get("price") as Long?,
                            product_db?.get("url")?.toString(),
                            product_db?.get("weight") as Long?
                        )
                    )
                    all_product_db.put(
                        product_db?.get("name")?.toString()!!, MapDialogActivity.ALL_PRODUCT(
                            product_db?.get("price") as Long?,
                            product_db?.get("name")?.toString(),
                            product_db?.get("weight") as Long?,
                            product_db?.get("location")?.toString(),
                            product_db?.get("category")?.toString(),
                            product_db?.get("url")?.toString()
                        )
                    )
                }
                Log.d("접근", "${map_items}")
                val adapter = MapAdapter(map_items)
                adapter.itemClick = object : MapAdapter.ItemClick {
                    override fun onClick(view: View, position: Int) {
//                            Toast.makeText(context, map_items[position].getname(), Toast.LENGTH_SHORT).show()
                        val location_map =
                            all_product_db[map_items[position].getname()]?.getlocation().toString()
                        val dlg = ImageDialog(this@MapDialogActivity, location_map)
                        Toast.makeText(
                            this@MapDialogActivity,
                            "${location_map} 구역 ${
                                all_product_db[map_items[position].getname()]?.getcategory()
                                    .toString()
                            } 칸에 있습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dlg.show()
                    }
                }
                rv_map.adapter = adapter
                rv_map.layoutManager = GridLayoutManager(this, 5)

            }
            .addOnFailureListener { exception ->
                Log.d("product DB 전체 가져오기 실패!", "Error getting documents: ", exception)
            }
//        tv_test.text = "제품을 선택해주세요."
        btn_serach.setOnClickListener {
            (rv_map.adapter as MapAdapter).search(et_search.text.toString(), map_items)
        }
        setupPermissions()
    }
    private fun setupPermissions(){
        var permission_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        var permission_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(permission_audio != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission to recode denied audio")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
        }
        else if(permission_storage != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission to recode denied storage")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else {
            //본문실행
            startUsingSpeechSDK()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }

            STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startUsingSpeechSDK(){
//        Toast.makeText(this, "Start Newton", Toast.LENGTH_SHORT).show()

        //SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this)

        //버튼 클릭
        btn_voice.setOnClickListener {
            et_search.setText("")
//            var mediaPlayer = MediaPlayer.create(this, R.raw.android_latest)
//            mediaPlayer?.start()

            //클라이언트 생성
            val builder = SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)
            val client = builder.build()

            //Callback
            client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
                //콜백함수들
                override fun onReady() {
                    Log.d(TAG, "모든 하드웨어 및 오디오 서비스가 준비되었습니다.")


                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "사용자가 말을 하기 시작했습니다.")
                    tv_voice.setText("음성인식 중입니다.")
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "사용자의 말하기가 끝이 났습니다. 데이터를 서버로 전달합니다.")
                    tv_voice.setText("음성인식이 끝났습니다.")
                }

                override fun onPartialResult(partialResult: String?) {
                    //현재 인식된 음성테이터 문자열을 출력해 준다. 여러번 호출됨. 필요에 따라 사용하면 됨.
                    //Log.d(TAG, "현재까지 인식된 문자열:" + partialResult)
                    et_search.setText(partialResult)
                }

                /*
                최종결과 - 음성입력이 종료 혹은 stopRecording()이 호출되고 서버에 질의가 완료되고 나서 호출됨
                Bundle에 ArrayList로 값을 받음. 신뢰도가 높음 것 부터...
                 */
                override fun onResults(results: Bundle?) {
                    val texts = results?.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS)
                    val confs = results?.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES)

                    Log.d(TAG, texts?.get(0).toString())
                    //정확도가 높은 첫번째 결과값을 텍스트뷰에 출력
                    runOnUiThread {
                        et_search.setText(texts?.get(0))
                    }


                }

                override fun onAudioLevel(audioLevel: Float) {
                    //Log.d(TAG, "Audio Level(0~1): " + audioLevel.toString())
                }

                override fun onError(errorCode: Int, errorMsg: String?) {
                    //에러 출력 해 봄
                    Log.d(TAG, "Error: " + errorMsg)
                }
                override fun onFinished() {
                }
            })

            //음성인식 시작함
            client.startRecording(true)
        }
    }
}