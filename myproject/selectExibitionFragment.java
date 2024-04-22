package com.example.myproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class selectExibitionFragment extends Fragment {

	// 新增data
	ArrayList<String> mData; // 要呈現在UI上的展區值
	ArrayList<String> data; // 用來接db的展區值

	// 所選擇的展區 (要傳到地圖頁面用的)
	private String selected_exibition;

	View v;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		v =  inflater.inflate(R.layout.fragment_select_exibition, container, false);

		// 初始化mData的arraylist
		mData = new ArrayList<>();

		// DB取值之執行緒
		new Thread(new Runnable(){
			@Override
			public void run(){
				db con = new db();
				con.run(); // 連接資料庫
				data = new ArrayList<>(con.getExname()); // 取展區值
				mData = data;

				// find id of listview
				ListView listView = v.findViewById(R.id.listview);
				// 把取出的值放入listView
				listView.post(new Runnable() {
					@Override
					public void run() {
						// listView調配器，把取出的值放入listView裡面顯示出來
						MyAdapter adapter = new MyAdapter(getContext(), mData);
						listView.setAdapter(adapter);

						// listView的點擊事件
						listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

								selected_exibition = mData.get(position);

								// 傳值到main activity的exhibition setter
								((MainActivity)getActivity()).setExhibitionData(selected_exibition);

								// 跳轉至選擇偏好頁面
								replace(new selectPreferenceFragment());
							}
						});

					}
				});
			}
		}).start();

		return v;
	}

	// 換頁function
	private void replace(Fragment nextFragment) {

		if (nextFragment != null){
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			Fragment PreviousFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.framelayout); // 獲取當前fragment

			if (PreviousFragment != null) {
				transaction.hide(PreviousFragment); // 隱藏目前fragment的狀態
				transaction.addToBackStack(PreviousFragment.getTag()); // 添加至回退棧(可以從下一頁回退回來)
			}

			if (!nextFragment.isAdded()){ // 判斷是否添加過
				transaction.add(R.id.framelayout, nextFragment);
			}

			transaction.commit();
		}

	}


	static class MyAdapter extends ArrayAdapter<String>{

		Context context;
		ArrayList<String> rData;

		MyAdapter(Context c, ArrayList<String> data){
			super(c, R.layout.list_item, R.id.TextView, data);
			this.context = c;
			this.rData = data;
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View list_item = layoutInflater.inflate(R.layout.list_item, parent, false);
			TextView myData = list_item.findViewById(R.id.TextView);

			// set our own resources on views
			myData.setText(rData.get(position));

			return list_item;
		}
	}

}
