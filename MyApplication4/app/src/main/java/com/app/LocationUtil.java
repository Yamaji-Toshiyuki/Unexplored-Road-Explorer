package com.app;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class LocationUtil implements LocationListener {

	private Location mLocation;

	/**
	 * ゲッター
	 */
	public Location getLocation() {
		return mLocation;
	}

	/**
	 * セッター
	 * 現在地が変わると呼ばれる
	 */
	@Override
	public void onLocationChanged(Location location) {
		mLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
}
