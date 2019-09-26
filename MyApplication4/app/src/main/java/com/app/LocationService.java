package com.app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

	private FusedLocationProviderClient mLocation = null;

	@Override
	public void onCreate(){
		super.onCreate();

		// LocationClient クラスのインスタンスを作成
		mLocation = LocationServices.getFusedLocationProviderClient(this);

		String title = this.getString(R.string.app_name);

		String chID = "channelID";
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chID);
		Notification notification = builder
				.setDefaults(0)
				.setContentTitle(title)
				.setContentText("start")
				.setSmallIcon(R.drawable.ic_launcher_foreground)
				.build();

		// バックグラウンドで動作させる
		startForeground(R.string.app_name, notification);
		// 位置情報取得開始
		startUpdateLocation();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		return START_NOT_STICKY;
	}

	/**
	 * 位置情報を取得する
	 */
	public void startUpdateLocation(){
		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setInterval(20000);
		locationRequest.setFastestInterval(10000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mLocation.requestLocationUpdates(locationRequest, new MyLocationCallBack(), null);
	}

	/**
	 * 位置情報受け取りコールバッククラス
	 * LocationRequest で設定した間隔で呼ばれる
	 */
	private class MyLocationCallBack extends LocationCallback{
		@Override
		public void onLocationResult(LocationResult locationResult){
			if(locationResult == null){
				return;
			}
			// 現在地を取得
			Location location = locationResult.getLastLocation();
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
