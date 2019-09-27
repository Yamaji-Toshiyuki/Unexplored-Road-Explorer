package com.app.ui;

import android.content.Context;

import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView extends AppCompatImageView {

	public CustomImageView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// 横幅に縦幅を合わせる
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}
}
