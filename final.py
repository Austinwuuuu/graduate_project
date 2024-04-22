import cv2
import numpy as np
import mysql.connector

# 載入預先訓練好的人臉辨識模型
frontal_face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
profile_face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_profileface.xml')
# 開啟攝影機
cap = cv2.VideoCapture(0)
# 初始化人臉計數器及前一個人臉位置
face_count = 0
prev_face_position = None
moving_direction = None  # 追蹤人臉的移動方向
has_changed_direction = False  # 記錄是否已經變換過方向
db_config = {
    "host": "140.127.220.88",
    "user": "fuckyouharry",
    "password": "MEWatRn9",
    "database": "thecrowd"
}
conn = mysql.connector.connect(**db_config)

if conn.is_connected():
    print("成功連接到資料庫")
else:
    print("無法連接到資料庫")
cursor = conn.cursor()
while True:
    # 讀取攝影機影像
    ret, frame = cap.read()
    if not ret:
        break
    # 將影像轉換成灰階
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    # 首先判斷是否為正臉
    frontal_faces = frontal_face_cascade.detectMultiScale(gray, scaleFactor=1.2, minNeighbors=2, minSize=(60, 60), maxSize=(120, 120))
    # 如果判斷不是正臉，再判斷是否為側臉
    if len(frontal_faces) == 0:
        profile_faces = profile_face_cascade.detectMultiScale(gray, scaleFactor=1.2, minNeighbors=2, minSize=(60, 60), maxSize=(120, 120))
        faces = profile_faces
    else:
        faces = frontal_faces
    # 更新人臉計數器
    current_face_count = len(faces)
    # 判斷人臉的移動方向並更新人臉計數
    if prev_face_position is not None and current_face_count == 1:
        x, _, w, _ = faces[0]
        if x > frame.shape[1] // 2:  # 人臉從影像右側移至左側
            if not has_changed_direction:
                moving_direction = 'left'
                face_count -= 1
                has_changed_direction = True
        elif x + w < frame.shape[1] // 2:  # 人臉從影像左側移至右側
            if not has_changed_direction:
                moving_direction = 'right'
                face_count += 1
                has_changed_direction = True
        else:
            moving_direction = None
            has_changed_direction = False
    prev_face_position = faces[0] if current_face_count == 1 else None
    if face_count < 0:
        face_count = 0
    density = int(face_count / 1.0)
    No = 'C5'
    update_query = "UPDATE building SET degree = %s WHERE No = %s"
    cursor.execute(update_query, (density, No))
    conn.commit()
    # 繪製辨識結果
    for (x, y, w, h) in faces:
        cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
    # 在影像上顯示人臉數量
    cv2.putText(frame, f"Face Count: {face_count}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
    # 顯示結果影像
    cv2.imshow('Face Detection and Tracking', frame)
    # 按下'q'鍵則結束程式
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break
cap.release()
cv2.destroyAllWindows()