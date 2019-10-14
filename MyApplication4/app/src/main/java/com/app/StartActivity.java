package com.app;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.util.InterfaceHolder;
import com.app.util.OpenDialog;
import com.app.util.SharedPreferencesUtil;
import com.app.util.VolleyToServerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class StartActivity extends AppCompatActivity implements OpenDialog {

	private final int REQUEST_CODE_PERMISSION = 2000;
	private final int REQUEST_CODE_ACTIVITY = 3000;

	private SharedPreferencesUtil util;

	private TextView text1;
	private TextView text2;
	private TextView text3;
	private ImageView logo;

	private TextView user;
	private boolean isEntry = false;

	@Override
	public void showDialog(DialogFragment dialog) {
		InterfaceHolder.set(this);
		dialog.show(getSupportFragmentManager(), "tag");
	}

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
				showDialog(new ListDialogFragment());
			}
		});

		// サーバーのURLを決める
		util = new SharedPreferencesUtil(getApplicationContext());
		util.setServerIP(util.getServerIP());

		// ユーザー登録が済んでいるか確認する
		isEntry = checkEntry();
	}

	public static class ListDialogFragment extends DialogFragment {
		@Override
		@NonNull
		public Dialog onCreateDialog(Bundle saveInstanceState){
			CharSequence[] items = {"デモンストレーション", "サーバーURLを変更", "ユーザー名を変更", "閉じる"};

			Activity activity = getActivity();

			assert activity != null;
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					InterfaceHolder.set(this);
					switch (which){
						case 0:
							Log.i("dialogFragment", "click demo");
							InterfaceHolder.get().showDialog(new DemoDialogFragment());
							dismiss();
							break;
						case 1:
							Log.i("dialogFragment", "click change url");
							InterfaceHolder.get().showDialog(new ChangeIPDialogFragment());
							break;
						case 2:
							Log.i("dialogFragment", "click change user");
							InterfaceHolder.get().showDialog(new ChangeUserDialogFragment());
							break;
						case 3:
							Log.i("dialogFragment", "click cancel");
							break;
					}
				}
			});

			return builder.create();
		}
	}

	public static class DemoDialogFragment extends DialogFragment{
		@Override
		@NonNull
		public Dialog onCreateDialog(Bundle saveInstanceState){
			AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.dialog_demo, null);

			builder.setView(view);
			builder.setMessage("デモンストレーションモードに変更しますか")
					.setPositiveButton("はい", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferencesUtil util = new SharedPreferencesUtil(getContext());
							util.setIsDemo(true);
						}
					})
					.setNegativeButton("いいえ", null);

			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);

			return dialog;
		}
	}

	public static class ChangeUserDialogFragment extends DialogFragment {
		@Override
		@NonNull
		public Dialog onCreateDialog(Bundle saveInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

			final EditText editText = new EditText(getContext());
			editText.setHint("user name");

			builder.setTitle("ユーザー名を入力してください")
					.setView(editText)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// サーバーにユーザー名を送る
							VolleyToServerUtil volley = new VolleyToServerUtil(getContext());
							volley.userAuth(String.valueOf(editText.getText()));

							ViewGroup viewGroup = (ViewGroup) editText.getParent();
							viewGroup.removeView(editText);
						}
					})
					.setNegativeButton("Cancel", null);

			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);

			return dialog;
		}
	}

	/**
	 * サーバーのURLを変更するダイアログ
	 */
	public static class ChangeIPDialogFragment extends DialogFragment {
		@Override
		@NonNull
		public Dialog onCreateDialog(Bundle saveInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
			Context context = getContext();
			final SharedPreferencesUtil util = new SharedPreferencesUtil(context);

			final EditText editText = new EditText(getContext());
			editText.setText(util.getServerIP());
			// ユーザー名を保存する
			// サーバーのURLを決める
			builder.setTitle("IPを入力してください")
					.setView(editText)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// サーバーのURLを決める
							util.setServerIP(String.valueOf(editText.getText()));

							ViewGroup viewGroup = (ViewGroup) editText.getParent();
							viewGroup.removeView(editText);
						}
					});

			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);

			return dialog;
		}
	}

	/**
	 * ユーザーIDがあるか確認して表示する
	 * ない場合は登録用ダイアログを表示させる
	 */
	protected boolean checkEntry(){
		// ユーザー名を取得して表示する
		String name = util.getUserName();
		String id = util.getUserId();
		Log.i("register", name + " / " + id);

		if(name == null || id == null){
            Log.i("register", "name or id is null");
			// ユーザー名のファイルがなかったらダイアログを表示する
			InterfaceHolder.set(this);
			InterfaceHolder.get().showDialog(new ChangeUserDialogFragment());
			text3.setText(null);
			return false;
		}
		else{
            Log.i("register", "success");
			idCheckVolley(name, id);
		}
		user = findViewById(R.id.user_id);
		user.setText(id);

		return isEntry;
	}

	/**
	 * 登録済みか確認する
	 */
	protected void idCheckVolley(final String username, final String userId){
		// サーバーのアドレス
		String GET_URL = "http://" + util.getServerIP() + "/user_auth/" + userId + "/" + username;
		// リクエストキュー
		RequestQueue getQueue = Volley.newRequestQueue(this);

		// Volleyによる通信開始
		JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.GET, GET_URL,
				// 通信成功
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// ユーザー名を保存する
						try {
							if("success".equals(response.getString("status"))){
								isEntry = true;
							}
							else{
								Log.e("err", "not found id");
								isEntry = false;
							}
						} catch (JSONException e) {
							e.printStackTrace();
							isEntry = false;
						}
					}
				},
				// 通信失敗
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("err", "error response server", error);
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
			isEntry = checkEntry();
			if(isEntry){
				Intent intent = new Intent(this, MainActivity.class);
				startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
				release();
			}
			else {
				// ユーザー登録が済んでいないならダイアログを表示
				InterfaceHolder.set(this);
				InterfaceHolder.get().showDialog(new ChangeUserDialogFragment());
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
		user.setText(null);
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
		if(requestCode == REQUEST_CODE_ACTIVITY && data != null){
			// 終了ボタンを押していたら終了する
			if(data.getBooleanExtra("finish", false)){
				finish();
			}
		}
	}
}