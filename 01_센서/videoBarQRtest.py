# 필요한 패키지를 import
from imutils.video import VideoStream
from pyzbar import pyzbar
import argparse
import datetime
import imutils
import time
import cv2
 
# argument 파서를 생성하고 파싱을 한다
ap = argparse.ArgumentParser()
ap.add_argument("-o", "--output", type=str, default="barcodes.csv",
help="path to output CSV file containing barcodes")
args = vars(ap.parse_args())

# 비디오 스트림을 초기화하고 카메라 센서를 준비 시킨다.
print("[INFO] starting video stream...")
# vs = VideoStream(src=0).start()           # USB 카메라용
vs = VideoStream(usePiCamera=True).start()  # PyCamera 용
time.sleep(2.0)
# 기록을 위한 CSV 파일을 열고 초기화
# barcodes found
csv = open(args["output"], "w")
found = set()

# 비디오 스트림의 프레임의 루프
while True:
    # 쓰레드의 비디오를 캡쳐하고 크기를 최대 400 pixels로 조절
    frame = vs.read()
    frame = imutils.resize(frame, width=400)
    
    #프레임에서 바코드(barcodes)를 찾고 각 바코드를 디코드
    barcodes = pyzbar.decode(frame)

    # 검출한 바코드 루프
    for barcode in barcodes:
        # 바코드의 영역을 추출하고 영역 그리기
        # 이미지의 바코드 주변에 박스를 그림
        (x, y, w, h) = barcode.rect
        cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
        
        # 바코드 데이터는 바이트 객체이므로 이미지에 그리려면

        #문자열을 먼져 바꿔야 한다.

        barcodeData = barcode.data.decode("utf-8")
        barcodeType = barcode.type
        
        # 바코드 데이터와 타입을 이미지에 그림
        text = "{} ({})".format(barcodeData, barcodeType)
        cv2.putText(frame, text, (x, y - 10),
        cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
        
        # 바코드 텍스트가 CSV 파일에 있는 바코드가 아니면
        # 타임스탬프 + 바코드를 디스크에 기록하고 set을 업데이트
        if barcodeData not in found:
            csv.write("{},{}\n".format(datetime.datetime.now(),
            barcodeData))
            csv.flush()
            found.add(barcodeData)

    # 출력 프레임을 보여 줌
    cv2.imshow("Barcode Scanner", frame)
    key = cv2.waitKey(1) & 0xFF

    #`q` 키가 눌리면 루프를 벗어남
    if key == ord("q"):
        break
    
# 출력 CSV 파일을 닫고 정리를 한다.
print("[INFO] cleaning up...")
csv.close()
cv2.destroyAllWindows()
vs.stop()
