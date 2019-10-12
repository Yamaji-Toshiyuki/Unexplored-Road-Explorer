package com.app.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class VolleyToServerUtil {

	private SharedPreferencesUtil util;
	private Context mContext;

	public VolleyToServerUtil(Context context){
		mContext = context;
		util = new SharedPreferencesUtil(mContext);
	}

	/**
	 * 名前を登録する
	 * @param name 登録用の名前
	 */
	public void RegisterName(String name){
		final String mName = name;
		// サーバーのアドレス
		String GET_URL = "http://" + util.getServerIP() + "/register/" + name;
		// リクエストキュー
		RequestQueue getQueue = Volley.newRequestQueue(mContext);

		// Volleyによる通信開始
		// (GETかPOST、サーバーのURL、受信メソッド、エラーメソッド)
		JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.GET, GET_URL,
				// 通信成功
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// ユーザー名を保存する
						try {
							if("success".equals(response.getString("status"))){
								// ユーザー名とユーザーIDを登録する
								util.setUserName(mName);
								util.setUserId(response.getString("id"));
							}
							else{
								String err = response.getString("message");
								Log.e("err", "status failure : " + err);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				},
				// 通信失敗
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("err", "error response server", error);
					}
				});

		getQueue.add(mRequest);
	}
}
