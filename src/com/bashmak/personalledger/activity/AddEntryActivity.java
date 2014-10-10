package com.bashmak.personalledger.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.UpdateLegersAsync;

public class AddEntryActivity extends WrapperActivity
{
	private String mDescription = "";
	private String mAmount = "";
	private Long mDocDate;
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_add_entry);
	}

	@Override public void onResume()
	{
		super.onResume();
		((EditText) findViewById(R.id.editDescription)).setText(mDescription);
	}
	
	@Override public void handleAsyncResult(ApiResult result)
	{
		if (!result.Error.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "Unable to create new entry: " + result);
			setContentView(R.layout.view_add_entry);
		}
		else
		{
			BeeToast.showCenteredToastShort(this, result.Response);
			finish();
		}
	}
	
	public void onCancelClicked(View view)
	{
		finish();
	}
	
	public void onSubmitClicked(View view)
	{
		EditText et = (EditText) findViewById(R.id.editDescription);
		mDescription = et.getText().toString().trim();
		if (mDescription.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "You must enter a ledger description");
			return;
		}
		String description = mDescription.replaceAll("\n", " ");
		
		//Common.addLedger(mTitle, description);

		hideKeyboard(view);
		setProgressView(getString(R.string.txt_wait_create));
        new UpdateLegersAsync(this, "/").execute();
	}
}
