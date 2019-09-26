package com.app.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.app.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GalleryFragment extends Fragment {

	private LinearLayout viewContainer;

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
		//addGalleryContents(viewContainer, getContext());
	}

	private static final String FILENAME_DATE_FORMAT = "MM:dd";

	private List<String> list = new ArrayList<>();

	private void addGalleryContents(LinearLayout container, Context context){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(FILENAME_DATE_FORMAT, Locale.US);
		String dateTime = sdf.format(date);

		TextView date_text = new TextView(context);
		date_text.setText(dateTime);
		date_text.setTextColor(0xFF000000);
		date_text.setTextSize(20f);

		GridLayout gridLayout = new GridLayout(context);

		String path = Objects.requireNonNull(context.getExternalFilesDir(null)).getPath();
		File[] files = new File(path).listFiles();
		for(File file:files){
			if(file.getName().startsWith("photo")){
				list.add(file.getName());
			}
		}

		int i = 0;
		int j = 0;
		for(String filename:list) {
			if (i > 3) {
				i = 0;
				j++;
			}
			// GridLayout用のパラメータ
			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.columnSpec = GridLayout.spec(i, GridLayout.FILL, 1);
			params.rowSpec = GridLayout.spec(j, GridLayout.FILL, 1);

			// itemにパラメータをつける
			ImageView image = new ImageView(context);
			image.setLayoutParams(params);

			File input = new File(path + "/" + filename);
			Uri uri = Uri.fromFile(input);
			image.setImageURI(uri);

			gridLayout.addView(image);

			i++;
		}

		container.addView(gridLayout);
	}
}
