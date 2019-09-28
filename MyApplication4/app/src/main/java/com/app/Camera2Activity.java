package com.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.app.fragment.Camera2Fragment;

public class Camera2Activity extends AppCompatActivity {

	private Camera2Fragment fragment;

	@Override
	protected void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		setContentView(R.layout.activity_camera);

		// 撮影用フラグメントを呼ぶ
		fragment = Camera2Fragment.newInstance();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit();


		// 戻るボタンを押したら戻る
		ImageButton back = findViewById(R.id.button_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onPause(){
		super.onPause();
		// フラグメントの終了
		getSupportFragmentManager().beginTransaction().remove(fragment).commit();
	}
}
