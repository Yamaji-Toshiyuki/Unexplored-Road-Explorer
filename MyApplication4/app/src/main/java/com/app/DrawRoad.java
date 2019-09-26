package com.app;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.MapView;

public class DrawRoad {

	private final String url = "http://tom/tom.php";

	private MapView mMapView;

	private Context mContext;

	public DrawRoad(Context context, MapView mapView){
		mContext = context;
		mMapView = mapView;
	}

	public void requestRoad(){
		RequestQueue getQueue = Volley.newRequestQueue(mContext);

		JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.GET, url,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
					}
				},

				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d("err", "error request");
					}
				}
		);

		getQueue.add(mRequest);
	}

	private void drawRoad(JSONObject response){
	}

	private JSONArray arrayPerth(JSONObject response){
		StringBuilder sb = new StringBuilder();
		try {
			JSONArray elements = response.getJSONArray("elements");

			for(int i = 0; i < elements.length(); i++){
				JSONObject data = elements.getJSONObject(i);
				if("way".equals(data.getString("type"))){
					sb.append(data);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
}
