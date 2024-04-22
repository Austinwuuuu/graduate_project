package com.example.myproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class viewPageFragment extends AppCompatActivity {

    ViewPager mSlideViewPager;
    LinearLayout mDotLayout;
    Button backButton, nextButton, skipButton, LineButton;

    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_view_page);

        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        LineButton = findViewById(R.id.Line_btn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getitem(0) > 0){
                    mSlideViewPager.setCurrentItem(getitem(-1), true);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getitem(0) < dots.length-1){
                    mSlideViewPager.setCurrentItem((getitem(1)), true);
                }
                else{
                    Intent intent = new Intent(viewPageFragment.this, index.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipButton.setTextColor(getColor(R.color.grey));
                Intent intent = new Intent(viewPageFragment.this, index.class);
                startActivity(intent);
                finish();
            }
        });

        mSlideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.indicator_layout);

        viewPagerAdapter = new ViewPagerAdapter(this);

        mSlideViewPager.setAdapter(viewPagerAdapter);

        // page 1
        setUpindicator(0);

        mSlideViewPager.addOnPageChangeListener(viewListener);
    }

    public void setUpindicator(int position){

        dots = new TextView[4];
        mDotLayout.removeAllViews();

        for (int i=0; i<dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);

            // set dots color to white (this page is visible)
            dots[i].setTextColor(getResources().getColor(R.color.white, getApplicationContext().getTheme()));
            mDotLayout.addView(dots[i]);
        }

        // 頁面1，不顯示back button
        if (position == 0){
            backButton.setVisibility(View.INVISIBLE);
        }

        // 頁面4，顯示Line Button
        if (position == 3){
            LineButton.setVisibility(View.VISIBLE);

            // 用Line Button連結到The Crowd的LINE官方帳號
            LineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("https://liff.line.me/1645278921-kWRPP32q/?accountId=574gurjm");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }

        //不是頁面4，不顯示Line Button
        else{
            LineButton.setVisibility(View.INVISIBLE);
        }

        // set dots color to black
        dots[position].setTextColor(getResources().getColor(R.color.black, getApplicationContext().getTheme()));
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            setUpindicator(position);

            // 除了頁面1，其他頁面都顯示back button
            if (position > 0){
                backButton.setVisibility(View.VISIBLE);
            }
            else{
                backButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private int getitem(int i) {
        return mSlideViewPager.getCurrentItem() + i;
    }

}