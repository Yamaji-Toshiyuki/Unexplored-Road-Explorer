package com.app.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class LocationNewOverlayUtil extends MyLocationNewOverlay {

	private Location mLocation;
	private boolean isDebug = false;
	private SharedPreferencesUtil util;
	private List<GeoPoint> list = new ArrayList<>();

	public LocationNewOverlayUtil(MapView mapView){
		this(new GpsMyLocationProvider(mapView.getContext()), mapView);
	}

	public LocationNewOverlayUtil(IMyLocationProvider myLocationProvider, MapView mapView) {
		super(myLocationProvider, mapView);
	}

	@Override
	public void onLocationChanged(final Location location, IMyLocationProvider source) {
		if(util != null){
			this.mLocation = util.getLatLon();
		}

		if(isDebug && this.mLocation != null){
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
	public void setMyLocation(Context context, boolean flag){
		this.util = new SharedPreferencesUtil(context);
		this.isDebug = flag;
	}

	public void setMyLocation(List<GeoPoint> points, Context context, boolean flag){
		this.list = points;
		this.util = new SharedPreferencesUtil(context);
		this.isDebug = flag;
	}
}
