package com.bashmak.personalledger.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.UpdateLedgersAsync;
import com.bashmak.personalledger.utility.Common;

public class AddLedgerActivity extends WrapperActivity
{
	private String mTitle = "";
	private String mDescription = "";
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_add_ledger);
	}

	@Override public void onResume()
	{
		super.onResume();
		((EditText) findViewById(R.id.editTitle)).setText(mTitle);
		((EditText) findViewById(R.id.editDescription)).setText(mDescription);
	}
	
	@Override public void handleAsyncResult(ApiResult result)
	{
		if (!result.Error.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "Unable to create ledger: " + result);
			setContentView(R.layout.view_add_ledger);
		}
		else
		{
			//BeeToast.showCenteredToastShort(this, result.Response);
			finish();
		}
	}
	
	public void onCancelClicked(View view)
	{
		finish();
	}
	
	public void onSubmitClicked(View view)
	{
		EditText et = (EditText) findViewById(R.id.editTitle);
		mTitle = et.getText().toString().trim();
		if (mTitle.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "You must enter a ledger title");
			return;
		}

		et = (EditText) findViewById(R.id.editDescription);
		mDescription = et.getText().toString().trim();
		if (mDescription.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "You must enter a ledger description");
			return;
		}
		String description = mDescription.replaceAll("\n", " ");
		
		Common.addLedger(mTitle, description);

		hideKeyboard(view);
		setProgressView(getString(R.string.txt_wait_create_ledger));
        new UpdateLedgersAsync(this, "/").execute();
	}
}
