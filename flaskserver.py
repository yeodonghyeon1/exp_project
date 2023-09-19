from flask import Flask, redirect, request, Response
import base64
import io
import os
from PIL import Image
from flask_cors import CORS
from werkzeug.serving import run_simple
import subprocess
import cv2
import torch
from ultralytics import YOLO
import DeepTextRecognitionBenchmark.demo2
import pyzbar.pyzbar as pyzbar  
import numpy as np  
import pymysql
app = Flask(__name__)
CORS(app)

count = 0
ocr_data = ""
#model = torch.hub.load('D:/yolov5-master', 'custom', path='./model/exp_find_model(yolov5).pt', source='local')
#model2 = torch.hub.load('D:/yolov5-master', 'custom', path='./model/date_model(yolov5).pt', source='local')

model = YOLO('./model/exp_find_model(yolov8).pt')
model.load('./model/exp_find_model(yolov8).pt')
model2 = YOLO('./model/date_model(yolov8).pt')
model2.load('./model/date_model(yolov8).pt')

#!yolo val model=D:/ultralytics-main/runs/detect/train5/weights/best.pt data=coco129.yaml
#!yolo predict model=D:/ultralytics-main/runs/detect/train5/weights/best.pt source='D:/yolov5-master/data/mobile_crop_images/' #2model
#!yolo predict model=D:/ultralytics-main/runs/detect/train/weights/best.pt source='D:/yolov5-master/data/images/' #1model

# conn = pymysql.connect(host='127.0.0.1', user= 'root',password='1234', db='expdb', charset='utf8')
# cur = conn.cursor()

# sql = "SELECT * FROM `busan-jibun`;"
# cursor.execute(sql)
# result = cursor.fetchall()

def object_detect_crop():
    # Model
    im = './data/mobile_images/sample.png'
    results = model(im)
    list_loc = []
    for r in results:
        list_a = r.boxes.xyxy.tolist()  # print the Boxes object containing the detection bounding boxes
    if(len(list_a) != 0):
        img = Image.open('./data/mobile_images/sample.png')
        img_cropped = img.crop((list_a[0][0],list_a[0][1],list_a[0][2],list_a[0][3]))
        img_cropped.save('./data/mobile_crop_images/sample.png','png')


def object_detect_detail_crop():
    
    im = './data/mobile_crop_images/sample.png'
    results = model2(im)
    list_loc = []
    list_name = []
    for r in results:
            list_loc = r.boxes.xyxy.tolist()  # print the Boxes object containing the detection bounding boxes
            list_name = r.boxes.cls.tolist()
    for x in range(0, len(list_loc)):
        img = Image.open('./data/mobile_crop_images/sample.png')
        img_cropped = img.crop((list_loc[x][0], list_loc[x][1], list_loc[x][2], list_loc[x][3]))
        if(list_name[x] == 0.0):
            img_name = "year"
        elif(list_name[x] == 1.0):
            img_name = "month"
        elif(list_name[x] == 2.0):
            img_name = "day"
        img_cropped.save('./data/mobile_crop_images/{}.png'.format(img_name),'png')
        
    os.remove('./data/mobile_crop_images/sample.png')

    
def object_ocr():
    subprocess.call("python ./DeepTextRecognitionBenchmark/demo.py --Transformation None --FeatureExtraction VGG --SequenceModeling BiLSTM --Prediction CTC --image_folder ./data/mobile_crop_images/ --saved_model ./model/None-VGG-BiLSTM-CTC-Seed1111.pth")

    #f = open(r'D:/yolov5-master/data/mobile_txt/log_demo_result.txt')

    ocr_data = []
    count = 0
    all_ocr_data = ""
    f = open(r'./data/mobile_txt/log_demo_result.txt')
    if f.mode =="r":
        content = f.readlines()
        for x in content:
            x = x.replace("./data/mobile_crop_images/", "")
            x = x.replace(".png", "")
            x = x.replace("\t", "  ")
            x = x.replace("day", "일")
            x = x.replace("month", "월")
            x = x.replace("year", "년")
            print(x)
            ocr_data.append(str(x[3:10].strip())+ x[:2].strip())
            # ocr_data.append(str(x))
    f.close
    for i in ocr_data:
        all_ocr_data += str(i)

    return all_ocr_data

def object_ocr2():
    pass 
    

#바코드 탐지
def decode(im):
    # Find barcodes and QR codes
    decodedObjects = pyzbar.decode(im)

    # Print results
    for obj in decodedObjects:
        print('Type : ', obj.type)
        print('Data : ', obj.data, '\n')

    return decodedObjects


@app.route("/sendFrame", methods=['GET', 'POST']) 
def re():
    try:
        a = ""
        global count
        global ocr_data
        global barcode
        print(request.method)
        one_data = request.form['image']



        print("작동 중", count)
        if count == 0:
                count = 1
                #안드로이드에서 'image'변수에 base64로 변환된 bitmap이미지
                #base64로 인코딩된 이미지 데이터를 디코딩하여 byte형태로 변환
                imgdata = base64.b64decode(one_data)
                #byte형태의 이미지 데이터를 이미지로 변환
                image = Image.open(io.BytesIO(imgdata))
                #이미지 사이즈 조정
                img_resize = image.resize((int(640), int(640)))
                #cv2.imwrite("d:/yolov5-master/data/mobile_images/a.png", img_resize)
                
                
                barcode = decode(img_resize)
                 
                if(barcode != None):
                    print(barcode)
                    pass
                
                img_resize.save('./data/mobile_images/sample.png', 'png')
                object_detect_crop()
                object_detect_detail_crop()
                ocr_data = object_ocr()
                #이미지 분석관련 코드 작성
                count = 0
                os.remove('./data/mobile_crop_images/day.png')
                os.remove('./data/mobile_crop_images/month.png')
                os.remove('./data/mobile_crop_images/year.png')
                os.remove('./data/mobile_txt/log_demo_result.txt')
        a = ocr_data
        return a
    except:
        print("aaa")
        count = 0
        return "0"
    
    #결과값 리턴    
    
@app.route("/sendvideo", methods=['GET', 'POST']) 
def re2():
    try:
        try:
            if os.path.isfile("./test.mp4"):
                os.remove('./test.mp4')
        except:
            pass
        f = request.files['files']
        f.save('./' + f.filename)
        f.close()
        video = cv2.VideoCapture('./test.mp4')
        if video.isOpened():
            fps = video.get(cv2.CAP_PROP_FPS)
            f_count = video.get(cv2.CAP_PROP_FRAME_COUNT)
            f_width = video.get(cv2.CAP_PROP_FRAME_WIDTH)
            f_height = video.get(cv2.CAP_PROP_FRAME_HEIGHT)
            
            print('Frames per second : ', fps,'FPS')
            print('Frame count : ', f_count)
            print('Frame width : ', f_width)
            print('Frame height : ', f_height)
        print("a")
        frame = []
        best_conf = []
        count = 0
        count2 = 0
        barcodes = []
        barcode_info = None


        #비디오 저장 코드 입니다
        w = round(video.get(cv2.CAP_PROP_FRAME_WIDTH))
        h = round(video.get(cv2.CAP_PROP_FRAME_HEIGHT))
        fps = video.get(cv2.CAP_PROP_FPS) # 카메라에 따라 값이 정상적, 비정상적
        fourcc = cv2.VideoWriter_fourcc(*'DIVX')
        delay = round(1000/fps)
        font = cv2.FONT_HERSHEY_SIMPLEX


        out = cv2.VideoWriter('output.avi', fourcc, fps, (w, h)) 
        out2 = cv2.VideoWriter('output2.avi', fourcc, fps, (w, h))
        if not out.isOpened():
            print('File open failed!')
 
        while video.isOpened():
            ret, frame = video.read()
            if ret :
                results = model(frame, stream=False)
                for result in results:
                    boxes = result.boxes.xyxy  # Boxes object for bbox outputs
                    masks = result.masks  # Masks object for segmentation masks outputs
                    keypoints = result.keypoints  # Keypoints object for pose outputs
                    probs = result.probs  # Class probabilities for classification outputs
                    conf = result.boxes.conf
                    cls = result.boxes.cls
                # cv2.imshow("Results", results[0].plot())

                if not barcodes:
                    print("바코드 인식 못함")
                    barcodes = decode(frame)
                elif barcodes:
                        barcodes = decode(frame)
                        
                        for barcode in barcodes:
                            x, y, w, h = barcode.rect
                            barcode_info = barcode.data.decode("utf-8")
                            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
                            cv2.putText(frame, barcode_info, (x , y - 20), font, 0.5, (0, 0, 255), 1)
                        print(barcode_info)
                        print("들어옴2")
                        count2 += 1
                out.write(results[0].plot())
                
                if(boxes.tolist()):
                    if(conf.tolist()[0] > 0.5):
                        print(boxes.tolist()[0])
                        print(conf.tolist()[0])
                        if(count == 0):
                            best_conf.append(conf.tolist()[0])
                            best_conf.append(boxes.tolist()[0])
                            best_conf.append(cls.tolist()[0])
                            count += 1
                        else:
                            if(best_conf[0] < conf.tolist()[0]):
                                best_conf[0] = conf.tolist()[0]
                                best_conf[1] = boxes.tolist()[0]
                                best_conf[2] = cls.tolist()[0]
                                cv2.imwrite('./data/mobile_crop_images/sample.png',frame)
                        print("best_conf:", best_conf)

            else:
                img = Image.open('./data/mobile_crop_images/sample.png')
                img_cropped = img.crop((best_conf[1][0], best_conf[1][1], best_conf[1][2], best_conf[1][3]))
                img_cropped.save('./data/mobile_crop_images/sample.png','png')
                object_detect_detail_crop()
                ocr_data = object_ocr()
                print("Frame이 끝났습니다.")

                break
        # cv2.destroyAllWindows()


        return ocr_data + " " + str(barcode_info)
    except:
        print("오류")
        return str(barcode_info)
    
if __name__ == "__main__":
    app.run(host="0.0.0.0", port="8080",use_reloader=False)