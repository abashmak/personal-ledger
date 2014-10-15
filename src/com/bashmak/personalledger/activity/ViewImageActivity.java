package com.bashmak.personalledger.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.GetImageAsync;

public class ViewImageActivity extends WrapperActivity
{
	private final String TAG = "PL-Image";
	private String code;
	private String name;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_image);
		findViewById(R.id.progress1).setVisibility(View.VISIBLE);
		findViewById(R.id.img1).setVisibility(View.GONE);
		code = getIntent().getExtras().getString("code");
		name = getIntent().getExtras().getString("name");
		new GetImageAsync(this, "/" + code).execute(name);
	}

	@Override public void handleAsyncResult(ApiResult result)
	{
		findViewById(R.id.progress1).setVisibility(View.GONE);
		if (!result.Error.isEmpty() || result.Thumbnails.isEmpty())
		{
			BeeLog.i1(TAG, "Error retrieving image: " + result.Error);
			BeeToast.showCenteredToastShort(this, "Unable to retrieve full-size image");
			finish();
		}
		else
		{
			findViewById(R.id.img1).setVisibility(View.VISIBLE);
			ImageView img = (ImageView) findViewById(R.id.img1);
			img.setImageBitmap(result.Thumbnails.get(0));
		}
	}
}
