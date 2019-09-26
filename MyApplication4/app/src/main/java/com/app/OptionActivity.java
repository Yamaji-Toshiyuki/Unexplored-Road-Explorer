package com.app;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class OptionActivity extends AppCompatActivity {

	private Button s;
	private Switch s2;

	private Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option);

		// 戻るボタンを押したら戻る
		ImageButton back = findViewById(R.id.button_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// トグルスイッチのインスタンス生成
		s =  findViewById(R.id.switch1);
		s2 = (Switch) findViewById(R.id.switch2);


	}
}