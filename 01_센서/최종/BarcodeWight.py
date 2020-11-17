import pyzbar.pyzbar as pyzbar
import cv2
import time
import sys

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
from hx711 import HX711

EMULATE_HX711=False
referenceUnit = 24 # 가장 정확한듯!
hx = HX711(5, 6)

# 브로커 접속 시도 결과 처리 콜백 함수
def on_connect(client, userdata, flags, rc):
    #print("Connected with result code "+ str(rc))
    if rc == 0:
        client.subscribe("iot/app") # 연결 성공시 토픽 구독 신청
    else:
        print('연결 실패 : ', rc)
 
# 관련 토픽 메시지 수신 콜백 함수
def on_message(client, userdata, msg):
    global value

    value = str(msg.payload.decode("utf-8"))
    print(f" {msg.topic} {value}")
    client.disconnect()
    
 
# 1. MQTT 클라이언트 객체 인스턴스화
client = mqtt.Client()
 
# 2. 관련 이벤트에 대한 콜백 함수 등록
client.on_connect = on_connect
client.on_message = on_message

cred = credentials.Certificate('smartcart-1453a-firebase-adminsdk-dg97s-f709784d8b.json')
firebase_admin.initialize_app(cred)

db = firestore.client()

cap = cv2.VideoCapture(0)

i = 0
weight, weight2 = 0, 0
barcode_check = ''
barcode_data = ''
loadcell_data = 0
value = ''
while(cap.isOpened()):
    
    ret, img = cap.read()

    if not ret:
        continue

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
     
    decoded = pyzbar.decode(gray)
    
    
    for d in decoded: 
        x, y, w, h = d.rect
        
        barcode_data = d.data.decode("utf-8")
        
        barcode_type = d.type

        cv2.rectangle(img, (x, y), (x + w, y + h), (0, 0, 255), 2)

        text = '%s (%s)' % (barcode_data, barcode_type)
        cv2.putText(img, text, (x, y), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 2, cv2.LINE_AA)
    
    cv2.imshow('img', img)

    key = cv2.waitKey(1)
    if key == ord('q'):
        break
    elif key == ord('s'):
        i += 1
        cv2.imwrite('c_%03d.jpg' % i, img)
    
    
    

    if barcode_data != '':
        print(barcode_data)

        barcode_ref = db.collection(u'Product').document(barcode_data)
        barcode = barcode_ref.get()

        if barcode.to_dict() is None:
            continue

        print(u'Barcode data: {}'.format(barcode.to_dict()))
        
        try :
            client.connect("192.168.0.127")
            client.publish("iot/and", barcode_data)
            client.loop_start()
            client.loop_stop()
            client.disconnect()
            
        except Exception as err:
            print('에러 : %s'%err)

        start = time.time()
        if barcode: 
            while (time.time() - start < 2): # 2초 정지
                pass 
            barcode_data = ''
            cap.release()
            cap = cv2.VideoCapture(0)
    else:
        client.connect("192.168.0.127")
        client.loop_start()

        if value == "weight":
            hx.set_reading_format("MSB", "MSB")
            print("referenceUnit = ", referenceUnit)
            hx.set_reference_unit(referenceUnit)
            hx.reset()
            hx.tare()
            print("Tare done! Add weight now...")
            weight = hx.get_weight(5)
            weight = int(weight) 
            print("weight = ", weight, "g")
            client.connect("192.168.0.127")
            client.publish("iot/and", str(weight))
            client.loop_stop()
            value=''
            client.disconnect()

        elif value == "weight2":
            weight2 = hx.get_weight(5)
            weight2 = int(weight2) 
            print("weight2 = ", weight2, "g")
            client.connect("192.168.0.127")
            client.publish("iot/and", str(weight2))
            client.loop_stop()
            value=''
            client.disconnect()
        
            print("무게", weight2 - weight, "g")
        

cap.release()
cv2.destroyAllWindows()