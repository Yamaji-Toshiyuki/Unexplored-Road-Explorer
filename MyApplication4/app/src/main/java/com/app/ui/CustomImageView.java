package com.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView extends AppCompatImageView {

	public CustomImageView(Context context) {
		super(context, null);
	}

	public CustomImageView(Context context, AttributeSet attrs){
		super(context, attrs, 0);
	}

	public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// 横幅に縦幅を合わせる
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension(width, height);
	}
}
