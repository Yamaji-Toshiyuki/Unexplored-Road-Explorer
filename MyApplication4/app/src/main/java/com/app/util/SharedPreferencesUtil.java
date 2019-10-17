package com.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

	public enum Key{
		LATITUDE,
		LONGITUDE,
		IS_DEMO,
		IS_DEBUG,
		SERVER_IP,
		USER_NAME,
		USER_ID
	}

	public static final double MAP_LAT = 34.2338;
	public static final double MAP_LON = 133.6354;

	private final double EXPONENT = Math.pow(10, 8);

	private SharedPreferences data;

	public SharedPreferencesUtil(Context context){
		data = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
	}

	//////////////
	// セッター
	//////////////

	public void setLatLon(Key key, double lat){
		data.edit()
				.putLong(key.name(), (long)(lat * EXPONENT))
				.apply();
	}

	public void setIsDemo(boolean bol){
		data.edit()
				.putBoolean(Key.IS_DEMO.name(), bol)
				.apply();
	}

	public void setIsDebug(boolean bol){
		data.edit()
				.putBoolean(Key.IS_DEBUG.name(), bol)
				.apply();
	}

	public void setServerIP(String str){
		data.edit()
				.putString(Key.SERVER_IP.name(), str)
				.apply();
	}

	public void setUserName(String str){
		data.edit()
				.putString(Key.USER_NAME.name(), str)
				.apply();
	}

	public void setUserId(String str){
		data.edit()
				.putString(Key.USER_ID.name(), str)
				.apply();
	}

	//////////////
	// ゲッター
	//////////////

	public double getLatLon(Key key, double defaultValue){
		return (double) data.getLong(key.name(), (long)(defaultValue * EXPONENT)) / EXPONENT;
	}

	public boolean getIsDemo(){
		return data.getBoolean(Key.IS_DEMO.name(), false);
	}

	public boolean getIsDebug(){
		return data.getBoolean(Key.IS_DEBUG.name(), false);
	}

	public String getServerIP(){
		return data.getString(Key.SERVER_IP.name(), "192.168.11.16:5001");
	}

	public String getUserName(){
		return data.getString(Key.USER_NAME.name(), null);
	}

	public String getUserId(){
		return data.getString(Key.USER_ID.name(), null);
	}
}
