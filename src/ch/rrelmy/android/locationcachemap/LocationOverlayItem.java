package ch.rrelmy.android.locationcachemap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class LocationOverlayItem extends OverlayItem {

	protected int mAccuracy;
	
	public LocationOverlayItem(GeoPoint point, String title, String snippet)
	{
		super(point, title, snippet);
	}

	public LocationOverlayItem(GeoPoint point, String title, String snippet, int accuracy)
	{
		super(point, title, snippet);
		mAccuracy = accuracy;
	}
	
	public void setAccuracy(int accuracy)
	{
		mAccuracy = accuracy;
	}
	
	public int getAccuracy()
	{
		return mAccuracy;
	}
}
