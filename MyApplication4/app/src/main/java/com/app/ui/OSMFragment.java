package com.app.ui;

import androidx.fragment.app.Fragment;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.app.LocationUtil;
import com.app.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OSMFragment extends Fragment {

	private static final double MAP_LAT = 34.2338;
	private static final double MAP_LON = 133.6354;
	private static final double MAP_ZOOM = 15.0;

	private MapView mMapView;

	private Marker mMarker;
	private Button mButton;

	private LocationUtil mLocationUtil;
	private GeoPoint mCenterPoint;

	/**
	 * アダプターで管理するためにインスタンスを生成して返す
	 */
	static OSMFragment newInstance(){
		OSMFragment fragment = new OSMFragment();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		View v = inflater.inflate(R.layout.fragment_map, null);
		mButton = v.findViewById(R.id.button);

		mMapView = v.findViewById(R.id.mapview);
		mMapView.setDestroyMode(false);

		mLocationUtil = new LocationUtil();

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

		// ネットからタイルソースをとってこない
		mMapView.setUseDataConnection(false);

		// タイルソースを設定する
		mMapView.setTileSource(new XYTileSource("OSMPublicTransport", 15, 15, 256, ".jpg", new String[]{""}));
		// マップを更新する
		mMapView.invalidate();

		// 現在地を表示
		MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(Objects.requireNonNull(getContext())), mMapView);
		myLocationOverlay.enableMyLocation();		// 現在地にマーカーを表示する
		myLocationOverlay.enableFollowLocation();	// 現在地に画面をスナップする
		mMapView.getOverlays().add(myLocationOverlay);

		// osmのcopyrightを表示
		CopyrightOverlay mCopyrightOverlay = new CopyrightOverlay(getContext());
		mMapView.getOverlays().add(mCopyrightOverlay);

		// コンパス表示
		CompassOverlay mCompassOverlay = new CompassOverlay(getContext(), new InternalCompassOrientationProvider(getContext()), mMapView);
		mCompassOverlay.enableCompass();
		mMapView.getOverlays().add(mCompassOverlay);

		// 回転ジェスチャーを有効にする
		/*RotationGestureOverlay mRotationOverlay = new RotationGestureOverlay(mMapView);
		mRotationOverlay.setEnabled(true);
		mMapView.getOverlays().add(mRotationOverlay);*/

		// ピンチでズーム
		mMapView.setMultiTouchControls(true);
		// 地図上の文字をスケーリングする
		mMapView.setTilesScaledToDpi(true);
		// ズームボタンを非表示にする
		mMapView.setBuiltInZoomControls(false);

		// 初期のカメラ位置を決める
		IMapController mMapController = mMapView.getController();
		mMapController.setZoom(MAP_ZOOM);
		mCenterPoint = new GeoPoint(MAP_LAT, MAP_LON);
		mMapController.setCenter(mCenterPoint);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 現在地に画面をスナップする
				IMapController mapController = mMapView.getController();
				Location location = mLocationUtil.getLocation();
				if(location != null) {
					mCenterPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
				}
				mapController.setCenter(mCenterPoint);
				array.clear();
			}
		});


		mMarker = new Marker(mMapView);

		// マーカーのクリックリスナーを設定する
		mMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker, MapView mapView) {
				return false;
			}
		});

		Drawable image = getResources().getDrawable(R.drawable.marker, null);
		mMarker.setIcon(image);

		array = new ArrayList<>();
		polyline = new Polyline(mMapView);
		polyline.setColor(0xFF00BFFF);

		// シングルタップのイベントを設定する
		MapEventsReceiver mEventsReceiver = new MapEventsReceiver() {
			@Override
			public boolean singleTapConfirmedHelper(GeoPoint p) {
				mMapView.getOverlays().remove(mMarker);
				mMarker.setPosition(p);
				mMapView.getOverlays().add(mMarker);
				mMapView.invalidate();
				array.add(p);
				return true;
			}

			@Override
			public boolean longPressHelper(GeoPoint p) {
				for(GeoPoint geo:array){
					polyline.addPoint(geo);
				}
				mMapView.getOverlays().add(polyline);
				array.clear();
				return true;
			}
		};
		MapEventsOverlay mEventsOverlay = new MapEventsOverlay(mEventsReceiver);
		mMapView.getOverlays().add(mEventsOverlay);
	}

	List<GeoPoint> array;
	Polyline polyline;
	@Override
	public void onPause(){
		super.onPause();

		if(mMapView != null){
			mMapView.onPause();
		}
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		mMapView.onDetach();
	}

	@Override
	public void onResume() {
		super.onResume();

		if(mMapView != null){
			mMapView.onResume();
		}
	}
}
