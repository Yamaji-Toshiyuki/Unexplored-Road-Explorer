package com.app.util;

import android.location.Criteria;
import android.location.Location;
import android.util.Log;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class LocationNewOverlayUtil extends MyLocationNewOverlay {

	private Location mLocation;
	private boolean isDebug = false;

	public LocationNewOverlayUtil(MapView mapView) {
		this(new GpsMyLocationProvider(mapView.getContext()), mapView);
	}

	public LocationNewOverlayUtil(IMyLocationProvider myLocationProvider, MapView mapView) {
		super(myLocationProvider, mapView);
	}

	@Override
	public void onLocationChanged(final Location location, IMyLocationProvider source) {
		// TODO SharedPreferencesから位置情報を持ってくる
		Log.w("LocationOverlay", "location change");
		if(isDebug && this.mLocation != null){
			Log.w("LocationOverlay", "location change to fake");
			super.onLocationChanged(this.mLocation, source);
		}
		else{
			super.onLocationChanged(location, source);
		}
	}

	/**
	 * デバッグ用
	 * @param flag trueにするとデバッグモードになる
	 */
	public void setMyLocation(Location location, boolean flag){
		Log.w("LocationOverlay", "set fake location");
		this.mLocation = location;
		this.isDebug = flag;
	}
}
