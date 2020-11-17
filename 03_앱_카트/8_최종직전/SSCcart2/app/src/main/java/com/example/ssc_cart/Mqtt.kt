package com.example.ssc_cart


import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class Mqtt(val ctx: Context?, val uri: String?) {
    val TAG ="Mqtt"
    var mqttClient: MqttAndroidClient
    init {
        mqttClient = MqttAndroidClient(ctx, uri, MqttClient.generateClientId())
    }

    // 토픽 subscribe 수신을 위한 콜백 등록
    fun setCallback(callback: (topic: String, message: MqttMessage) -> Unit){
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.i(TAG, "connection lost");
            }
            // subscribe 메시지 처리
            override fun messageArrived(topic: String, message: MqttMessage) {
                callback(topic, message)
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.i(TAG, "msg delivered");
            }
        })
    }

    fun connect(topics : Array<String>?=null) {
        val mqttConnectOptions = MqttConnectOptions()
        mqttClient.connect(mqttConnectOptions, null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(TAG, "connect succeed")
                    topics?.map {subscribeTopic(it)}
                }
                override fun onFailure(asyncActionToken: IMqttToken,
                                       exception: Throwable) {
                    Log.i(TAG, "connect failed")
                }
            })
    }

    // 토픽 subscribe 등록
    private fun subscribeTopic(topic: String, qos:Int = 0) {
        mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.i(TAG, "subscribed succeed")
            }
            override fun onFailure(asyncActionToken: IMqttToken,
                                   exception: Throwable) {
                Log.i(TAG, "subscribed failed")
            }
        })
    }

    // 토픽 publish
    fun publish(topic: String, payload: String, qos: Int = 0) {
        if(mqttClient.isConnected() === false) {
            mqttClient.connect()
        }
        val message = MqttMessage()
        message.payload = payload.toByteArray()
        message.qos = qos
        mqttClient.publish(topic, message, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.i(TAG, "publish succeed! ")
            }
            override fun onFailure(asyncActionToken: IMqttToken,
                                   exception: Throwable) {
                Log.i(TAG, "publish failed!")
            }
        })
    }
}
