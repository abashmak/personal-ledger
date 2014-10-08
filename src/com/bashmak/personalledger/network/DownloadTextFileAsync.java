package com.bashmak.personalledger.network;

import java.io.ByteArrayOutputStream;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;

public class DownloadTextFileAsync extends AsyncTask<Void, Long, Boolean>
{
	private final static String TAG = "PL_DN_TEXT";
	private WrapperActivity mActivity;
    private String mPath;
    private String mResult;

    public DownloadTextFileAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = "";
    }

    @Override protected Boolean doInBackground(Void... params)
    {
        try
        {
            DropboxInputStream dis = Common.DropboxApi.getFileStream(mPath, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dis.copyStreamToOutput(baos, null);
            mResult = baos.toString("UTF-8");
            BeeLog.i1(TAG, mResult);
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
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
