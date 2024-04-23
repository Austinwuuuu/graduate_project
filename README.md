### 檔案說明 ###
 專案有以下幾個檔案:
   1.My Project.zip : 為專案主程式檔案，包含所有介面與邏輯設計的執行檔。
   2.final.zip : 為影像辨識檔案，包含影像辨識的程式執行檔。
   3.lineAddFollower.zip : 推播通知檔案，包含推播通知的程式執行檔。

### 開發軟體工具安裝 ###
 影像辨識:
   1.安裝python跟numpy。
   2.安裝git。
   3.安裝CMAKE。
   4.安裝VS並勾選python開發及C++桌面開發。
   5.安裝opencv並解壓縮，同時在同個路徑下創建一個build資料夾
   6.開啟CMAKE設定路徑。
   7.在剛剛創建的build資料夾中開啟ALL_BUILD.vcxproj，並將debug改為release並開始建置環境。
 
 主程式:
   1.安裝java。
   2.安裝Android Studio。

 推播功能:
   1.安裝python
   2.樹梅派安裝bluetooth bluez套件
   3.樹梅派安裝nodejs
   4.樹梅派安裝並設定line simple beacon 套件 
     (樹梅派執行 : cd line-simple-beacon
		   cd tools
		   cd line-simplebeacon-nodejs-sample
		   sudo ./simplebeacon.js --hwid=017051e066)
   5.pip install line-bot-sdk
   6.pip install flask
   7.pip install flask_cors
   8.pip install pymsql
   
### 執行 ###
 1.解壓縮My Project.zip、final.zip、lineAddFollower.zip。
 2.開啟解壓縮後的final.zip，雙擊final.py開啟程式檔。
 3.確認python與其套件是否有安裝，並開啟或連接鏡頭。
 4.開始執行影像辨識。
 5.雙擊安裝好的Android Studio進入軟體。
 6.選擇導入解壓縮後的My Project專案。
 7.確認沒有錯誤後，連接手持裝置或虛擬機並開啟GPS定位，點選右上角執行按鈕執行程式。
 8.開始使用系統。
 9.使用推播通知前，確認上述推播功能安裝步驟無誤，確認設定好樹梅派後啟動，執行lineAddFollower.py檔。
 10.加入The Crowd的Line官方帳號。(https://liff.line.me/1645278921-kWRPP32q/?accountId=574gurjm)
 11.以手持裝置開啟藍牙定位服務與Line，接近樹梅派後就可以收到作品推播通知。

 應用程式操作介面影片：https://youtube.com/shorts/bTD7eZCH_KA

### 注意事項 ###
 1.首次啟動程式會比較久，若沒有跳出錯誤訊息請耐心等候。
 2.若有錯誤訊息，請檢查軟體是否有正確安裝，以及裝置有成功連接。
 3.若收不到Line的推播通知，請檢查是否有開啟裝置上的藍牙，以及設定中的Line藍牙服務。
