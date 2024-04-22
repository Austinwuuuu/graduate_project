package com.example.myproject;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class selectPreferenceFragment extends Fragment {

    View v;

    // back button
    private Button btn_back;

    // confirm button
    private Button confirm_btn;

    // 呈現在UI上的偏好值
    ArrayList<String> pData;
    // 用來接db的展區值
    ArrayList<String> data;

    // 使用者點選的偏好
    List<String> userCategoryPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_select_preference, container, false);

        // 初始化pData的arraylist
        pData = new ArrayList<>();

        // 將使用者點選的偏好值list做初始化
        userCategoryPreferences = new ArrayList<>();

        // 使用者所選展區
        String exName = ((MainActivity)getActivity()).getExhibitionData();

//        if (!exName.equals("國立高雄大學")){ //　如果選擇不是高雄大學，就跳出尚未與此展區合作的訊息
//            Toast.makeText(getActivity(), "尚未與此展區合作!", Toast.LENGTH_SHORT).show();
//        }

        // DB取值之執行緒
        new Thread(new Runnable(){
            @Override
            public void run(){
                db con = new db();
                con.run(); // 連接資料庫
                data = new ArrayList<>(con.getPreference(exName)); // 取偏好值
                pData = data;

                // find id of listview
                ListView listView = v.findViewById(R.id.listview_preference);

                // 把取出的值放入listView
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        // listView調配器，把取出的值放入listView裡面顯示出來
                        MyAdapter adapter = new MyAdapter(getContext(), pData);
                        listView.setAdapter(adapter);

                        // listView的點擊事件
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                String text = pData.get(position); // 點選到的分類

                                // 新增所選到偏好list
                                if (!userCategoryPreferences.contains(text)) {
                                    userCategoryPreferences.add(text); // 新增所選的item到userCategoryPreferences
                                    view.setSelected(true); // 設置所選的按鈕為實心
                                }
                                else{
                                    userCategoryPreferences.remove(text); // 移除所選的item
                                    view.setSelected(false);
                                }

                            }
                        });

                    }
                });
            }
        }).start();

        // 確認按鈕
        confirm_btn = v.findViewById(R.id.confirmButton);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (userCategoryPreferences.size()==0){ // 使用者沒有輸入偏好
                        Toast.makeText(getActivity(), "尚未選擇偏好!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        // 設定使用者偏好
                        ((MainActivity) getActivity()).setUserPreferences(userCategoryPreferences);

                        // 跳轉至選擇結果頁面
                        replace(new showSelectResultFragment());
                    }
            }
        });

        // 回上頁按鈕
        btn_back = v.findViewById(R.id.backButton);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 跳轉回選擇展區
                FragmentManager fm = getActivity().getSupportFragmentManager(); // 呼叫getSupportFragmentManager()的方法來取得FragmentManager
                fm.beginTransaction().replace(R.id.framelayout, new selectExibitionFragment()).commit(); // 跳轉至選擇偏好頁面

                // 把展區值清空
                ((MainActivity)getActivity()).setExhibitionData("");
                // 把偏好值清空
                userCategoryPreferences.clear();
                ((MainActivity)getActivity()).setUserPreferences(userCategoryPreferences);
            }
        });

        return v;
    }

    static class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> rData;

        MyAdapter(Context c, ArrayList<String> data){
            super(c, R.layout.list_item_preferance, R.id.TextView, data);
            this.context = c;
            this.rData = data;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View list_item = layoutInflater.inflate(R.layout.list_item_preferance, parent, false);
            TextView myData = list_item.findViewById(R.id.TextView);

            // set our own resources on views
            myData.setText(rData.get(position));

            return list_item;
        }
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

}