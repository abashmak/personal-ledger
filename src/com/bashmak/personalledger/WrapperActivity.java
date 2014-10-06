package com.bashmak.personalledger;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class WrapperActivity extends Activity
{
	protected DatabaseHelper mHelper = new DatabaseHelper(this);
	
	protected void hideKeyboard(View view)
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	protected void setProgressView(String message)
	{
		setContentView(R.layout.view_progress);
		TextView msg = (TextView) findViewById(R.id.message);
		msg.setText(message);
	}
}
