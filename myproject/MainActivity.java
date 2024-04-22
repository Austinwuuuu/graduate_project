
	 
	/*
	 *	This content is generated from the API File Info.
	 *	(Alt+Shift+Ctrl+I).
	 *
	 *	@desc 		
	 *	@file 		show_map
	 *	@date 		Monday 13th of March 2023 09:06:16 AM
	 *	@title 		
	 *	@author 	
	 *	@keywords 	
	 *	@generator 	Export Kit v1.3.figma
	 *
	 */
	

package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import nl.joery.animatedbottombar.AnimatedBottomBar;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


	public class MainActivity extends AppCompatActivity{

	// location
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
	private FusedLocationProviderClient fusedLocationClient;

	AnimatedBottomBar bottomBar;

	// 展區值
	String exhibitionData = "";

	// 偏好值
	String preferenceData = "";

	private double userLatitude = 0.0;
	private double userLongitude = 0.0;

	// constructor
	public MainActivity(){

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set default Fragment
		replace(new selectExibitionFragment());

		bottomBar = findViewById(R.id.bottom_bar);


		// bottom bar listener
		bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
			@Override
			// 未選中 -> 選中
			public void onTabSelected(int lastIndex, @Nullable AnimatedBottomBar.Tab lastTab, int newIndex, @NonNull AnimatedBottomBar.Tab newTab) {

				// 選中
				switch (newTab.getId()){
					case R.id.select_area:
						// 判斷展區值跟偏好值是否為空
						if (getUserPreferences().isEmpty()){
							if (getExhibitionData().equals("")) { // 偏好值空、展區值也空，則回到選擇展區
								replace(new selectExibitionFragment());
							}
							if (!getExhibitionData().equals("")) { // 偏好值空、但展區值不為空，則回到選擇偏好
								replace(new selectPreferenceFragment());
							}
						}
						// 展區值跟偏好值都不為空，則回到結果
						else {
							replace(new showSelectResultFragment());
						}
						break;
					case R.id.show_map:
						replace(new showMapFragment());
						break;
					case R.id.recommanded:
						replace(new recommandedFragment());
						break;
				}
			}

			@Override
			// 重複點擊同樣的bottomBar
			public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {

			}

		});


		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					LOCATION_PERMISSION_REQUEST_CODE);
		} else {
			getLocation();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// 在這裡處理權限請求的結果
		if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// 如果授權成功，則獲取位置資訊
				getLocation();
			}
		}
	}

	public Map<String, List<String>> getLocation() {

		Map<String, List<String>> result = null;

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.getLastLocation()
					.addOnSuccessListener(this, new OnSuccessListener<Location>() {
						@Override
						public void onSuccess(Location location) {
							if (location != null) {
								setUserLatitude(location.getLatitude());
								setUserLongitude(location.getLongitude());
//								System.out.println("Latitude: " + userLatitude + "\nLongitude: " + userLongitude);
							}
						}
					});

			// 呼叫執行緒
			taskRunner task1 = new taskRunner(getExhibitionData(), getUserPreferences(), getUserLatitude(), getUserLongitude());
			Thread t1 = new Thread(task1);
			try{
				t1.start(); // 開始
				t1.join(); // 中止
			}catch (Exception e){

			}
			
			result = task1.getRecommanded();
			
		}
		return result;
	}

	//***** setter、getter *****//
	private void setUserLatitude(double userLatitude){
		this.userLatitude = userLatitude;
	}
	public double getUserLatitude(){
		return userLatitude;
	}
	private void setUserLongitude(double userLongitude){
		this.userLongitude = userLongitude;
	}
	public double getUserLongitude(){
		return userLongitude;
	}

	// 展區值setter、getter
	public void setExhibitionData(String exhibitionData){
		this.exhibitionData = exhibitionData;
	}
	public String getExhibitionData(){
		return this.exhibitionData;
	}

	List<String> userPreferences = new ArrayList<>();
	public void setUserPreferences(List<String> userPreferences){
		this.userPreferences = userPreferences;
	}
	public List<String> getUserPreferences(){
		return userPreferences;
	}

	// 替換fragment
	private void replace(Fragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.framelayout, fragment);
		transaction.commit();
	}

}

class taskRunner implements Runnable{

		// 展區名稱
		String exhibitionData = "";

		// 使用者偏好
		List<String> userPreferences = new ArrayList<>();

		// 使用者位置經緯度
		double userLatitude;
		double userLongitude;

		// 存建築物名稱、建築物人流數量、建築物經緯度
		Map<String, ArrayList<Object>> bLocation;

		// 處理後推薦路線順序
		Map<String, List<String>> recommendations;

		// constructor
		public taskRunner(String exhibitionData, List<String> userPreferences, double userLatitude, double userLongitude){
			this.exhibitionData = exhibitionData;
			this.userPreferences = userPreferences;
			this.userLatitude = userLatitude;
			this.userLongitude = userLongitude;
		}

		@Override
		public void run() {
			// 連DB，取展區經緯度
			db con = new db();
			con.run();
			bLocation = new HashMap<>();
			bLocation = con.getBuildingLocation(exhibitionData); // 展區內建築物

			Map<String, List<Exhibition>> exhibitionDict = new HashMap<>(); //建立Map，用來執行後續的順序值

			// ("building_name" = "degree","經緯度","建築分類")
			for (Map.Entry<String, ArrayList<Object>> bloc: bLocation.entrySet()) { // 同時取得建築物的name(key)和人數、location(value)、category
				int degree = (int) bloc.getValue().get(0); // degree值(轉int)
				// 對建築物經緯度的字串處理，轉為double型態
				String[] spiltBloc = bloc.getValue().get(1).toString().split(", "); // 經緯度
				Double lat_of_bloc = Double.parseDouble(spiltBloc[0].trim()); // 緯度
				Double long_of_bloc = Double.parseDouble(spiltBloc[1].trim()); // 經度
				String category = bloc.getValue().get(2).toString(); // 建築分類
				// 把建築名稱、degree、經緯度存進exhibitionDict
				exhibitionDict.put(bloc.getKey(), new ArrayList<>(Arrays.asList(
						new Exhibition(bloc.getKey(), lat_of_bloc, long_of_bloc,degree, category)
				)));
			}

			Map<String, List<String>> recommendedOrder = recommendVisitOrder(userLatitude, userLongitude, exhibitionDict, userPreferences);

			// Output the recommended visit order
			recommendations = recommendedOrder;



		}

		public Map<String, List<String>> getRecommanded(){
			return recommendations;
		}

		private static Map<String, List<String>> recommendVisitOrder(double userLatitude, double userLongitude,
																	 Map<String, List<Exhibition>> exhibitionDict, List<String> userPreferences) {
			List<ExhibitionScore> scores = new ArrayList<>();

			for (Map.Entry<String, List<Exhibition>> entry : exhibitionDict.entrySet()) {
				List<Exhibition> exhibitions = entry.getValue();

				for (Exhibition exhibition : exhibitions) {
					double distance = calculateDistance(userLatitude, userLongitude, exhibition.getLatitude(),
							exhibition.getLongitude());

					double preferenceRank=0;//設定偏好分數的變數
					for(int i=0 ; i<userPreferences.size() ; i++){//逐一讀取使用者剛剛所輸入的偏好
						String category = userPreferences.get(i);//將偏好存入category變數中
						if(exhibition.getCategory().equals(category)){//判斷目前這個展區的類別是否符合使用者剛剛所選擇的偏好之一
							preferenceRank=preferenceRank+0.1;//若符合則給予偏好分數，沒有則不跑這段，維持preferenceRank=0
						}
					}

					double degreeScore = (1.0 / ((Math.round((exhibition.getdegree()+1.0)*1000.0)/1000.0))); //計算人潮擁擠分數
					double distanceScore = 1.0 / ((Math.round((distance+1.0)*1000.0)/1000.0)); //計算距離分數
					double preferenceScore = 0;
					if(preferenceRank!=0){
						preferenceScore = preferenceRank;
					}

					double weightedScore = (0.1 * Math.round(distanceScore*1000.0)/1000.0) + (0.2 * preferenceScore) + (0.7 * Math.round(degreeScore*100000.0)/100000.0); //加權計算取得總分
					scores.add(new ExhibitionScore(exhibition.getName(), weightedScore, exhibition.getLatitude(), exhibition.getLongitude(), exhibition.getCategory()));
				}
			}

			Collections.sort(scores);

			Map<String ,List<String>> recommendedMap = new HashMap<>();
			List<String> recommendedOrder;
			int i=0;
			for (ExhibitionScore score : scores) {
				i++;
				recommendedOrder = new ArrayList<>();
				recommendedOrder.add(String.valueOf(i)); // 順序值
				recommendedOrder.add(String.valueOf(score.getbLat())); // 緯度
				recommendedOrder.add(String.valueOf(score.getbLong())); // 經度
				recommendedOrder.add(String.valueOf(score.getCategory())); // 展館類別

				recommendedMap.put(score.getName(), recommendedOrder);
			}

			//將總分排序並回傳
			return recommendedMap;
		}

		private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {//計算各展區與使用者距離
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
					+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515 * 1.609344; // Convert to kilometers
			return dist;
		}
}

class Exhibition { //Map內容
	private String name;
	private double latitude;
	private double longitude;
	private int degree;
	private String category;//添加一個展區分類

	public Exhibition(String name, double latitude, double longitude, int degree, String category) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.degree = degree;
		this.category = category;//設定展區的分類
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getdegree() {
		return degree;
	}

	public String getCategory() {
		return category;//回傳展區的分類
	}
}

class ExhibitionScore implements Comparable<ExhibitionScore> {
	private String name;
	private double score;

	private double bLat; // 建築經緯度
	private double bLong;
	private String category;

	public ExhibitionScore(String name, double score, double bLat, double bLong, String category) {
		this.name = name;
		this.score = score;
		this.bLat = bLat;
		this.bLong = bLong;
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public double getScore() {
		return score;
	}

	public double getbLat(){
		return bLat;
	}
	public double getbLong(){
		return bLong;
	}
	public String getCategory(){
		return category;
	}

	@Override
	public int compareTo(ExhibitionScore other) {
		return Double.compare(other.score, this.score);
	}
}

	