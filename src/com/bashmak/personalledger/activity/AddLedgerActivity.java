package com.bashmak.personalledger.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.UploadTextFileAsync;
import com.bashmak.personalledger.utility.Common;

public class AddLedgerActivity extends WrapperActivity
{
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_add_ledger);
	}
	
	@Override public void handleAsyncResult(String result)
	{
		if (result.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "Success!");
			finish();
		}
		else
		{
			BeeToast.showCenteredToastShort(this, "Unable to create ledger: " + result);
			setContentView(R.layout.view_add_ledger);
		}
	}
	
	public void onCancelClicked(View view)
	{
		finish();
	}
	
	public void onSubmitClicked(View view)
	{
		EditText et = (EditText) findViewById(R.id.editTitle);
		String title = et.getText().toString().trim();
		if (title.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "You must enter a ledger title");
			return;
		}

		et = (EditText) findViewById(R.id.editDescription);
		String description = et.getText().toString().trim();
		if (description.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "You must enter a ledger description");
			return;
		}
		description = description.replaceAll("\n", " ");
		
		Common.addLedger(title.replaceAll(" ", ""), title, description);

		hideKeyboard(view);
		setProgressView(getString(R.string.txt_wait_create));
        new UploadTextFileAsync(this, mApi, "/ledgers.json").execute();
	}
}
