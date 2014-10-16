package com.bashmak.personalledger.activity;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.ImageGridAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.DeleteImageAsync;
import com.bashmak.personalledger.network.GetThumbsAsync;
import com.bashmak.personalledger.utility.Common;

public class ViewEntryActivity extends WrapperActivity implements OnItemClickListener, OnItemLongClickListener
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

	@Override public void handleAsyncResult(ApiResult result)
	{
		if (!result.Error.isEmpty())
		{
			setViewsVisibility(false, false, true, true);
			BeeLog.i1(TAG, "Error retrieving thumbs: " + result.Error);
		}
		else if (result.Response.equals("delete success"))
		{
			setViewsVisibility(false, true, false, false);
			new GetThumbsAsync(this, "/" + mLedger.optString("code")).execute(mEntry.optString("number"));
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
			gv.setOnItemClickListener(this);
			gv.setOnItemLongClickListener(this);
			setViewsVisibility(true, false, false, true);
		}
	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		startImageActivity(position);
	}

	@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		showContextMenu(position);
		return true;
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

	public void onNewImageClicked(View view)
	{
		BeeToast.showCenteredToastShort(this, "Image addition not implemented yet");
	}
	
	private void setViewsVisibility(boolean grid, boolean progress, boolean text, boolean button)
	{
		findViewById(R.id.gridThumbs).setVisibility(grid ? View.VISIBLE : View.GONE);
		findViewById(R.id.progressThumbs).setVisibility(progress ? View.VISIBLE : View.GONE);
		findViewById(R.id.txtNoThumbs).setVisibility(text ? View.VISIBLE : View.GONE);
		//findViewById(R.id.btnNewImage).setVisibility(button ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnNewImage).setVisibility(View.GONE);
	}

	private void showContextMenu(final int position)
    {
		CharSequence[] options = {getString(R.string.txt_view), getString(R.string.txt_delete)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.txt_image_actions);
		builder.setItems(options, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int option)
			{
				switch (option)
				{
				case 0:
					startImageActivity(position);
    			   break;
				case 1:
					AlertDialog.Builder builder = new AlertDialog.Builder(ViewEntryActivity.this);
					builder.setTitle(R.string.txt_confirm_delete);
					builder.setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							for (int i = 0; i < Common.Entries.size(); i++)
							{
								String imgName = "";
								if (Common.Entries.get(i).optString("number").equals(mEntry.optString("number")))
								{
									JSONArray images = mEntry.optJSONArray("images");
									if (images != null)
									{
										JSONArray newImages = new JSONArray();
										for (int j = 0; j < images.length(); j++)
										{
											if (j != position)
											{
												newImages.put(images.optString(j));
											}
											else
											{
												imgName = images.optString(j);
											}
										}
										try
										{
											mEntry.putOpt("images", newImages);
											Common.Entries.set(i, mEntry);
										}
										catch (JSONException e)
										{
											e.printStackTrace();
										}
									}
									setViewsVisibility(false, true, false, false);
									new DeleteImageAsync(ViewEntryActivity.this, "/" + mLedger.optString("code") + "/", imgName).execute(mEntry);
									break;
								}
							}
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

	private void startImageActivity(int position)
	{
		Intent i = new Intent(this, ViewImageActivity.class);
		i.putExtra("code", mLedger.optString("code"));
		JSONArray images = mEntry.optJSONArray("images");
		if (images != null)
		{
			i.putExtra("name", images.optString(position));
		}
		startActivity(i);
	}
}
