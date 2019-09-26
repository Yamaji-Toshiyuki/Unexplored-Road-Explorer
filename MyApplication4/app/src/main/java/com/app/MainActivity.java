package com.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.app.ui.SectionsPagerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

	private final static String TILE_FILE_NAME = "shikoku.zip";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent intent = new Intent(this, LocationService.class);
		// 位置情報取得開始
		//startService(intent);

		// MapTileを用意する
		//setupTile();

		// カメラのボタン
		ImageButton camera = findViewById(R.id.button_camera);
		camera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// カメラ起動
				Intent intent = new Intent(getApplicationContext(), Camera2Activity.class);
				startActivity(intent);
			}
		});

		// 設定ボタン
		ImageButton option = findViewById(R.id.button_option);
		option.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 設定を開く
				Intent intent = new Intent(getApplicationContext(), OptionActivity.class);
				startActivity(intent);
			}
		});

		// SectionPagerAdapterのインスタンス生成
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.view_pager);
		// ViewPagerにアダプターをセットする
		viewPager.setAdapter(sectionsPagerAdapter);

		TabLayout tabs = findViewById(R.id.tabs);
		// tabとViewPagerを関連付ける
		tabs.setupWithViewPager(viewPager);
		// tabにアイコンをつける
		Objects.requireNonNull(tabs.getTabAt(0)).setIcon(R.drawable.maps);
		Objects.requireNonNull(tabs.getTabAt(1)).setIcon(R.drawable.search);
		Objects.requireNonNull(tabs.getTabAt(2)).setIcon(R.drawable.gallery);
	}

	private void setupTile(){
		// 外部ストレージにMapTileを保存する
		File main_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		File sub_dir = new File(main_dir, "osmdroid");
		if(!sub_dir.exists()){
			boolean p = sub_dir.mkdir();
			if(!p){
				Log.d("err", "Failed make dir");
			}
		}

		File file = new File(sub_dir, TILE_FILE_NAME);
		FileOutputStream fos = getFileOutput(file);
		InputStream is = getAssetInputStream();
		boolean ret = copyStream(is, fos);
		if(ret){
			Log.d("stream", "Successful");
		}
		else{
			Log.d("err", "Failed");
		}
	}

	private FileOutputStream getFileOutput(File file){
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(file, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return fos;
	}

	private InputStream getAssetInputStream(){
		InputStream is = null;
		try{
			is = getAssets().open(TILE_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return is;
	}

	private boolean copyStream(InputStream is, OutputStream os){
		byte[] buffer = new byte[1024];
		int n;
		boolean flag = true;

		try{
			while (-1 != (n = is.read(buffer))){
				os.write(buffer, 0, n);
			}

			os.close();
			is.close();
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		}

		return flag;
	}
}