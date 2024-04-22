package com.example.myproject;

import android.content.Context;
import android.content.res.ObbInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class recommandedFragment extends Fragment {

    View view;
    MapView map = null;
    IMapController mapController;
    TextView recommend_top_text; // 頂上的展區名稱
    TextView notSelect; // 未設定展區時所顯示的
    Button btn_reset_recommendMap_to_center; // reset map button
    Button btn_refresh_recommendMap; // refresh map button
    String exLocation; // 展區經緯度

    CardView recommended_cardView; // 展館簡介卡
    TextView cardView_title; // 簡介卡標題(展館名稱)
    TextView cardView_subTitle; // 簡介卡副標題(展館類別)
    TextView cardView_content; // 簡介卡內容(展館路線順序)


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recommanded, container, false);

        // map view
        map = view.findViewById(R.id.map_recommend);
        // 尚未設置展區所顯示的文字
        notSelect = view.findViewById(R.id.notSelect);
        // reset map button
        btn_reset_recommendMap_to_center = view.findViewById(R.id.btn_reset_recommendMap_to_center);
        // refresh map button
        btn_refresh_recommendMap = view.findViewById(R.id.btn_refresh_recommendMap);
        // 展館簡介卡
        recommended_cardView = view.findViewById(R.id.recommended_cardView);
        // 簡介卡標題(展館名稱)
        cardView_title = view.findViewById(R.id.cardView_title);
        // 簡介卡副標題(展館類別)
        cardView_subTitle = view.findViewById(R.id.cardView_subTitle);
        // 簡介卡內容(展館路線順序)
        cardView_content = view.findViewById(R.id.cardView_content);


        String exName = ((MainActivity)getActivity()).getExhibitionData(); // 目前所選擇的展區名稱
        List<String> userPreference = ((MainActivity)getActivity()).getUserPreferences(); // 目前選擇的參觀偏好值

//        if (exName.equals("國立高雄大學")) { // 若選擇的展區為國立高雄大學，則顯示地圖中的詳細物件

            if (!(exName.equals("") || userPreference.size()==0)){ // 判斷若展區值且偏好值不為空，則載入地圖
                // 頂上的展區名稱
                recommend_top_text = view.findViewById(R.id.recommend_top_text);
                recommend_top_text.setText(exName);

                // osm地圖初始化
                initMap();

                // 載入地圖與推薦路線結果
                map_thread(exName);
            }
            else{ // 展區值為空，不顯示地圖，顯示文字提醒
                Toast.makeText(getActivity(), "尚未選擇展區或偏好!", Toast.LENGTH_SHORT).show();
                notSelect.setVisibility(View.VISIBLE);
            }

//        }
//        else{ // 若不是選擇國立高雄大學，則跳出未與該展區合作的訊息
//            if (exName.equals("")){ // 展區值為空
//                Toast.makeText(getActivity(), "尚未選擇展區或偏好!", Toast.LENGTH_SHORT).show();
//                notSelect.setVisibility(View.VISIBLE);
//            }
//            else { // 展區值不為空
//                map.setVisibility(View.GONE); // 隱藏map
//                notSelect.setText("尚未與此展區合作!");
//                notSelect.setVisibility(View.VISIBLE);
//            }
//        }

        return view;
    }

    //
    private void initMap(){
        //--- map ---//
        // map view 設為看得見
        map.setVisibility(View.VISIBLE);

        // context
        Context ctx = getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //inflate and create the map
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // 不設置縮放地圖+-按鈕
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // 地圖控制器
        mapController = map.getController();
        // 縮放比例為18
        mapController.setZoom(18);
    }

    private void map_thread(String exName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 連DB，取展區經緯度
                db con = new db();
                con.run();
                exLocation = con.getExLocation(exName); // 展區中心點

                // 經緯度做字串處理並轉成double型態
                String[] splitLocation = exLocation.split(",");
                Double latitude_of_centerPoint = Double.valueOf(splitLocation[0].trim());
                Double longitude_of_centerPoint = Double.valueOf(splitLocation[1].trim());

                // 把從DB取出來的展區經緯度定為中心點，放到畫面上
                map.post(new Runnable() {
                    @Override
                    public void run() {
                        // 設置中心點
                        GeoPoint startPoint = new GeoPoint(latitude_of_centerPoint, longitude_of_centerPoint);
                        mapController.animateTo(startPoint); // 設定地圖中心點

                        //********** 設置建築物標點 **********//
                        Marker marker; // 設置建築物標點
                        GeoPoint buildingLoc; // 建築物經緯度
                        String buildingName; // 建築物名稱
                        String buildingSequence; // 建築物參觀順序編號
                        String buildingCategory; // 展館類別

                        Map<String, List<String>> recommanded;
                        recommanded = ((MainActivity)getActivity()).getLocation();

                        for (Map.Entry<String, List<String>> bloc : recommanded.entrySet()){
                            // 把建築物經緯度轉為double型態
                            Double lat_of_bloc = Double.valueOf(bloc.getValue().get(1));
                            Double long_of_bloc = Double.valueOf(bloc.getValue().get(2));

                            marker = new Marker(map); // 設置marker物件
                            buildingLoc = new GeoPoint(lat_of_bloc, long_of_bloc); // 建築物的經緯度
                            buildingName = bloc.getKey(); // 建築物的名稱
                            buildingSequence = bloc.getValue().get(0); // 建築物參觀順序編號
                            buildingCategory = bloc.getValue().get(3); // 取得展館類別

                            marker.setPosition(buildingLoc); // 設置標點的位置
                            marker.setTitle(buildingName); // 設置標點的建築物名稱

                            // 判斷該建築物的人流狀況給予參觀順序編號
                            marker.setIcon(markerSequence(buildingSequence));

                            // marker的點擊事件
                            String cardView_BuildingName = buildingName; // 展館簡介卡:展館名稱
                            String cardView_BuildingCategory = buildingCategory; // 展館簡介卡:展館類別
                            String cardView_BuildingSequence = buildingSequence; // 展館簡介卡:展館路線順序

                            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {

                                    mapController.animateTo(marker.getPosition()); // 將點選到的marker移到中心點(有滑動動畫)
                                    recommended_cardView.setVisibility(View.VISIBLE); // 簡介卡設為可視
                                    cardView_title.setText(cardView_BuildingName); // 設置簡介卡標題(展館名稱)
                                    cardView_subTitle.setText(cardView_BuildingCategory); // 設置簡介卡副標題(展館類別)
                                    cardView_content.setText("參觀順序\n"+"- "+cardView_BuildingSequence); // 設置簡介卡內容(展館路線順序)

                                    return true; // 表示可以點擊
                                }
                            });

                            map.getOverlays().add(marker); // 新增標點於map上
                        }

                        // reset map to user location button
                        double uLat = ((MainActivity)getActivity()).getUserLatitude();
                        double uLong = ((MainActivity)getActivity()).getUserLongitude();
                        GeoPoint userLocation = new GeoPoint(uLat, uLong);
                        reset_map(userLocation);

                        // refresh map
                        refresh_map(exName);
                    }
                });
            }
        }).start();
    }

    private void reset_map(GeoPoint userLocation){
        btn_reset_recommendMap_to_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.setZoom(18);
                mapController.animateTo(userLocation);
                recommended_cardView.setVisibility(View.GONE); // 簡介卡設為不可視
            }
        });
    }

    private void refresh_map(String exName){
        btn_refresh_recommendMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map_thread(exName);
                recommended_cardView.setVisibility(View.GONE); // 簡介卡設為不可視
            }
        });
    }

    // 推薦路線順序標點之method，傳入順序值1,2,3...
    private Drawable markerSequence(@Nullable String text){

        // 初始化paint //
        // marker icon 內的白圓圈背景
        Paint indicatorBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorBackground.setColor(getActivity().getColor(R.color.white));
        indicatorBackground.setStyle(Paint.Style.FILL);

        // marker icon 內的文字
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int spSize = 18;
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spSize, getResources().getDisplayMetrics()); // 18sp轉像素
        textPaint.setTextSize(scaledSizeInPixels);
        textPaint.setColor(getActivity().getColor(R.color.___________color));
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 繪製marker icon(原圖)
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.recommend_marker , null);
        Bitmap markerBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas markerCanvas = new Canvas(markerBitmap);    // Construct a canvas with specified bitmap to draw into
        drawable.setBounds(0, 0, markerCanvas.getWidth(), markerCanvas.getHeight());
        drawable.draw(markerCanvas);    // draw drawable into canvas

        // 繪製marker icon(調整後)
        Bitmap processedBitmap = Bitmap.createBitmap(markerBitmap.getWidth(), markerBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(processedBitmap);
        canvas.drawBitmap(markerBitmap, 1,  1, null);

        // 畫白色圓形背景和文字在marker上
        float indicatorRadius = (float)(processedBitmap.getWidth()/4); // 半徑
        float indicatorX = (float)(processedBitmap.getWidth()/2); // x軸:水滴寬度的一半
        float indicatorY = (float)(processedBitmap.getHeight()/2.6); // y軸:水滴長度的1/3.5

        canvas.drawCircle(indicatorX, indicatorY, indicatorRadius, indicatorBackground);
        canvas.drawText(text,indicatorX,indicatorY+(indicatorRadius/2), textPaint);

        // marker icon的原圖先回收
        markerBitmap.recycle();

        // 回傳設置好的marker icon d
        Drawable d = new BitmapDrawable(getResources(), processedBitmap);
        return d;
    }
}
