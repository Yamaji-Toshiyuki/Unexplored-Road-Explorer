package com.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.app.fragment.Camera2Fragment;

public class ReadQRCodeCamera2Dialog extends AppCompatActivity implements Camera2Fragment.onOkClickedListener {

	private Camera2Fragment fragment;

	@Override
	protected void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setLayoutParams(params);
		frameLayout.setId(R.id.fragment_container);
		setContentView(frameLayout);

		// 撮影用フラグメントを呼ぶ
		fragment = Camera2Fragment.newInstance(Camera2Fragment.MODE_BARCODE);
		getSupportFragmentManager().beginTransaction()
				.replace(frameLayout.getId(), fragment)
				.commit();
	}

	@Override
	public void onPause() {
		super.onPause();
		// フラグメントの終了
//		getSupportFragmentManager().beginTransaction().remove(fragment).commit();
	}

	@Override
	public void onClicked() {
		String qrCode = "ok";
		Log.w("qrcode", qrCode);

		Intent intent = new Intent();
		intent.putExtra("qrcode", qrCode);
		setResult(RESULT_OK, intent);

		getSupportFragmentManager().beginTransaction().remove(fragment).commit();

		finish();
	}
}
