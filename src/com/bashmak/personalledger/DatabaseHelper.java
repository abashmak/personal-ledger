package com.bashmak.personalledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.bashmak.beeutils.BeeLog;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = "PL_dbhelper";
	private static final String dbName="pl.db";
    private SQLiteDatabase mDB;

	public DatabaseHelper(Context context)
	{
		super(context, dbName, null, 1);
	}
	
	@Override public void onCreate(SQLiteDatabase db)
	{
		String code = "(Code TEXT UNIQUE NOT NULL, ";
		String name = "Name TEXT UNIQUE NOT NULL, ";
		String desc = "Description TEXT NOT NULL, ";
		String tmplt = "Template INTEGER NOT NULL, ";
		String creator = "Creator TEXT NOT NULL, ";
		String email = "CreatorEmail TEXT NOT NULL, ";
		String cdate = "CreateDate INTEGER NOT NULL, ";
		String mdate = "ModifyDate INTEGER NOT NULL)";
		mDB = db;
		mDB.execSQL("CREATE TABLE Ledgers " + code + name + desc + tmplt + creator + email + cdate + mdate);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Do not need to do anything here unless table columns are changing or new columns are added
	}
	
	@Override public void close()
	{
		if (mDB != null && mDB.isOpen())
		{
			mDB.close();
		}
		mDB = null;
		super.close();
	}

	private void getDB()
	{
		if (mDB == null)
		{
			mDB = getWritableDatabase();
		}
	}
	
	private ContentValues getCV(Ledger ledger)
	{
		ContentValues cv = new ContentValues();
		
		cv.put("Code", ledger.Code);
		cv.put("Name", ledger.Name);
		cv.put("Description", ledger.Description);
		cv.put("Creator", ledger.Creator);
		cv.put("Email", ledger.Email);
		cv.put("CreateDate", ledger.CreateDate);
		cv.put("ModifyDate", ledger.ModifyDate);
		cv.put("Template", ledger.getTemplate());
		
		return cv;
	}

	public void addLedger(Ledger ledger)
	{
		try
		{
			getDB();
			if (mDB.insert("Ledgers", null, getCV(ledger)) < 0)
			{
				BeeLog.e1(TAG, "Unable to insert new ledger: " + ledger.Name);
			}
		}
		catch (Exception e)
		{
			BeeLog.e1(TAG, "Exception inserting new ledger:", e);
		}		
		close();
	}

	public void updateLedger(Ledger ledger)
	{
		try
		{
			getDB();
			if (mDB.update("Ledgers", getCV(ledger), "Code=?", new String[] {ledger.Code}) < 0)
			{
				BeeLog.e1(TAG, "Unable to update ledger: " + ledger.Name);
			}
		}
		catch (Exception e)
		{
			BeeLog.e1(TAG, "Exception updating ledger:", e);
		}		
		close();
	}

	public void deleteLedger(String field, boolean code_not_name)
	{
		try
		{
			getDB();
			String selection = (code_not_name) ? "Code=?" : "Name=?";
			if (mDB.delete("Ledgers", selection, new String[] {field}) <= 0)
			{
				BeeLog.e1(TAG, "Unable to delete ledger: " + field);
			}
		}
		catch (Exception e)
		{
			BeeLog.e1(TAG, "Exception deleting ledger:", e);
		}
		close();
	}
	
	// Note: close needs to be called explicitly after calling this function and consuming the cursor
	public Cursor getLedgers(String orderBy)
	{
		Cursor cursor = null;
		try
		{
			cursor = query("Ledgers", null, null, null, orderBy);
		}
		catch (Exception e)
		{
			BeeLog.e1(TAG, "Exception retrieving ledger:", e);
		}
        return cursor;
	}

	// Note: close needs to be called explicitly after calling this function and consuming the cursor
	public Cursor getLedger(String field, boolean code_not_name)
	{
		Cursor cursor = null;
		try
		{
			String selection = (code_not_name) ? "Code=?" : "Name=?";
			cursor = query("Ledgers", selection, new String[] {field}, null, null);
		}
		catch (Exception e)
		{
			BeeLog.e1(TAG, "Exception retrieving ledger:", e);
		}
        return cursor;
	}

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String Table, String selection, String[] selectionArgs, String[] columns, String orderBy)
    {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Table);

        if (mDB == null)
        {
        	mDB = getReadableDatabase();
        }
        
        Cursor cursor = builder.query(mDB, columns, selection, selectionArgs, null, null, orderBy);
        
        if (cursor == null || cursor.isClosed())
        {
            return null;
        }
        else
        {
        	return cursor;
        }
    }
}
