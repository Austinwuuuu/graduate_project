

	/*
	 *	This content is generated from the API File Info.
	 *	(Alt+Shift+Ctrl+I).
	 *
	 *	@desc
	 *	@file 		android_small___1
	 *	@date 		Sunday 14th of May 2023 02:50:53 PM
	 *	@title 		Page 1
	 *	@author
	 *	@keywords
	 *	@generator 	Export Kit v1.3.figma
	 *
	 */


	package com.example.myproject;

	import android.content.Intent;
	import android.os.Bundle;


	import android.view.View;
	import android.widget.Button;
	import android.widget.ImageButton;

	import androidx.appcompat.app.AppCompatActivity;

	public class index extends AppCompatActivity {


		Button index_button;
		ImageButton tipButton;

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			setContentView(R.layout.index);

			// 前往導覽頁面按鈕
			tipButton = findViewById(R.id.tipButton);
			tipButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent();
					intent.setClass(index.this, viewPageFragment.class);
					startActivity(intent);
				}
			});

			// 進入APP按鈕
			index_button = findViewById(R.id.index_button);
			index_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent();
					intent.setClass(index.this, MainActivity.class);
					startActivity(intent);
				}
			});

		}
	}
