package com.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.app.ImageDetailsDialog;
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

	private List<String> list = new ArrayList<>();
	private LinearLayout viewContainer;

//	private ScrollView scrollView;

	static GalleryFragment newInstance(){
		GalleryFragment fragment = new GalleryFragment();
		Bundle bundle = new Bundle();
		bundle.putString("fragment", "gallery");
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
		View v = inflater.inflate(R.layout.fragment_gallery, null);
		viewContainer = v.findViewById(R.id.container);
//		scrollView = v.findViewById(R.id.scroll_view);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle saveInstanceState) {
		super.onActivityCreated(saveInstanceState);

		addGalleryContents(viewContainer, Objects.requireNonNull(getContext()));
	}

	@Override
	public void onResume(){
		super.onResume();
	}

	@Override
	public void onPause(){
		list.clear();
		super.onPause();
	}

	private void addGalleryContents(final LinearLayout container, final Context context){
		list.clear();
		container.removeAllViews();

		String path = Objects.requireNonNull(context.getExternalFilesDir(null)).getPath();
		File[] files = new File(path).listFiles();
		for(File file:files){
			if(file.getName().startsWith("photo")){
				list.add(file.getName());
			}
		}

		CustomImageLayout layout = new CustomImageLayout(context);

		int i = 0;
		String tmp = "00/00";
		for(String filename:list) {
			if (i > 2) {
				container.addView(layout);
				layout = new CustomImageLayout(context);
				i = 0;
			}

			TextView date_text = new TextView(context);
			date_text.setTextColor(0xFF000000);
			date_text.setTextSize(getTextSize());
			date_text.setBackgroundResource(R.drawable.shadow);

			String date = filename.substring(11, 16);
			if (!tmp.equals(date)) {
				if(i != 0){
					container.addView(layout);
					layout = new CustomImageLayout(context);
					i = 0;
				}

				String str = date.substring(0, 2) + "/" + date.substring(3, 5);
				date_text.setText(String.format(" %s", str));
				container.addView(date_text, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				tmp = date;
			}

			File file = new File(path + "/" + filename);
			final Uri uri = Uri.fromFile(file);
			layout.setImageUri(i, uri);
			layout.setOnClickListener(i, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getContext(), ImageDetailsDialog.class);
					intent.putExtra("uri", uri.toString());
					startActivityForResult(intent, 3003);
				}
			});

			i++;
		}

		container.addView(layout);
//		containerScroll();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 3003){
			if(data != null){
				Uri uri = Uri.parse(data.getStringExtra("uri"));
				File file = new File(uri.getPath());
				file.delete();
				addGalleryContents(viewContainer, Objects.requireNonNull(getContext()));
			}
		}
	}

	private float getTextSize(){
		if(Build.VERSION.SDK_INT <= 24){
			return 30;
		}
		else {
			return 50;
		}
	}

	/*private void containerScroll(){
		scrollView.fullScroll(View.FOCUS_DOWN);
	}*/

	public void updateGallery(){
		if(viewContainer == null){
			return;
		}
		addGalleryContents(viewContainer, Objects.requireNonNull(getContext()));
	}
}
