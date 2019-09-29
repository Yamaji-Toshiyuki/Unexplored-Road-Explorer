package com.app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.app.R;
import com.app.ui.CustomImageLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GalleryFragment extends Fragment {

	private LinearLayout viewContainer;

	private List<String> list = new ArrayList<>();

	static GalleryFragment newInstance(){
		GalleryFragment fragment = new GalleryFragment();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
		View v = inflater.inflate(R.layout.fragment_gallery, null);
		viewContainer = v.findViewById(R.id.container);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle saveInstanceState) {
		super.onActivityCreated(saveInstanceState);
		addGalleryContents(viewContainer, Objects.requireNonNull(getContext()));
		/*button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewContainer.removeAllViews();
				viewContainer.addView(view);
			}
		});*/
	}

	@Override
	public void onPause(){
		list.clear();
		super.onPause();
	}

	private void addGalleryContents(LinearLayout container, Context context){

		String path = Objects.requireNonNull(context.getExternalFilesDir(null)).getPath();
		File[] files = new File(path).listFiles();
		for(File file:files){
			if(file.getName().startsWith("photo")){
				list.add(file.getName());
			}
		}

		TextView date_text = new TextView(context);
		date_text.setTextColor(0xFF000000);
		date_text.setTextSize(60f);
		date_text.setBackgroundResource(R.drawable.shadow);

		CustomImageLayout layout = new CustomImageLayout(context);

		int i = 0;
		String tmp = "00/00";
		for(String filename:list) {
			if (i > 2) {
				container.addView(layout);
				layout = new CustomImageLayout(context);
				i = 0;
			}
			String date = filename.substring(11, 16);
			if (!tmp.equals(date)) {
				String str = date.substring(0, 2) + "/" + date.substring(3, 5);
				date_text.setText(str);
				container.addView(date_text, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				tmp = date;
			}

			File file = new File(path + "/" + filename);
			Uri uri = Uri.fromFile(file);
			layout.setImageUri(i, uri);
			layout.setOnClickListener(i, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO 画像の詳細
				}
			});

			i++;
		}

		container.addView(layout);
	}
}
