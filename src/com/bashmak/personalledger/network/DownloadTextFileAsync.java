package com.bashmak.personalledger.network;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;

public class DownloadTextFileAsync extends AsyncTask<Void, Long, Boolean>
{
	private WrapperActivity mActivity;
    private DropboxAPI<?> mApi;
    private String mPath;
    private String mResult;

    public DownloadTextFileAsync(WrapperActivity activity, DropboxAPI<?> api, String dropboxPath)
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
            DropboxInputStream dis = mApi.getFileStream(mPath, null);
			StringBuilder sb = new StringBuilder();
			byte[] buffer = new byte[8192];
			while (dis.read(buffer) != -1)
			{
				sb.append(new String(buffer));
			}
			mResult = sb.toString();

            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1("Dropbox api exception: " + e.toString());
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
