package com.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

public class SharedPreferencesUtil {

	public enum Key{
		LATITUDE,
		LONGITUDE,
		IS_DEMO,
		IS_DEBUG,
		SERVER_IP,
		USER_NAME,
		USER_ID,
		CURRENT
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

	public void setLatLon(double lat, double lon){
		data.edit()
				.putLong(Key.LATITUDE.name(), (long)(lat * EXPONENT))
				.putLong(Key.LONGITUDE.name(), (long)(lon * EXPONENT))
				.commit();
	}

	public void setIsDemo(boolean bol){
		data.edit()
				.putBoolean(Key.IS_DEMO.name(), bol)
				.commit();
	}

	public void setIsDebug(boolean bol){
		data.edit()
				.putBoolean(Key.IS_DEBUG.name(), bol)
				.commit();
	}

	public void setServerIP(String str){
		data.edit()
				.putString(Key.SERVER_IP.name(), str)
				.commit();
	}

	public void setUserName(String str){
		data.edit()
				.putString(Key.USER_NAME.name(), str)
				.commit();
	}

	public void setUserId(String str){
		data.edit()
				.putString(Key.USER_ID.name(), str)
				.commit();
	}

	public void setCurrentNumber(String str){
		data.edit()
				.putString(Key.CURRENT.name(), str)
				.commit();
	}

	//////////////
	// ゲッター
	//////////////

	public Location getLatLon(){
		Location location = new Location("?");
		location.setLatitude((double) data.getLong(Key.LATITUDE.name(), (long)(MAP_LAT * EXPONENT)) / EXPONENT);
		location.setLongitude((double) data.getLong(Key.LONGITUDE.name(), (long)(MAP_LON * EXPONENT)) / EXPONENT);

		return location;
	}

	public boolean getIsDemo(){
		return data.getBoolean(Key.IS_DEMO.name(), false);
	}
	public String getDemo(){
		return data.getBoolean(Key.IS_DEMO.name(), false) ? "/demo" : "";
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

	public String getCurrentNumber(){
		return data.getString(Key.CURRENT.name(), null);
	}
}
