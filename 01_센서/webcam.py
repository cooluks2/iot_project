import pyzbar.pyzbar as pyzbar
import cv2
import time

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

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


        # if (d.data.decode("utf-8") == barcode_data):
        #     break

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

    if barcode_check != barcode_data and barcode_data != '':
        print(barcode_data)
        docs = db.collection(u'Product').where(u'barcode', u'==',8801056154011).stream()
        for doc in docs:
            print(u'{} => {}'.format(doc.id, doc.to_dict()))
        del docs
    barcode_check = barcode_data
    

cap.release()
cv2.destroyAllWindows()