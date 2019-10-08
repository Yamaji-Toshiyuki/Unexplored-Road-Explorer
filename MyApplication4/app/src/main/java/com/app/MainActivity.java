package com.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.fragment.GalleryFragment;
import com.app.util.VariableUtil;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.app.fragment.SectionsPagerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

	private final int REQUEST_CODE_CAMERA = 3000;
	private final int REQUEST_CODE_OPTION = 3001;

	private final static String TILE_FILE_NAME = "shikoku.zip";

	private SectionsPagerAdapter sectionsPagerAdapter;

	private VariableUtil variable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		variable = new VariableUtil();
		variable.setIsLocationService(true);
		variable.setIsForeground(true);
		variable.setIsApplicationStop(false);

		Intent intent = new Intent(this, LocationService.class);
		intent.putExtra(VariableUtil.SERIAL_NAME, variable);
		// 位置情報取得開始
		//startService(intent);

		// MapTileを用意する
		setupTile();

		float scale = getResources().getDisplayMetrics().density;
		int size;
		int mapIconId;
		int searchIconId;
		int galleryIconId;
		if(Build.VERSION.SDK_INT <= 24){
			size = (int)(50 * scale);
			mapIconId = R.layout.tab_map_icon_mdpi;
			searchIconId = R.layout.tab_search_icon_mdpi;
			galleryIconId = R.layout.tab_gallery_icon_mdpi;
		}
		else{
			size = (int)(70 * scale);
			mapIconId = R.layout.tab_map_icon_hdpi;
			searchIconId = R.layout.tab_search_icon_hdpi;
			galleryIconId = R.layout.tab_gallery_icon_hdpi;
		}

		// ボタンの設定
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
			params.addRule(RelativeLayout.ALIGN_PARENT_START);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			ImageButton cameraButton = findViewById(R.id.button_camera);
			cameraButton.setLayoutParams(params);
			cameraButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// カメラ起動
					Intent intent = new Intent(getApplicationContext(), Camera2Activity.class);
					intent.putExtra(VariableUtil.SERIAL_NAME, variable);
					startActivityForResult(intent, REQUEST_CODE_CAMERA);
				}
			});

			params = new RelativeLayout.LayoutParams(size, size);
			params.addRule(RelativeLayout.ALIGN_PARENT_END);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			ImageButton optionButton = findViewById(R.id.button_option);
			optionButton.setLayoutParams(params);
			optionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 設定を開く
					Intent intent = new Intent(getApplicationContext(), OptionActivity.class);
					intent.putExtra(VariableUtil.SERIAL_NAME, variable);
					startActivityForResult(intent, REQUEST_CODE_OPTION);
				}
			});

			params = new RelativeLayout.LayoutParams(size, size);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			ImageView logo = findViewById(R.id.logo);
			logo.setLayoutParams(params);
		}

		// SectionPagerAdapterのインスタンス生成
		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		ViewPager viewPager = findViewById(R.id.view_pager);
		// ViewPagerにアダプターをセットする
		viewPager.setAdapter(sectionsPagerAdapter);

		// tabとViewPagerを関連付ける
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);

		// tabにアイコンをつける
		Objects.requireNonNull(tabs.getTabAt(0)).setCustomView(mapIconId);
		Objects.requireNonNull(tabs.getTabAt(1)).setCustomView(searchIconId);
		Objects.requireNonNull(tabs.getTabAt(2)).setCustomView(galleryIconId);
	}

	/**
	 * startActivityForResultの結果を受け取る
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			// activityから帰ってきたときgalleryを開いていたら更新
			case REQUEST_CODE_CAMERA:
				// viewPager更新
				Fragment fragment = sectionsPagerAdapter.getCurrentFragment();
				Bundle bundle = fragment.getArguments();
				assert bundle != null;
				if("gallery".equals(bundle.getString("fragment"))){
					GalleryFragment galleryFragment = (GalleryFragment) fragment;
					galleryFragment.updateGallery();
				}
				break;
			case REQUEST_CODE_OPTION:
				variable.setIsForeground(data.getBooleanExtra("isForeground", variable.getIsForeground()));
				variable.setIsLocationService(data.getBooleanExtra("isForeground", variable.getIsLocationService()));
				Intent service = new Intent(this, LocationService.class);
				// 終了ボタンを押していたら終了する
				if(data.getBooleanExtra("isApplicationStop", variable.getIsApplicationStop())){
					Intent intent = new Intent();
					intent.putExtra("finish", true);
					setResult(RESULT_OK, intent);
					if(!variable.getIsForeground() || variable.getIsLocationService()){
						stopService(service);
					}
					finish();
					break;
				}
				// 位置情報の取得停止ボタンを押していたら停止
				if(!data.getBooleanExtra("isLocationServer", variable.getIsLocationService())){
					stopService(service);
					break;
				}
				// バックグラウンド動作させるかどうか
				service.putExtra(VariableUtil.SERIAL_NAME, variable);
				// 位置情報取得開始
				startService(service);
				break;
		}
	}

	/**
	 * マップタイルをセットアップする
	 */
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
		try {
			new FileInputStream(file);
		} catch (FileNotFoundException e) {
			try{
				FileOutputStream fos = new FileOutputStream(file);
				InputStream is = getAssetInputStream();
				boolean ret = copyStream(is, fos);
				if(ret){
					Log.d("stream", "Successful");
				}
				else{
					Log.d("err", "Failed");
				}
				is.close();
			} catch (IOException err) {
				err.printStackTrace();
			}
		}
	}

	/**
	 * assetにおいてあるファイルのinputStreamを取得する
	 */
	private InputStream getAssetInputStream(){
		InputStream is = null;
		try{
			is = getAssets().open(TILE_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return is;
	}

	/**
	 * コピー用のoutputStreamを作成する
	 */
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