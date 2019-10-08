package com.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.R;
import com.squareup.picasso.Picasso;

public class CustomImageLayout extends LinearLayout {

	ImageView imageView1;
	ImageView imageView2;
	ImageView imageView3;

	public CustomImageLayout(Context context) {
		this(context, null);
	}

	public CustomImageLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.custom_image_view, this, true);

		imageView1 = findViewById(R.id.custom_image1);
		imageView2 = findViewById(R.id.custom_image2);
		imageView3 = findViewById(R.id.custom_image3);
	}

	public void setImageUri(int index, Uri uri){
		switch(index){
			case 0:
				Picasso.get().load(uri).into(imageView1);
				break;
			case 1:
				Picasso.get().load(uri).into(imageView2);
				break;
			case 2:
				Picasso.get().load(uri).into(imageView3);
				break;
		}
	}

	public void setOnClickListener(int index, OnClickListener listener){
		switch (index){
			case 0:
				imageView1.setOnClickListener(listener);
				break;
			case 1:
				imageView2.setOnClickListener(listener);
				break;
			case 2:
				imageView3.setOnClickListener(listener);
				break;
		}
	}
}
