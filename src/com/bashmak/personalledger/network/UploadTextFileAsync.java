package com.bashmak.personalledger.network;

import java.io.ByteArrayInputStream;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;

public class UploadTextFileAsync extends AsyncTask<Void, Long, Boolean>
{
	private final static String TAG = "PL_UP_TEXT";
	private WrapperActivity mActivity;
    private String mPath;
    private String mResult;

    public UploadTextFileAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = "";
    }

    @Override protected Boolean doInBackground(Void... params)
    {
        try
        {
        	StringBuilder sb = new StringBuilder("[ ");
        	for (JSONObject ledger : Common.Ledgers)
        	{
        		sb.append(ledger.toString()).append(",");
        	}
        	sb.replace(sb.length()-1, sb.length(), "]");
        	byte[] bytes = sb.toString().getBytes("UTF-8");
        	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        	Common.DropboxApi.putFileOverwrite(mPath, bais, bytes.length, null);
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
        	mResult = "unknown error";
        	if (Common.Ledgers.size() > 0)
        	{
        		Common.Ledgers.remove(Common.Ledgers.size()-1);
        	}
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
