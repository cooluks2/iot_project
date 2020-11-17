import pyzbar.pyzbar as pyzbar
import cv2
import time

import socket
import time

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import paho.mqtt.client as mqtt

# global barcode_data

# 브로커 접속 시도 결과 처리 콜백 함수
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+ str(rc))
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
    
    # return value
    # MongoDB에 데이터 저장하는 코드가 여기에서 이루어짐
 
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
barcode_check = ''
barcode_data = ''
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
            # 3. 브로커 연결
            client.connect("192.168.0.127")
            # 4. 메시지 루프 - 이벤트 발생시 해당 콜백 함수 호출됨
            client.loop_forever()

            if value == "barcode":
                client.connect("192.168.0.127")
                client.publish("iot/and", barcode_data)
                client.disconnect()


            # client.loop_start()
            # 새로운 스래드를 가동해서 운영 - daemon 스레드  Thread.setDaemon(True)
        except Exception as err:
            print('에러 : %s'%err)

        start = time.time()
        if barcode: 
            while (time.time() - start < 2): # 2초 정지
                pass 
            barcode_data = ''
            cap.release()
            cap = cv2.VideoCapture(0)

cap.release()
cv2.destroyAllWindows()