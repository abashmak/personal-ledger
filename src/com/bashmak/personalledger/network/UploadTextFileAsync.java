package com.bashmak.personalledger.network;

import java.io.StringBufferInputStream;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;

@SuppressWarnings("deprecation")
public class UploadTextFileAsync extends AsyncTask<Void, Long, Boolean>
{
	private WrapperActivity mActivity;
    private DropboxAPI<?> mApi;
    private String mPath;
    private String mResult;

    public UploadTextFileAsync(WrapperActivity activity, DropboxAPI<?> api, String dropboxPath)
    {
    	mActivity = activity;
        mApi = api;
        mPath = dropboxPath;
        mResult = "";
    }

    @Override protected Boolean doInBackground(Void... params)
    {
        try
        {
        	StringBuilder sb = new StringBuilder();
        	for (JSONObject ledger : Common.Ledgers)
        	{
        		sb.append(ledger.toString());
        	}
        	StringBufferInputStream sbis = new StringBufferInputStream(sb.toString());
        	mApi.putFileOverwrite(mPath, sbis, sb.length(), new ProgressListener()
        	{
				@Override public void onProgress(long arg0, long arg1)
				{
				}
			});
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1("Dropbox api exception: " + e.toString());
        	mResult = "unknown error";
            return false;
        }
    }

    @Override protected void onPostExecute(Boolean result)
    {
    	if (mActivity!= null)
    	{
    		mActivity.handleAsyncResult(mResult);
    	}
    }
}