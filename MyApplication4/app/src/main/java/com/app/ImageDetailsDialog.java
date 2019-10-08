package com.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageDetailsDialog extends AppCompatActivity {

	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

		setContentView(R.layout.dialog_image_details);

		String str = getIntent().getStringExtra("uri");
		Uri uri = Uri.parse(str);

		// 画像詳細
		image = findViewById(R.id.image_details);
		Picasso.get().load(uri).into(image);

		ImageButton upServer = findViewById(R.id.up_server);
		upServer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// up server
			}
		});

		ImageButton trash = findViewById(R.id.trash);
		trash.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// trash
			}
		});
	}

	@Override
	public void onDestroy(){
		if(image != null){
			image.setImageDrawable(null);
		}
		super.onDestroy();
	}
}
