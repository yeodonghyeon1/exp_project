# 노안 및 시각장애인 식품 탐지 애플리케이션

![KakaoTalk_20250420_113134120](https://github.com/user-attachments/assets/dd0a921c-3763-4e39-81ef-5904186262ee)
![KakaoTalk_20250420_113135866](https://github.com/user-attachments/assets/2b3f2a80-3175-44e4-b953-8beb75b93c9a)


시각장애인과 노안이 있는 사용자들을 위한 식품 유통기한 및 바코드 인식 애플리케이션입니다.

## 프로젝트 구조

```
exp_project/
├── app/                    # 안드로이드 애플리케이션
├── data/                   # 데이터 분석 결과 저장소
├── DeepTextRecognitionBenchmark/  # OCR 모델
├── exam_file/             # 실험용 예제 파일
├── model/                 # 훈련된 모델 저장소
├── test_app/             # 테스트용 앱
└── flaskserver.py        # Flask 웹 서버
```

## 주요 기능

1. **유통기한 인식**
   - YOLOv8 모델을 사용하여 유통기한 영역 탐지
   - Deep Text Recognition Benchmark 모델을 통한 OCR 처리
   - 인식된 날짜 정보 TTS 음성 안내

2. **바코드 인식**
   - pyzbar 라이브러리를 사용한 바코드 스캔
   - 실시간 바코드 정보 추출

3. **영상 처리**
   - 실시간 카메라 스트림 처리
   - 이미지 크롭 및 전처리
   - 객체 탐지 결과 시각화

## 사용 기술

### 딥러닝
- PyTorch 2.0.1
- YOLOv8
- Deep Text Recognition Benchmark

### 백엔드
- Flask
- OpenCV
- MySQL

### 프론트엔드
- Android Studio (Java)

## 사용된 오픈소스

- [Deep Text Recognition Benchmark](https://github.com/clovaai/deep-text-recognition-benchmark)
- [YOLOv8](https://github.com/ultralytics/ultralytics)
- [TextRecognitionDataGenerator](https://github.com/Belval/TextRecognitionDataGenerator)

## 설치 및 실행 방법

1. 필요한 패키지 설치
```bash
pip install -r requirements.txt
```

2. Flask 서버 실행
```bash
python flaskserver.py
```

3. 안드로이드 앱 설정
- Android Studio에서 app 프로젝트 열기
- 서버 주소 설정 (2곳)
- 앱 빌드 및 실행

## 현재 버전 (v1.0.0 - 2023.09.18)

### 구현된 기능
- 웹캠을 통한 유통기한 및 바코드 촬영
- 실시간 이미지 처리 및 분석
- TTS를 통한 음성 안내

### 알려진 문제점
1. 사진 촬영 기능 오류
2. SurfaceView가 ImageView를 가리는 현상
3. MySQL 연동 미구현
4. 데이터 저장 및 불러오기 기능 미구현

## 시스템 아키텍처

### 서버 (Flask)
- 이미지 처리 및 분석 담당
- 딥러닝 모델 추론 실행
- REST API 엔드포인트 제공
  - `/sendFrame`: 이미지 프레임 처리
  - `/sendvideo`: 비디오 스트림 처리

### 안드로이드 앱
- 카메라 인터페이스
- 서버와의 통신
- TTS 음성 출력
- 사용자 인터페이스

## 향후 개선 계획

1. 사진 촬영 기능 버그 수정
2. UI/UX 개선
   - SurfaceView와 ImageView 겹침 문제 해결
3. 데이터베이스 연동
   - MySQL 구현
   - 데이터 저장/불러오기 기능
4. 성능 최적화
   - 모델 경량화
   - 처리 속도 개선

## 기여 방법

1. Fork the Project
2. Create your Feature Branch
3. Commit your Changes
4. Push to the Branch
5. Open a Pull Request

## 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
