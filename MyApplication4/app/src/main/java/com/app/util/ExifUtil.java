package com.app.util;

import android.location.Location;
import android.os.Build;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExifUtil {

	private static final String EXIF_DATE_FORMAT = "yyyy:MM:dd HH:mm:ss";
	private static final String FILENAME_DATE_FORMAT = "yyyy:MM:dd_HH:mm:ss";

	private static final String EXIF_PREFIX = "photo_";
	private static final String EXIF_SUFFIX = ".jpg";

	/**
	 * ファイル名を決める
	 * photo_ + 日付 + .jpgで名前を付ける
	 */
	public static String getFilename(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat(FILENAME_DATE_FORMAT, Locale.US);
		String dateTime = sdf.format(date);
		return EXIF_PREFIX + dateTime + EXIF_SUFFIX;
	}

	/**
	 * exifファイルに位置情報と日付をつける
	 */
	public static void addExif(Date date, Location location, File file){
		SimpleDateFormat sdf = new SimpleDateFormat(EXIF_DATE_FORMAT, Locale.US);
		String dateTime = sdf.format(date);

		ExifInterface exif = null;
		try {
			exif = new ExifInterface(file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert exif != null;
		exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
		exif.setAttribute(ExifInterface.TAG_DATETIME, dateTime);

		exif = setGpsAttributes(exif, location);
		try {
			exif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 位置情報と日付をつける
	 */
	private static ExifInterface setGpsAttributes(ExifInterface exif, Location location){
		if(location == null){
			return exif;
		}

		double lat = location.getLatitude();
		double lon = location.getLongitude();
		double alt = location.getAltitude();

		exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(lat));			// 緯度
		exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(lon));			// 経度
		exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef(lat));	// 方角(東or西)
		exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef(lon));	// 方角(北or南)
		/*exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, convertAltitude(alt));	// 高度
		exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "0");*/			// 基準高度(0は海面)

		return exif;
	}

	/**
	 * 座標を度から度分秒に変換する
	 */
	private static String convert(double latitude){
		// 座標を度から度分秒に変換する
		latitude = Math.abs(latitude);
		int degree = (int)latitude;
		latitude = (latitude - degree) * 60;
		int minute = (int)latitude;
		latitude = (latitude - minute) * 60;
		int second = (int)latitude * 1000;

		// 度/分母, 分/分母, 秒/分母の形にする
		StringBuilder sb = new StringBuilder(20);
		sb.setLength(0);
		sb.append(degree);
		sb.append("/1,");
		sb.append(minute);
		sb.append("/1,");
		sb.append(second);
		sb.append("/1000,");

		return sb.toString();
	}

	/**
	 * 緯度から東か西を返す
	 */
	private static String latitudeRef(double latitude){
		if(latitude > 0){
			// east
			return "E";
		}
		else{
			// west
			return "W";
		}
	}

	/**
	 * 経度から北か南を返す
	 */
	private static String longitudeRef(double longitude){
		if(longitude > 0){
			// north
			return "N";
		}
		else{
			// south
			return "S";
		}
	}

	/**
	 * 高度のやつ
	 */
	private static String convertAltitude(double altitude){
		int alt = (int)altitude;

		StringBuilder sb = new StringBuilder(20);
		sb.setLength(0);
		sb.append(alt);
		sb.append("/1,");

		return sb.toString();
	}
}
