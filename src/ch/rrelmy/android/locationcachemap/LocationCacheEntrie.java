package ch.rrelmy.android.locationcachemap;

import java.util.Date;

import android.text.format.DateFormat;

public class LocationCacheEntrie {

	protected String mKey;
	protected int mAccuracy;
	protected int mConfidence;
	protected double mLatitude;
	protected double mLongitude;
	protected long mDate;
	
	public void setKey(String key)
	{
		mKey = key;
	}
	
	public String getKey()
	{
		return mKey;
	}
	
	public void setAccuracy(int acc)
	{
		mAccuracy = acc;
	}
	
	public int getAccuracy()
	{
		return mAccuracy;
	}
	
	public void setConfidence(int conf)
	{
		mConfidence = conf;
	}
	
	public int getConfidence()
	{
		return mConfidence;
	}
	
	public void setLatitude(double lat)
	{
		mLatitude = lat;
	}
	
	public double getLatitude()
	{
		return mLatitude;
	}
	
	public void setLongitude(double lng)
	{
		mLongitude = lng;
	}
	
	public double getLongitude()
	{
		return mLongitude;
	}
	
	public void setDate(long date)
	{
		mDate = date;
	}
	
	public long getDate()
	{
		return mDate;
	}
	
	public String toString()
	{
		return
			mKey + "\n" +
			"accuracy: " + mAccuracy + "\n" + 
			"confidence: " + mConfidence + "\n" +
			"latitude: " + mLatitude + "\n" +
			"longitude: " + mLongitude + "\n" + 
			"date: " + DateFormat.format("dd.MM.yyyy kk:mm", new Date(mDate)) + "\n"
		;
	}
}
