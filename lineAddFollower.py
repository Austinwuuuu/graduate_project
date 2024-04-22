from flask import Flask, abort, request
import pymysql

# 載入 json 處理回傳的資料格式
import json

# 載入 LINE Message API 
from linebot import LineBotApi, WebhookHandler
from linebot.exceptions import InvalidSignatureError
from linebot.models import MessageEvent, TextMessage, TextSendMessage
from linebot.models import BeaconEvent, TextSendMessage, FollowEvent

app = Flask(__name__)

#line channel資訊
line_bot_api = LineBotApi('5YV5niyN76r8Lk75dHdZ+Zu6DNxej/cgkAM6kmVBFtotQRYK671arbfj/uUrmlZwuY7juzTW2aBBe+MGiHBzSImiMb0rQTgT7vXDrW2D7U+67Db5SmgQe1VMFiydtGNQ1DPR4wDTyNPjlGfZMf6FIgdB04t89/1O/w1cDnyilFU=')
handler = WebhookHandler('6403fe640ed8bdc06b64188b06cf937c')

# 資料庫連結
DBHOST = "140.127.220.88"
DBUSER = "fuckyouharry"
DBPASSWORD = "MEWatRn9"
DBNAME = "thecrowd"

@app.route("/linebot", methods=['POST'])
def linebot():
    signature = request.headers['X-Line-Signature']
    body = request.get_data(as_text=True)
    try:
        handler.handle(body, signature)
    except InvalidSignatureError:
        abort(400)
    return 'OK'

@handler.add(BeaconEvent)
def handle_beacon_event(event):
    hwid = event.beacon.hwid
    beacon_type = event.beacon.type
    if hwid == '017051e066' and beacon_type == 'enter':
        DBdata = get_data()

        if DBdata:
            line_bot_api.reply_message(
                event.reply_token,
                TextSendMessage(text=f"{DBdata}")
            )
        else:
            line_bot_api.reply_message(
                event.reply_token,
                TextSendMessage(text="No data found in the database")
            )

#加好友提醒
@handler.add(FollowEvent)
def handle_new_follower(event):
    welcome_message = "您好,歡迎加入TheCrowd好友,請開啟藍芽及LINE Beacon接收以獲得最新推播通知。"
    line_bot_api.reply_message(
        event.reply_token,
        TextSendMessage(text= welcome_message)
    )

def get_data():
    connection = None
    try:
        connection = pymysql.connect(host=DBHOST, user=DBUSER, password=DBPASSWORD, db=DBNAME)
        with connection.cursor() as cursor:
            sql = """
                SELECT building.introduction
                FROM building
                JOIN beacon ON building.beaconId = beacon.deviceId
                WHERE beacon.HWid = '017051e066'
            """
            cursor.execute(sql)
            result = cursor.fetchone()
            if result:
                return result[0]
            else:
                return None
    except Exception as e:
        print("An error occurred while connecting to the database:", str(e))
        return None
    finally:
        if connection:
            connection.close()

if __name__ == "__main__":
    app.run()