package com.example.myproject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class showMapFragment extends Fragment  {

    View view;
    MapView map = null;
    IMapController mapController;
    TextView show_map_top_text; // 頂上的展區名稱
    RelativeLayout remind_view; // 提示
    TextView nowTime; // 目前時間

    TextView nonSelect; // 未設定展區時所顯示的

    Button btn_reset_map_to_center; // reset map button
    Button btn_refresh_map; // refresh map button

    String exLocation; // 展區經緯度
    Map<String, ArrayList<Object>> bLocation; // 存建築物名稱、建築物人流數量、建築物經緯度

    CardView intro_cardView; // 展區簡介卡片
    TextView cardView_title; // 展區簡介卡片的標題(展館名稱)
    TextView cardView_subTitle; // 展區簡介卡的副標題(展館類別)
    TextView cardView_content; // 展區簡介卡片的內容(展館人流狀況)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_show_map, container, false);

        // map view
        map = view.findViewById(R.id.map);
        // remind view layout
        remind_view = view.findViewById(R.id.remind_view);
        //now time textview
        nowTime = view.findViewById(R.id.nowTime);
        // 未設定展區所顯示的
        nonSelect = view.findViewById(R.id.nonSelect);
        // reset map button
        btn_reset_map_to_center = view.findViewById(R.id.btn_reset_map_to_center);
        // refresh map button
        btn_refresh_map = view.findViewById(R.id.btn_refresh_map);
        // 展區簡介卡片
        intro_cardView = view.findViewById(R.id.intro_cardView);
        // 展區簡介卡片的標題
        cardView_title = view.findViewById(R.id.cardView_title);
        // 展區簡介卡片的副標題
        cardView_subTitle = view.findViewById(R.id.cardView_subTitle);
        // 展區簡介卡的內容
        cardView_content = view.findViewById(R.id.cardView_content);

        String exName = ((MainActivity)getActivity()).getExhibitionData(); // 目前所選擇的展區名稱
        List<String> userPreference = ((MainActivity)getActivity()).getUserPreferences(); // 目前選擇的參觀偏好值

//        if (exName.equals("國立高雄大學")) { // 若選擇的展區為國立高雄大學，則顯示地圖中的詳細物件

            if (!(exName.equals("") || userPreference.size()==0)){ // 判斷若展區值且偏好值不為空，則載入地圖
                // 頂上的展區名稱
                show_map_top_text = view.findViewById(R.id.show_map_top_text);
                show_map_top_text.setText(exName);

                // remind view 設為看得見
                remind_view.setVisibility(View.VISIBLE);

                // 目前時間設為看得見
                nowTime.setVisibility(View.VISIBLE);

                // osm地圖之初始化
                initMap();

                // 載入地圖與人流狀況
                map_thread(exName);
            }

            else{ // 展區值為空，不顯示地圖，顯示文字提醒
                Toast.makeText(getActivity(), "尚未選擇展區或偏好!", Toast.LENGTH_SHORT).show();
                nonSelect.setVisibility(View.VISIBLE);
            }

            // 目前時間(顯示於左上角)
            timeThread();
//        }
//        else{ // 若不是選擇國立高雄大學，則跳出未與該展區合作的訊息
//            if (exName.equals("")){ // 展區值為空
//                Toast.makeText(getActivity(), "尚未選擇展區或偏好!", Toast.LENGTH_SHORT).show();
//                nonSelect.setVisibility(View.VISIBLE);
//            }
//            else { // 展區值不為空
//                map.setVisibility(View.GONE); // 隱藏map
//                remind_view.setVisibility(View.GONE); // 隱藏示警
//                nowTime.setVisibility(View.GONE); // 隱藏時間
//                nonSelect.setText("尚未與此展區合作!");
//                nonSelect.setVisibility(View.VISIBLE);
//            }
//        }

        return view;
    }

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
                bLocation = new HashMap<>();
                bLocation = con.getBuildingLocation(exName); // 展區內建築物

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

                        // 人流狀況標點物件(綠、橘、紅)，視人流狀況設置不同建築物標點
                        Drawable normal = ResourcesCompat.getDrawable(getResources(), R.drawable.show_map_normal, null);
                        Drawable some = ResourcesCompat.getDrawable(getResources(), R.drawable.show_map_some, null);
                        Drawable crowded = ResourcesCompat.getDrawable(getResources(), R.drawable.show_map_crowded, null);

                        Marker marker; // 設置建築物標點
                        GeoPoint buildingLoc; // 建築物經緯度
                        String buildingName; // 建築物名稱
                        String buildingCategory; // 建築物類別

                        for (Map.Entry<String, ArrayList<Object>> bloc: bLocation.entrySet()){ // 同時取得建築物的name(key)和人數、location(value)、類別
                            // 對建築物經緯度的字串處理，轉為double型態
                            String[] spiltBloc = bloc.getValue().get(1).toString().split(", "); // 經緯度
                            Double lat_of_bloc = Double.parseDouble(spiltBloc[0].trim());
                            Double long_of_bloc = Double.parseDouble(spiltBloc[1].trim());

                            marker = new Marker(map); // 設置新的marker物件
                            buildingLoc = new GeoPoint(lat_of_bloc, long_of_bloc); // 建築物的經緯度
                            buildingName = bloc.getKey(); // 建築物的名稱
                            buildingCategory = (String) bloc.getValue().get(2); // 建築物的類別

                            marker.setPosition(buildingLoc); // 設置標點的位置
                            marker.setTitle(buildingName); // 設置標點的建築物名稱

                            // 判斷該建築物的人流狀況
                            int degree_of_building = (int) bloc.getValue().get(0); // 密度
                            String flow_of_building; // 人流(ex:正常、擁擠)

                            if (degree_of_building>=0 && degree_of_building <= 2){
                                marker.setIcon(normal); // 設置標點的人流狀況為正常
                                flow_of_building = "正常";
                            }
                            else if (degree_of_building>2 && degree_of_building<=5){
                                marker.setIcon(some); // 設置標點的人流狀況為適中
                                flow_of_building = "略多";
                            }
                            else{
                                marker.setIcon(crowded); // 設置標點的人流狀況為擁擠
                                flow_of_building = "擁擠";
                            }

                            // marker的點擊事件
                            String cardView_BuildingName = buildingName; // 展館簡介卡:展館名稱
                            String cardView_BuildingCategory = buildingCategory; // 展館簡介卡:展館類別
                            String cardView_BuildingFlow = flow_of_building; // 展館簡介卡:展館人流狀況
                            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                        mapController.animateTo(marker.getPosition()); // 將點選到的marker移到中心點(有滑動動畫)
                                        intro_cardView.setVisibility(View.VISIBLE); // 簡介卡設為可視
                                        cardView_title.setText(cardView_BuildingName); // 設置簡介卡標題文字(展館名稱)
                                        cardView_subTitle.setText(cardView_BuildingCategory); // 設置簡介卡副標題文字(展館類別)
                                        cardView_content.setText("人流狀況\n"+"- "+cardView_BuildingFlow); // 設置簡介卡內容文字(展館人流狀況)

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
        btn_reset_map_to_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.setZoom(18);
                mapController.animateTo(userLocation);
                intro_cardView.setVisibility(View.GONE); // 簡介卡設為不可視
            }
        });
    }

    private void refresh_map(String exName){
        btn_refresh_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map_thread(exName);
                intro_cardView.setVisibility(View.GONE); // 簡介卡設為不可視
            }
        });
    }

    private void updateTime(){
        nowTime = view.findViewById(R.id.nowTime);
        String dateFormat = "yyyy/MM/dd HH:mm:ss"; //日期的格式
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        String today = df.format(mCal.getTime());
        nowTime.setText(today);
    }

    private void timeThread(){
        updateTime();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTime();
                            }
                        });
                    }

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        };
        t.start();
    }

}
