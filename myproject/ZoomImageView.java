package com.example.myproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class ZoomImageView extends ImageView implements View.OnTouchListener {

    public class ZoomMode{
        public  final  static  int Ordinary=0;
        public  final  static  int  ZoomIn=1;
        public final static int TowFingerZoom = 2;
    }



    private Matrix matrix;
    //imageView的大小
    private PointF viewSize;
    //圖片的大小
    private PointF imageSize;
    //縮放後圖片的大小
    private PointF scaleSize = new PointF();
    //最初的寬高的縮放比例
    private PointF originScale = new PointF();
    //imageview中bitmap的xy實時座標
    private PointF bitmapOriginPoint = new PointF();
    //點擊的點
    private PointF clickPoint = new PointF();
    //設置的雙擊檢查時間限制
    private long doubleClickTimeSpan = 250;
    //上次點擊的時間
    private long lastClickTime = 0;
    //雙擊放大的倍數
    private int doubleClickZoom = 2;
    //當前縮放的模式
    private int zoomInMode = ZoomMode.Ordinary;
    //臨時座標比例數據
    private PointF tempPoint = new PointF();
    //最大縮放比例
    private float maxScrole = 4;
    //兩點之間的距離
    private float doublePointDistance = 0;
    //雙指縮放時候的中心點
    private PointF doublePointCenter = new PointF();
    //兩指縮放的比例
    private float doubleFingerScrole = 0;
    //上次觸碰的手指數量
    private int lastFingerNum = 0;


    public ZoomImageView(Context context) {
        super(context);
        init();
    }

    public ZoomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
        matrix = new Matrix();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        viewSize = new PointF(width,height);

        Drawable drawable = getDrawable();
        if (drawable != null){
            imageSize = new PointF(drawable.getMinimumWidth(),drawable.getMinimumHeight());
            showCenter();
        }
    }

    /**
     * 設置圖片居中等比顯示
     */
    private void showCenter(){
        float scalex = viewSize.x/imageSize.x;
        float scaley = viewSize.y/imageSize.y;

        float scale = scalex<scaley?scalex:scaley;
        scaleImage(new PointF(scale,scale));

        //移動圖片，並保存最初的圖片左上角（即原點）所在座標
        if (scalex<scaley){
            translationImage(new PointF(0,viewSize.y/2 - scaleSize.y/2));
            bitmapOriginPoint.x = 0;
            bitmapOriginPoint.y = viewSize.y/2 - scaleSize.y/2;
        }else {
            translationImage(new PointF(viewSize.x/2 - scaleSize.x/2,0));
            bitmapOriginPoint.x = viewSize.x/2 - scaleSize.x/2;
            bitmapOriginPoint.y = 0;
        }
        //保存下最初的縮放比例
        originScale.set(scale,scale);
        doubleFingerScrole = scale;
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //手指按下事件
                //記錄被點擊的點的座標
                clickPoint.set(event.getX(),event.getY());
                //判斷屏幕上此時被按住的點的個數，當前屏幕只有一個點被點擊的時候觸發
                if (event.getPointerCount() == 1) {
                    //設置一個點擊的間隔時長，來判斷是不是雙擊
                    if (System.currentTimeMillis() - lastClickTime <= doubleClickTimeSpan) {
                        //如果圖片此時縮放模式是普通模式，就觸發雙擊放大
                        if (zoomInMode == ZoomMode.Ordinary) {
                            //分別記錄被點擊的點到圖片左上角x,y軸的距離與圖片x,y軸邊長的比例，
                            //方便在進行縮放後，算出這個點對應的座標點
                            tempPoint.set((clickPoint.x - bitmapOriginPoint.x) / scaleSize.x,
                                    (clickPoint.y - bitmapOriginPoint.y) / scaleSize.y);
                            //進行縮放
                            scaleImage(new PointF(originScale.x * doubleClickZoom,
                                    originScale.y * doubleClickZoom));
                            //獲取縮放後，圖片左上角的xy座標
                            getBitmapOffset();

                            //平移圖片，使得被點擊的點的位置不變。這裏是計算縮放後被點擊的xy座標，
                            //與原始點擊的位置的xy座標值，計算出差值，然後做平移動作
                            translationImage(
                                    new PointF(
                                            clickPoint.x - (bitmapOriginPoint.x + tempPoint.x * scaleSize.x),
                                            clickPoint.y - (bitmapOriginPoint.y + tempPoint.y * scaleSize.y))
                            );
                            zoomInMode = ZoomMode.ZoomIn;
                            doubleFingerScrole = originScale.x*doubleClickZoom;
                        } else {
                            //雙擊還原
                            showCenter();
                            zoomInMode = ZoomMode.Ordinary;
                        }
                    } else {
                        lastClickTime = System.currentTimeMillis();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //屏幕上已經有一個點按住 再按下一點時觸發該事件
                //計算最初的兩個手指之間的距離
                doublePointDistance = getDoubleFingerDistance(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //屏幕上已經有兩個點按住 再鬆開一點時觸發該事件
                //當有一個手指離開屏幕後，就修改狀態，這樣如果雙擊屏幕就能恢復到初始大小
                zoomInMode = ZoomMode.ZoomIn;
                //記錄此時的雙指縮放比例
                doubleFingerScrole =scaleSize.x/imageSize.x;
                //記錄此時屏幕觸碰的點的數量
                lastFingerNum = 1;
                //判斷縮放後的比例，如果小於最初的那個比例，就恢復到最初的大小
                if (scaleSize.x<viewSize.x && scaleSize.y<viewSize.y){
                    zoomInMode = ZoomMode.Ordinary;
                    showCenter();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //手指移動時觸發事件
                /**************************************移動
                 *******************************************/
                if (zoomInMode != ZoomMode.Ordinary) {
                    //如果是多指，計算中心點爲假設的點擊的點
                    float currentX = 0;
                    float currentY = 0;
                    //獲取此時屏幕上被觸碰的點有多少個
                    int pointCount = event.getPointerCount();
                    //計算出中間點所在的座標
                    for (int i = 0; i < pointCount; i++) {
                        currentX += event.getX(i);
                        currentY += event.getY(i);
                    }
                    currentX /= pointCount;
                    currentY /= pointCount;
                    //當屏幕被觸碰的點的數量變化時，將最新算出來的中心點看作是被點擊的點
                    if (lastFingerNum != event.getPointerCount()) {
                        clickPoint.x = currentX;
                        clickPoint.y = currentY;
                        lastFingerNum = event.getPointerCount();
                    }
                    //將移動手指時，實時計算出來的中心點座標，減去被點擊點的座標就得到了需要移動的距離
                    float moveX = currentX - clickPoint.x;
                    float moveY = currentY - clickPoint.y;
                    //計算邊界，使得不能已出邊界，但是如果是雙指縮放時移動，因爲存在縮放效果，
                    //所以此時的邊界判斷無效
                    float[] moveFloat = moveBorderDistance(moveX, moveY);
                    //處理移動圖片的事件
                    translationImage(new PointF(moveFloat[0], moveFloat[1]));
                    clickPoint.set(currentX, currentY);
                }
                /**************************************縮放
                 *******************************************/
                //判斷當前是兩個手指接觸到屏幕才處理縮放事件
                if (event.getPointerCount() == 2){
                    //如果此時縮放後的大小，大於等於了設置的最大縮放的大小，就不處理
                    if ((scaleSize.x/imageSize.x >= originScale.x * maxScrole
                            || scaleSize.y/imageSize.y >= originScale.y * maxScrole)
                            && getDoubleFingerDistance(event) - doublePointDistance > 0){
                        break;
                    }
                    //這裏設置當雙指縮放的的距離變化量大於50，並且當前不是在雙指縮放狀態下，就計算中心點，等一些操作
                    if (Math.abs(getDoubleFingerDistance(event) - doublePointDistance) > 50
                            && zoomInMode != ZoomMode.TowFingerZoom){
                        //計算兩個手指之間的中心點，當作放大的中心點
                        doublePointCenter.set((event.getX(0) + event.getX(1))/2,
                                (event.getY(0) + event.getY(1))/2);
                        //將雙指的中心點就假設爲點擊的點
                        clickPoint.set(doublePointCenter);
                        //下面就和雙擊放大基本一樣
                        getBitmapOffset();
                        //分別記錄被點擊的點到圖片左上角x,y軸的距離與圖片x,y軸邊長的比例，
                        //方便在進行縮放後，算出這個點對應的座標點
                        tempPoint.set((clickPoint.x - bitmapOriginPoint.x)/scaleSize.x,
                                (clickPoint.y - bitmapOriginPoint.y)/scaleSize.y);
                        //設置進入雙指縮放狀態
                        zoomInMode = ZoomMode.TowFingerZoom;
                    }
                    //如果已經進入雙指縮放狀態，就直接計算縮放的比例，並進行位移
                    if (zoomInMode == ZoomMode.TowFingerZoom){
                        //用當前的縮放比例與此時雙指間距離的縮放比例相乘，就得到對應的圖片應該縮放的比例
                        float scrole =
                                doubleFingerScrole*getDoubleFingerDistance(event)/doublePointDistance;
                        //這裏也是和雙擊放大時一樣的
                        scaleImage(new PointF(scrole,scrole));
                        getBitmapOffset();
                        translationImage(
                                new PointF(
                                        clickPoint.x - (bitmapOriginPoint.x + tempPoint.x*scaleSize.x),
                                        clickPoint.y - (bitmapOriginPoint.y + tempPoint.y*scaleSize.y))
                        );
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //手指鬆開時觸發事件
                Log.e("kzg","***********************ACTION_UP");
                lastFingerNum = 0;
                break;
        }
        return true;
    }



    public void scaleImage(PointF scaleXY){
        matrix.setScale(scaleXY.x,scaleXY.y);
        scaleSize.set(scaleXY.x * imageSize.x,scaleXY.y * imageSize.y);
        setImageMatrix(matrix);
    }

    /**
     * 對圖片進行x和y軸方向的平移
     * @param pointF
     */
    public void translationImage(PointF pointF){
        matrix.postTranslate(pointF.x,pointF.y);
        setImageMatrix(matrix);
    }


    /**
     * 防止移動圖片超過邊界，計算邊界情況
     * @param moveX
     * @param moveY
     * @return
     */
    public float[] moveBorderDistance(float moveX,float moveY){
        //計算bitmap的左上角座標
        getBitmapOffset();
        Log.e("kzg","**********************moveBorderDistance--bitmapOriginPoint:"+bitmapOriginPoint);
        //計算bitmap的右下角座標
        float bitmapRightBottomX = bitmapOriginPoint.x + scaleSize.x;
        float bitmapRightBottomY = bitmapOriginPoint.y + scaleSize.y;

        if (moveY > 0){
            //向下滑
            if (bitmapOriginPoint.y + moveY > 0){
                if (bitmapOriginPoint.y < 0){
                    moveY = -bitmapOriginPoint.y;
                }else {
                    moveY = 0;
                }
            }
        }else if (moveY < 0){
            //向上滑
            if (bitmapRightBottomY + moveY < viewSize.y){
                if (bitmapRightBottomY > viewSize.y){
                    moveY = -(bitmapRightBottomY - viewSize.y);
                }else {
                    moveY = 0;
                }
            }
        }

        if (moveX > 0){
            //向右滑
            if (bitmapOriginPoint.x + moveX > 0){
                if (bitmapOriginPoint.x < 0){
                    moveX = -bitmapOriginPoint.x;
                }else {
                    moveX = 0;
                }
            }
        }else if (moveX < 0){
            //向左滑
            if (bitmapRightBottomX + moveX < viewSize.x){
                if (bitmapRightBottomX > viewSize.x){
                    moveX = -(bitmapRightBottomX - viewSize.x);
                }else {
                    moveX = 0;
                }
            }
        }
        return new float[]{moveX,moveY};
    }

    /**
     * 獲取view中bitmap的座標點
     */
    public void getBitmapOffset(){
        float[] value = new float[9];
        float[] offset = new float[2];
        Matrix imageMatrix = getImageMatrix();
        imageMatrix.getValues(value);
        offset[0] = value[2];
        offset[1] = value[5];
        bitmapOriginPoint.set(offset[0],offset[1]);
    }


    /**
     * 計算零個手指間的距離
     * @param event
     * @return
     */
    public static float  getDoubleFingerDistance(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return  (float)Math.sqrt(x * x + y * y) ;
    }
}
