package com.bashmak.personalledger.utility;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.bashmak.beeutils.BeeLog;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class Common
{
    private final static String APP_KEY = "v9izdoem5z240yn";
    private final static String APP_SECRET = "pzj4274kxvlouml";
    private final static String DROPBOX_ACCOUNT_PREFS_NAME = "prefs";
    private final static String ACCESS_SECRET_NAME = "access_secret";
    
    public static String DropboxSecret; 
    public static ArrayList<JSONObject> Ledgers = new ArrayList<JSONObject>();
	public static String CreatorName = "Unknown";
	public static String CreatorEmail = "unknown";

	public static void init(Context context)
	{
        SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        DropboxSecret = prefs.getString(ACCESS_SECRET_NAME, "");
	}
	
    public static AndroidAuthSession buildSession(Context context)
    {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        if (!DropboxSecret.isEmpty())
        {
            session.setOAuth2AccessToken(DropboxSecret);
        }
        return session;
    }
    
	public static void clearKeys(Context context)
	{
        DropboxSecret = "";
        SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        prefs.edit().remove(ACCESS_SECRET_NAME).commit();
	}

    public static void storeAuth(Context context, AndroidAuthSession session)
    {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null)
        {
            SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
            prefs.edit().putString(ACCESS_SECRET_NAME, oauth2AccessToken).commit();
        	DropboxSecret = oauth2AccessToken;
        }
    }

    public static void addLedger(String code, String title, String description)
    {
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.putOpt("code", code);
			jObj.putOpt("title", title);
			jObj.putOpt("description", description);
			jObj.putOpt("creator", CreatorName);
			jObj.putOpt("email", CreatorEmail);
			jObj.putOpt("create_date", System.currentTimeMillis());
			jObj.putOpt("modify_date", System.currentTimeMillis());
			jObj.putOpt("template", 1);
			Ledgers.add(jObj);
		}
		catch (JSONException e)
		{
			BeeLog.e1("Exception adding new ledger", e);
		}
    }
}
