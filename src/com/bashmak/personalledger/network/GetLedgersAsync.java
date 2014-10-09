package com.bashmak.personalledger.network;

import java.io.ByteArrayOutputStream;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.exception.DropboxServerException;

public class GetLedgersAsync extends AsyncTask<Void, Long, Boolean>
{
	private final static String TAG = "PL-GetLedgers";
	private WrapperActivity mActivity;
    private String mPath;
    private ApiResult mResult;

    public GetLedgersAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = new ApiResult();
    }

    @Override protected Boolean doInBackground(Void... params)
    {
        try
        {
            DropboxInputStream dis = Common.DropboxApi.getFileStream(mPath, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dis.copyStreamToOutput(baos, null);
            mResult.Response = baos.toString("UTF-8");
            baos.close();
            dis.close();
            return true;
        }
        catch (DropboxServerException e)
        {
        	if (e.error != DropboxServerException._404_NOT_FOUND)
        	{
        		mResult.Error = "Dropbox error: " + e.reason;
        	}
        	return false;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
        	mResult.Error = "unknown error";
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
