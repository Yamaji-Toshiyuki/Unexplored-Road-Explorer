package com.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class StartActivity extends AppCompatActivity {

	private final int REQUEST_CODE_PERMISSION = 2000;
	private final int REQUEST_CODE_ACTIVITY = 3000;

	private TextView text1;
	private TextView text2;
	private TextView text3;
	private ImageView logo;

	private EditText editText;

	private TextView userName;
	private AlertDialog dialog;
	private boolean isEntry = false;

	@Override
	protected void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);

		// 権限がない場合、許可ダイアログを表示
		// 位置情報
		boolean locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
		// カメラ
		boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
		// 外部ストレージ
		boolean storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
		if(locationPermission || cameraPermission || storagePermission){
			String[] permission = {
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.CAMERA,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
			};
			ActivityCompat.requestPermissions(this, permission, REQUEST_CODE_PERMISSION);
		}

		setContentView(R.layout.activity_start);
		text1 = findViewById(R.id.text1);
		text2 = findViewById(R.id.text2);
		text3 = findViewById(R.id.text3);
		logo = findViewById(R.id.logo);

		ImageButton userReset = findViewById(R.id.user_reset);
		userReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialog().show();
			}
		});

		// サーバーのURLを決める
		setRequestURL();

		// ユーザー登録が済んでいるか確認する
		isEntry = readUsername();
	}

	private AlertDialog createDialog(){
		editText = new EditText(this);
		editText.setHint("user name");
		// ユーザー名を保存する
		dialog = new AlertDialog.Builder(this)
				.setTitle("ユーザー名を入力してください")
				.setView(editText)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// サーバーにユーザー名を送る
						pushVolley(String.valueOf(editText.getText()));

						ViewGroup viewGroup = (ViewGroup) editText.getParent();
						viewGroup.removeView(editText);
					}
				})
				.create();
		dialog.setCanceledOnTouchOutside(false);

		return dialog;
	}

	/**
	 * ユーザー名とユーザーIDをファイルに書き込む
	 */
	protected void writeUsername(String username, String userid){
		// output user name
		try {
			FileOutputStream out_name = openFileOutput("user_name", MODE_PRIVATE);
			out_name.write(username.getBytes());
			out_name.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// output user id
		try {
			FileOutputStream out_id = openFileOutput("user_id", MODE_PRIVATE);
			out_id.write(userid.getBytes());
			out_id.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ユーザーIDがあるか確認して表示する
	 * ない場合は登録用ダイアログを表示させる
	 */
	protected boolean readUsername(){
		// ユーザー名を取得して表示する
		try {
			FileInputStream input = openFileInput("user_id");
			userName = findViewById(R.id.user_name);

			byte[] buffer = new byte[128];
			if(input.read(buffer) != 0){
				userName.setText(new String(buffer).trim());
			}

			input.close();
			return true;
		} catch (FileNotFoundException e) {
			// ユーザー名のファイルがなかったらダイアログを表示する
			text3.setText(null);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * サーバーと通信する
	 * @param username 登録用
	 */
	protected void pushVolley(final String username) {
		// サーバーのアドレス
		String GET_URL = "http://" + getURL() + "/register/" + username;
		// リクエストキュー
		RequestQueue getQueue = Volley.newRequestQueue(this);

		// Volleyによる通信開始
		// (GETかPOST、サーバーのURL、受信メソッド、エラーメソッド)
		StringRequest mRequest = new StringRequest(Request.Method.GET, GET_URL,
				// 通信成功
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						// ユーザー名を保存する
						writeUsername(username, response);
						isEntry = true;
					}
				},
				// 通信失敗
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("err", "error response volley", error);
						isEntry = false;
					}
				});

		getQueue.add(mRequest);
	}

	private String getURL(){
		try {
			FileInputStream input = openFileInput("path");

			byte[] buffer = new byte[128];
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

	private void setRequestURL(){
		try {
			FileOutputStream out = openFileOutput("path", MODE_PRIVATE);
			out.write("192.168.11.16:5001".getBytes());
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 画面のタッチイベントを取得
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		// 画面から指が離されたら画面を遷移
		if(event.getAction() == MotionEvent.ACTION_UP) {
			if(isEntry){
				Intent intent = new Intent(this, MainActivity.class);
				startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
				release();
			}
			else {
				// ユーザー登録が済んでいないならダイアログを表示
				createDialog().show();
			}
		}
		return true;
	}

	/**
	 * メモリが不足するのでリリースする
	 */
	private void release(){
		logo.setImageDrawable(null);
		text1.setBackground(null);
		text2.setText(null);
		text3.setText(null);
		userName.setText(null);
		dialog = null;
	}

	/**
	 * パーミッションリクエストの結果を受け取る
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults){
		if(requestCode == REQUEST_CODE_PERMISSION) {
			for(int i = 0; i < permission.length; i++) {
				if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
					finish();
				}
			}
		}
	}

	/**
	 * startActivityForResultの結果を受け取る
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_ACTIVITY){
			// 終了ボタンを押していたら終了する
			if(data.getBooleanExtra("finish", false)){
				finish();
			}
		}
	}

}
