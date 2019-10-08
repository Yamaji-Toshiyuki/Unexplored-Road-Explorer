package com.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.util.VariableUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OptionActivity extends AppCompatActivity {

	private Context mContext;

	private EditText mURL;

	private Intent intent;
	private VariableUtil variableUtil;

	private int debugCount;
	private TableRow debugRow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option);

		debugCount = -1;
		debugRow = findViewById(R.id.secret_row);
		float fontSize = setTextSize();

		intent = getIntent();
		variableUtil = (VariableUtil) intent.getSerializableExtra(VariableUtil.SERIAL_NAME);

		// トグルスイッチのインスタンス生成
		Switch toggleSwitch = findViewById(R.id.switch2);
		toggleSwitch.setChecked(variableUtil.getIsForeground());
		toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				variableUtil.setIsForeground(isChecked);
				if (debugCount > 0){
					debugCount++;
				}
				if(debugCount > 10){
					debugRow.setVisibility(View.VISIBLE);
				}
			}
		});

		float scale = getResources().getDisplayMetrics().density;
		int size;
		if(Build.VERSION.SDK_INT <= 24){
			size = (int)(50 * scale);
		}
		else{
			size = (int)(70 * scale);
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);

		intent = new Intent();
		// 戻るボタンを押したら戻る
		ImageButton back = findViewById(R.id.button_back);
		back.setLayoutParams(params);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent.putExtra("isLocationServer", variableUtil.getIsLocationService());
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		// LocationServiceを止めるボタン
		Button stopServiceButton = findViewById(R.id.stop_service);
		stopServiceButton.setTextSize(fontSize - ((fontSize - 20) / 2));
		stopServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// serviceを停止する
				variableUtil.setIsLocationService(false);
			}
		});

		// Applicationを終了させるボタン
		mContext = this;
		Button stopApplicationButton = findViewById(R.id.stop_application);
		stopApplicationButton.setTextSize(fontSize - ((fontSize - 20) / 2));
		stopApplicationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 確認ダイアログ
				new AlertDialog.Builder(mContext)
						.setTitle("アプリケーションを終了しますか?")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent();
								if(!variableUtil.getIsForeground()){
									intent.putExtra("isForeground", variableUtil.getIsForeground());
								}
								// 終了する
								intent.putExtra("isApplicationStop", true);
								setResult(RESULT_OK, intent);
								finish();
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								debugCount = 1;
							}
						})
						.create()
						.show();
			}
		});

		mURL = findViewById(R.id.edit_text);
		mURL.setText(getURL());
		mURL.setTextSize(fontSize);

		Button editReloadButton = findViewById(R.id.button_edit_reload);
		editReloadButton.setTextSize(fontSize - 5);
		editReloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setURL("192.168.11.16:5001");
				mURL.setText(getURL());
			}
		});

		Button editSetButton = findViewById(R.id.button_edit_set);
		editReloadButton.setTextSize(fontSize - 5);
		editSetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setURL(mURL.getText().toString());
				mURL.setText(getURL());
			}
		});
	}

	private float setTextSize(){
		float titleFontSize;
		float bodyFontSize;
		if(Build.VERSION.SDK_INT <= 24){
			titleFontSize = 20;
			bodyFontSize = 15;
		}
		else {
			titleFontSize = 30;
			bodyFontSize = 25;
		}

		TextView title1 = findViewById(R.id.title1);
		TextView title2 = findViewById(R.id.title2);
		TextView title3 = findViewById(R.id.title3);

		title1.setTextSize(titleFontSize);
		title2.setTextSize(titleFontSize);
		title3.setTextSize(titleFontSize);

		TextView body1 = findViewById(R.id.body1);
		TextView body2 = findViewById(R.id.body2);

		body1.setTextSize(bodyFontSize);
		body2.setTextSize(bodyFontSize);

		return titleFontSize;
	}

	private String getURL(){
		try {
			FileInputStream input = openFileInput("path");

			byte[] buffer = new byte[input.available()];
			if(input.read(buffer) == 0){
				return "192.168.11.16:5001";
			}

			String str = new String(buffer);
			return str.trim();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "192.168.11.16:5001";
	}

	public void setURL(String url){
		try{
			FileOutputStream output = openFileOutput("path", MODE_PRIVATE);
			output.write(url.getBytes());
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}