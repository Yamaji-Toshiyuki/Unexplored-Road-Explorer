package com.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
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

	private TextView text;
	private TextView text2;
	private ImageView logo;

	private EditText editText;
	private TextView textView;
	private AlertDialog.Builder dialog;
	private boolean isEntry = false;

	@Override
	protected void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);

		// 権限がない場合、許可ダイアログを表示
		// 位置情報
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
			String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
			ActivityCompat.requestPermissions(this, permission, 2000);
		}
		// カメラ
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			String[] permission = {Manifest.permission.CAMERA};
			ActivityCompat.requestPermissions(this, permission, 2001);
		}
		// 外部ストレージ
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
			String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
			ActivityCompat.requestPermissions(this, permission, 2002);
		}

		setContentView(R.layout.activity_start);
		text = findViewById(R.id.start_text);
		text2 = findViewById(R.id.text2);
		logo = findViewById(R.id.logo);

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
					}
				});

		isEntry = readUsername();
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
	 * ない場合は登録用ダイアログを表示する
	 */
	protected boolean readUsername(){
		// ユーザー名を取得して表示する
		try {
			FileInputStream in = openFileInput("user_id");
			textView = findViewById(R.id.user);

			byte[] buffer = new byte[128];
			in.read(buffer);
			textView.setText(new String(buffer).trim());

			in.close();
			return true;
		} catch (FileNotFoundException e) {
			// ユーザー名のファイルがなかったらダイアログを表示する
			dialog.show();
			text2.setText(null);
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
		String GET_URL = "http://192.168.11.16:5001/register/" + username;
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

	/**
	 * 画面のタッチイベントを取得
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		// 画面から指が離されたら画面を遷移
		if(event.getAction() == MotionEvent.ACTION_UP) {
			if(isEntry){
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				release();
			}
			else {
				// ユーザー登録が済んでいないならダイアログを表示
				dialog.show();
			}
		}
		return true;
	}

	/**
	 * メモリが不足するのでリリースする
	 */
	private void release(){
		if(logo != null){
			logo.setImageDrawable(null);
		}
		if(text != null){
			text.setText(null);
		}
		if(text2 != null){
			text2.setText(null);
		}
		if(textView != null){
			textView.setText(null);
		}
	}

	/**
	 * パーミッションリクエストの結果を受け取る
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults){
		if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
			switch (requestCode){
				case 2000:
					Log.d("err", "no location permission");
					finish();
				case 2001:
					Log.d("err", "no camera permission");
					finish();
				case 2002:
					Log.d("err", "no storage permission");
					finish();
			}
		}
	}

}
