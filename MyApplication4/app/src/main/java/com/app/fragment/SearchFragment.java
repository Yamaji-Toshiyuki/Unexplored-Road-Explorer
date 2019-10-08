package com.app.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {

	private static final double MAP_LAT = 34.2338;
	private static final double MAP_LON = 133.6354;
	private static final double MAP_ZOOM = 15.0;

	private MapView mMapView;
	private Marker mMarker;

	private ImageButton search;

	static SearchFragment newInstance(){
		SearchFragment fragment = new SearchFragment();
		Bundle bundle = new Bundle();
		bundle.putString("fragment", "search");
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
		View v = inflater.inflate(R.layout.fragment_search, null);
		mMapView = v.findViewById(R.id.map_view);
		mMapView.setDestroyMode(false);
		search = v.findViewById(R.id.search_button);

		mMarker = new Marker(mMapView);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle saveInstanceState){
		super.onActivityCreated(saveInstanceState);

		Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

		// ネットからタイルソースをとってこない
		mMapView.setUseDataConnection(false);

		// タイルソースを設定する
		mMapView.setTileSource(new XYTileSource("OSMPublicTransport", 15, 15, 256, ".jpg", new String[]{""}));
		// マップを更新する
		mMapView.invalidate();

		// 現在地を表示
		final MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(Objects.requireNonNull(getContext())), mMapView);
		mLocationOverlay.enableFollowLocation();
		mMapView.getOverlays().add(mLocationOverlay);

		// osmのcopyrightを表示
		CopyrightOverlay mCopyrightOverlay = new CopyrightOverlay(getContext());
		mMapView.getOverlays().add(mCopyrightOverlay);

		// ピンチでズーム
		mMapView.setMultiTouchControls(true);
		// 地図上の文字をスケーリングする
		mMapView.setTilesScaledToDpi(true);
		// ズームボタンを非表示にする
		mMapView.setBuiltInZoomControls(false);

		// 初期のカメラ位置を決める
		IMapController mMapController = mMapView.getController();
		mMapController.setZoom(MAP_ZOOM);
		GeoPoint centerPoint = new GeoPoint(MAP_LAT, MAP_LON);
		mMapController.setCenter(centerPoint);

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
		MapEventsReceiver mEventsReceiver = new MapEventsReceiver() {
			@Override
			public boolean singleTapConfirmedHelper(GeoPoint p) {
				return false;
			}

			@Override
			public boolean longPressHelper(GeoPoint p) {
				mMapView.getOverlays().remove(mMarker);
				mMarker.setPosition(p);
				mMapView.getOverlays().add(mMarker);
				mMapView.invalidate();

				return true;
			}
		};
		MapEventsOverlay mEventsOverlay = new MapEventsOverlay(mEventsReceiver);
		mMapView.getOverlays().add(mEventsOverlay);

		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 調べる
				searchVolley(mLocationOverlay.getMyLocation(), mMarker.getPosition());
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
	public void onResume(){
		super.onResume();

		if(mMapView != null){
			mMapView.onResume();
		}
	}

	/**
	 * 現在地から目的地までの経路を検索する
	 */
	private void searchVolley(GeoPoint myLocation, GeoPoint destLocation){
		// サーバーのアドレス
		String URL = "http://" + getURL() + "/search_" + myLocation + destLocation;
		// リクエストキュー
		RequestQueue getQueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
		// リクエスト
		JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.GET, URL,
				// 通信成功
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// 道の線を引く
						drawLine(response);
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

	private String getURL(){
		try {
			FileInputStream input = getContext().openFileInput("path");

			byte[] buffer = new byte[128];
			input.read(buffer);

			String str = new String(buffer);
			return str.trim();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "192.168.11.16:5001";
	}

	/**
	 * JSONObjectをパースしてマップに線を引く
	 */
	private void drawLine(JSONObject object){
		try {
			// 線の色を青に指定する
			Polyline polyline = new Polyline(mMapView);
			polyline.setColor(0xFF00BFFF);

			// JSONObjectから道情報を取得する
			String lines = object.getString("way");
			String[] point = lines.split(Pattern.quote(","), 0);
			for(String location:point){
				String lat = location.split(Pattern.quote(" "), 0)[0];
				String lon = location.split(Pattern.quote(" "), 0)[1];
				// ポイントごとにジオポイントを作成する
				GeoPoint gPt = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
				polyline.addPoint(gPt);
			}
			// マップに線を表示する
			mMapView.getOverlays().add(polyline);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
