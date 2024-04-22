package com.example.myproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.JsonUtils;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class showSelectResultFragment extends Fragment {

    View view;

    TextView text_preference;
    TextView text_exhibition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_show_select_result, container, false);


        text_exhibition = view.findViewById(R.id.text_exhibition);
        text_preference = view.findViewById(R.id.text_preference);

        //*** 設定選擇結果頁面的文字 ***//
        // 展區
        String txt1 = ((MainActivity)getActivity()).getExhibitionData();
        text_exhibition.setText(txt1);
        // 偏好
        StringBuilder str = new StringBuilder();
        List<String> arrayListOfText2 = ((MainActivity)getActivity()).getUserPreferences();
        for (String s : arrayListOfText2){
            str.append(s+"\n");
        }
        text_preference.setText(str);

        // 重新選擇按鈕
        Button reselect = view.findViewById(R.id.backButton);
        reselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳轉回選擇展區
                FragmentManager fm = getActivity().getSupportFragmentManager(); // 呼叫getSupportFragmentManager()的方法來取得FragmentManager
                fm.beginTransaction().replace(R.id.framelayout, new selectExibitionFragment()).commit(); // 跳轉至選擇偏好頁面

                // 把展區值都清空
                ((MainActivity)getActivity()).setExhibitionData("");
                // 把偏好值清空
                ((MainActivity)getActivity()).getUserPreferences().clear();
            }
        });

        return view;
    }
}