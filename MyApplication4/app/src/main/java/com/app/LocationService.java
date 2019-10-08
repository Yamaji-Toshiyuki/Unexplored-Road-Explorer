package com.app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.util.VariableUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.FileInputStream;
import java.io.IOException;

public class LocationService extends Service {

	private FusedLocationProviderClient mLocation = null;

	private MyLocationCallBack mCallBack;

	@Override
	public void onCreate(){
		super.onCreate();

		// LocationClient クラスのインスタンスを作成
		mLocation = LocationServices.getFusedLocationProviderClient(this);

		// 位置情報取得開始
		startUpdateLocation();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(intent != null){
			VariableUtil variableUtil = (VariableUtil) intent.getSerializableExtra(VariableUtil.SERIAL_NAME);

			if(variableUtil.getIsForeground()){
				String title = this.getString(R.string.app_name);

				String chID = "channelID";
				NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chID);
				Notification notification = builder
						.setDefaults(0)
						.setContentTitle(title)
						.setContentText("logging in")
						.setSmallIcon(R.mipmap.icon)
						.build();

				// バックグラウンドで動作させる
				startForeground(R.string.app_name, notification);
			}
			else {
				stopForeground(STOP_FOREGROUND_REMOVE);
			}
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy(){
		stopUpdateLocation();
	}

	/**
	 * 位置情報を取得する
	 */
	public void startUpdateLocation(){
		mCallBack = new MyLocationCallBack();
		mCallBack.switchLoggingVolley(true);

		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setInterval(20000);
		locationRequest.setFastestInterval(10000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mLocation.requestLocationUpdates(locationRequest, mCallBack, null);
	}

	/**
	 * 位置情報の取得を終了する
	 */
	public void stopUpdateLocation(){
		stopForeground(STOP_FOREGROUND_REMOVE);
		mCallBack.switchLoggingVolley(false);
		mLocation.removeLocationUpdates(mCallBack);
	}

	/**
	 * 位置情報受け取りコールバッククラス
	 * LocationRequest で設定した間隔で呼ばれる
	 */
	private class MyLocationCallBack extends LocationCallback{
		// リクエストキュー
		RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("err", "error response volley", error);
				Toast.makeText(getApplicationContext(), "error response volley", Toast.LENGTH_LONG).show();
			}
		};

		// URLの断片
		String SERVER_FLASK = "http://" + getURL();
		String USER_STATE = getUserId() + "/" + getUsername();

		/**
		 * 一定間隔ごとに呼ばれる
		 */
		@Override
		public void onLocationResult(LocationResult locationResult){
			if(locationResult == null){
				return;
			}
			// 現在地を取得
			Location location = locationResult.getLastLocation();
			sendLocationVolley(location);
			Log.d("log", location.getLatitude() + "/" + location.getLongitude());
		}

		/**
		 * loggingの開始と終了
		 */
		private  void switchLoggingVolley(boolean start){
			// サーバーのURL
			String url;
			final String msg;
			if(start){
				url = SERVER_FLASK + "/logging_switch/" + USER_STATE + "/ON";
				msg = "start";
			}
			else{
				url = SERVER_FLASK + "/logging_switch/" + USER_STATE + "/OFF";
				msg = "stop";
			}
			// リクエスト
			StringRequest request = new StringRequest(Request.Method.GET, url,
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							Log.d("success", msg + " logging");
							Toast.makeText(getApplicationContext(), msg + " logging", Toast.LENGTH_LONG).show();
						}
					}, errorListener);

			queue.add(request);
		}

		/**
		 * 位置情報をサーバーに送る
		 */
		private void sendLocationVolley(Location location){
			// サーバーのURL
			String url = SERVER_FLASK + "/logging/" + USER_STATE + "/" + location.getLongitude() + "," + location.getLatitude();
			// リクエスト
			StringRequest request = new StringRequest(Request.Method.GET, url,
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
						}
					}, errorListener);

			queue.add(request);
		}

		private String getURL(){
			try {
				FileInputStream input = getApplicationContext().openFileInput("path");

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

		private String getUserId(){
			try {
				FileInputStream input = getApplicationContext().openFileInput("user_id");

				byte[] buffer = new byte[128];
				if(input.read(buffer) == 0){
					return null;
				}

				String str = new String(buffer);
				return str.trim();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		private String getUsername(){
			try {
				FileInputStream input = getApplicationContext().openFileInput("user_name");

				byte[] buffer = new byte[128];
				if(input.read(buffer) == 0){
					return null;
				}

				String str = new String(buffer);
				return str.trim();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
