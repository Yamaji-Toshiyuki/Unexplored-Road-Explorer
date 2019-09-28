package com.app.fragment;

import androidx.fragment.app.Fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class OSMFragment extends Fragment {

	private static final double MAP_LAT = 34.2338;
	private static final double MAP_LON = 133.6354;
	private static final double MAP_ZOOM = 15.0;

	private MapView mMapView;
	private MyLocationNewOverlay myLocationOverlay;

	private Marker mMarker;
	private Button mButton;
	private Button mButtonSearch;
	private EditText mEdit;

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
		mButtonSearch = v.findViewById(R.id.button_search);
		mEdit = v.findViewById(R.id.edit_search);

		mMapView = v.findViewById(R.id.mapview);
		mMapView.setDestroyMode(false);

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
		myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(Objects.requireNonNull(getContext())), mMapView);
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
		GeoPoint mCenterPoint = new GeoPoint(MAP_LAT, MAP_LON);
		mMapController.setCenter(mCenterPoint);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 現在地に画面をスナップする
				IMapController mapController = mMapView.getController();
				mapController.setCenter(myLocationOverlay.getMyLocation());
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

		// シングルタップのイベントを設定する
		/*MapEventsReceiver mEventsReceiver = new MapEventsReceiver() {
			@Override
			public boolean singleTapConfirmedHelper(GeoPoint p) {
				*//*mMapView.getOverlays().remove(mMarker);
				mMarker.setPosition(p);
				mMapView.getOverlays().add(mMarker);
				mMapView.invalidate();
				array.add(p);
				return true;*//*
				return false;
			}

			@Override
			public boolean longPressHelper(GeoPoint p) {
				*//*for(GeoPoint geo:array){
					polyline.addPoint(geo);
				}
				mMapView.getOverlays().add(polyline);
				array.clear();*//*

				return false;
			}
		};
		MapEventsOverlay mEventsOverlay = new MapEventsOverlay(mEventsReceiver);
		mMapView.getOverlays().add(mEventsOverlay);*/

		mButtonSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchRoadVolley();
			}
		});
	}

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

	protected void searchRoadVolley(){
		// サーバーのアドレス
		GeoPoint geo = myLocationOverlay.getMyLocation();
		if(geo == null){
			myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(Objects.requireNonNull(getContext())), mMapView);
			myLocationOverlay.enableMyLocation();		// 現在地にマーカーを表示する
			mMapView.getOverlays().add(myLocationOverlay);
			return;
		}
		String zoom = String.valueOf(mEdit.getText());
		try {
			if(zoom == null){
				zoom = "1";
			}
			else if(Integer.parseInt(zoom) > 1000){
				zoom = "1000";
			}
		}catch (NumberFormatException e){
			zoom = "1";
		}
		String URL = "http://192.168.11.16:5001/search_road/" + zoom + "/" + geo.getLongitude() + "," + geo.getLatitude();

		// リクエストキュー
		RequestQueue getQueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
		// リクエスト
		JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.GET, URL,
				// 通信成功
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// 道の線を引く
						try {
							drawRoad(response.getJSONArray("result"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				},
				// 通信失敗
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("err", "error response volley", error);
					}
				});
		getQueue.add(mRequest);
	}

	private List<Polyline> polylines = new ArrayList<>();

	private void drawRoad(JSONArray response){
		for(Polyline poly:polylines){
			mMapView.getOverlays().remove(poly);
		}

		for(int i = 0; i < response.length(); i++){
			try {
				// 線の色を青に指定する
				polylines.add(i, new Polyline(mMapView));
				polylines.get(i).setColor(0xFF00BFFF);

				// JSONArrayから道情報を取得する
				JSONObject object = response.getJSONObject(i);
				String str = object.getString("way");
				String[] way = str.substring(11, str.length() - 1).split(Pattern.quote(","), 0);
				for(String point:way) {
					int spacerPos1 = point.indexOf(' ');

					GeoPoint gPt = new GeoPoint(Double.parseDouble(point.substring(spacerPos1 + 1)),
							Double.parseDouble(point.substring(0, spacerPos1)));
					// ポイントごとにジオポイントを作成する
					polylines.get(i).addPoint(gPt);
				}

				mMapView.getOverlays().add(polylines.get(i));
				Log.d("success", "draw road " + object.getString("name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mMapView.invalidate();
	}
}
