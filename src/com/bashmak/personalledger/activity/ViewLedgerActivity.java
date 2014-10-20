package com.bashmak.personalledger.activity;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.EntryListAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.DeleteEntryAsync;
import com.bashmak.personalledger.network.GetLedgerAsync;
import com.bashmak.personalledger.utility.Common;

public class ViewLedgerActivity extends WrapperActivity implements OnItemClickListener, OnItemLongClickListener
{
	private final String TAG = "PL-Ledger";
	private JSONObject mLedger;
	private int mLedgerPosition;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mLedgerPosition = getIntent().getExtras().getInt("position");
		mLedger = Common.Ledgers.get(mLedgerPosition);
		refreshUI();
	}

	@Override protected void onResume()
	{
		super.onResume();
		
		setViewsVisibility(false, true, false, false);
		new GetLedgerAsync(this, "/" + mLedger.optString("code") + "/catalog.json").execute();
    }

	@Override public void handleAsyncResult(ApiResult result)
	{
		refreshUI();
		if (!result.Error.isEmpty())
		{
			setViewsVisibility(false, false, true, true);
			BeeToast.showCenteredToastShort(this, result.Error);
		}
		else if (result.Response.equals("delete success"))
		{
			setViewsVisibility(false, true, false, false);
			new GetLedgerAsync(this, "/" + mLedger.optString("code") + "/catalog.json").execute();
		}
		else if (result.Response.isEmpty())
		{
			Common.Entries.clear();
			setViewsVisibility(false, false, true, true);
		}
		else
		{
			try
			{
				Common.Entries.clear();
				JSONArray jArr = new JSONArray(result.Response);
				int len = jArr.length();
				if (len > 0)
				{
					for (int i = 0; i < len; i++)
					{
						Common.Entries.add((JSONObject) jArr.get(i));
					}
					EntryListAdapter adapter = new EntryListAdapter(this, R.layout.list_item_1, Common.Entries);
					ListView lv = (ListView) findViewById(R.id.listEntries);
					lv.setAdapter(adapter);
					lv.setOnItemClickListener(this);
					lv.setOnItemLongClickListener(this);
					setViewsVisibility(true, false, false, true);
				}
				else
				{
					setViewsVisibility(false, false, true, true);
				}
			}
			catch (Exception e)
			{
				BeeLog.e1(TAG, "Exception parsing catalog.json", e);
				setViewsVisibility(false, false, true, true);
			}
		}
	}

	@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		showContextMenu(position);
		return true;
	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent i = new Intent(this, ViewEntryActivity.class);
		i.putExtra("ledger_position", mLedgerPosition);
		i.putExtra("entry_position", position);
		startActivity(i);
	}

	public void onDescriptionClicked(View view)
	{
		TextView tv = (TextView) view;
		if (tv.getLayout().getEllipsisCount(0) > 0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.txt_ledger_description);
			builder.setMessage(mLedger.optString("description"));
			builder.setNeutralButton("OK", null);
			builder.create().show();
		}
	}

	public void onNewEntryClicked(View view)
	{
		Intent i = new Intent(this, AddEntryActivity.class);
		i.putExtra("ledger_position", mLedgerPosition);
		startActivity(i);
	}
	
	public void onCreatorClicked(View view)
	{
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{mLedger.optString("email")});
		email.setType("html/text");
		startActivity(Intent.createChooser(email, "Send via..."));
	}

	private void setViewsVisibility(boolean list, boolean progress, boolean text, boolean button)
	{
		findViewById(R.id.listEntries).setVisibility(list ? View.VISIBLE : View.GONE);
		findViewById(R.id.progressEntries).setVisibility(progress ? View.VISIBLE : View.GONE);
		findViewById(R.id.txtNoEntries).setVisibility(text ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnNewEntry).setVisibility(button ? View.VISIBLE : View.GONE);
	}

	private void showContextMenu(final int position)
    {
		CharSequence[] options = {getString(R.string.txt_view), getString(R.string.txt_delete)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.txt_entry_actions);
		builder.setItems(options, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int option)
			{
				switch (option)
				{
				case 0:
					Intent i = new Intent(ViewLedgerActivity.this, ViewEntryActivity.class);
					i.putExtra("ledger_position", mLedgerPosition);
					i.putExtra("entry_position", position);
					startActivity(i);
    			   break;
				case 1:
					AlertDialog.Builder builder = new AlertDialog.Builder(ViewLedgerActivity.this);
					builder.setTitle(R.string.txt_confirm_delete);
					builder.setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							JSONObject entry = Common.Entries.get(position);
							Common.Entries.remove(position);
							setProgressView(getString(R.string.txt_wait_delete_entry));
					        new DeleteEntryAsync(ViewLedgerActivity.this, "/" + mLedger.optString("code") + "/").execute(mLedger, entry);
						}
					});
					builder.setNegativeButton(R.string.btn_cancel, null);
					builder.create().show();
					break;
				default:
					break;
				}
			}
		});
		builder.create().show();
    }

	private void refreshUI()
	{
		setContentView(R.layout.view_ledger);
		((TextView) findViewById(R.id.txtTitle)).setText(mLedger.optString("title"));
		((TextView) findViewById(R.id.txtDescription)).setText(mLedger.optString("description"));
		((TextView) findViewById(R.id.txtCreatedBy)).setText(mLedger.optString("creator"));
		((TextView) findViewById(R.id.txtCreatedOn)).setText(new Date(mLedger.optLong("create_date")).toString());
		((TextView) findViewById(R.id.txtModified)).setText(new Date(mLedger.optLong("modify_date")).toString());
	}
}
