package com.bashmak.personalledger.activity;

import java.util.Date;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.ImageGridAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.GetThumbsAsync;
import com.bashmak.personalledger.utility.Common;

public class ViewEntryActivity extends WrapperActivity
{
	private final String TAG = "PL-Entry";
	private JSONObject mLedger;
	private JSONObject mEntry;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_entry);
		mLedger = Common.Ledgers.get(getIntent().getExtras().getInt("ledger_position"));
		mEntry = Common.Entries.get(getIntent().getExtras().getInt("entry_position"));
		((TextView) findViewById(R.id.txtNumber)).setText(mEntry.optString("number"));
		((TextView) findViewById(R.id.txtDescription)).setText(mEntry.optString("description"));
		((TextView) findViewById(R.id.txtAmount)).setText(mEntry.optString("amount"));
		((TextView) findViewById(R.id.txtDocDate)).setText(mEntry.optString("doc_date"));
		((TextView) findViewById(R.id.txtCreatedOn)).setText(new Date(mEntry.optLong("create_date")).toString());
		((TextView) findViewById(R.id.txtModified)).setText(new Date(mEntry.optLong("modify_date")).toString());
	}

	@Override protected void onResume()
	{
		super.onResume();
		
		setViewsVisibility(false, true, false, false);
		new GetThumbsAsync(this, "/" + mLedger.optString("code")).execute(mEntry.optString("number"));
    }

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

	@Override public void handleAsyncResult(ApiResult result)
	{
		if (!result.Error.isEmpty())
		{
			setViewsVisibility(false, false, true, true);
			BeeLog.i1(TAG, "Error retrieving thumbs: " + result.Error);
		}
		else if (result.Thumbnails.isEmpty())
		{
			setViewsVisibility(false, false, true, true);
		}
		else
		{
			ImageGridAdapter adapter = new ImageGridAdapter(this, R.layout.grid_item_1, result.Thumbnails);
			GridView gv = (GridView) findViewById(R.id.gridThumbs);
			gv.setAdapter(adapter);
			//lv.setOnItemClickListener(this);
			//lv.setOnItemLongClickListener(this);
			setViewsVisibility(true, false, false, true);
		}
	}

	public void onDescriptionClicked(View view)
	{
		TextView tv = (TextView) view;
		if (tv.getLayout().getEllipsisCount(0) > 0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.txt_ledger_description);
			builder.setMessage(mEntry.optString("description"));
			builder.setNeutralButton("OK", null);
			builder.create().show();
		}
	}

	private void setViewsVisibility(boolean grid, boolean progress, boolean text, boolean button)
	{
		findViewById(R.id.gridThumbs).setVisibility(grid ? View.VISIBLE : View.GONE);
		findViewById(R.id.progressThumbs).setVisibility(progress ? View.VISIBLE : View.GONE);
		findViewById(R.id.txtNoThumbs).setVisibility(text ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnNewImage).setVisibility(button ? View.VISIBLE : View.GONE);
	}
}
