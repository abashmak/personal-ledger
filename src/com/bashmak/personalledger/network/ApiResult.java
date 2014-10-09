package com.bashmak.personalledger.network;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class ApiResult
{
	public String Error;
	public String Response;
	public ArrayList<Bitmap> Thumbnails;
	
	public ApiResult()
	{
		Error = "";
		Response = "";
		Thumbnails = new ArrayList<Bitmap>();
	}
	
	public ApiResult(String response)
	{
		Error = "";
		Response = response;
		Thumbnails = new ArrayList<Bitmap>();
	}
}
