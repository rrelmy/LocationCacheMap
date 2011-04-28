package ch.rrelmy.android.locationcachemap;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ShowMapActivity extends MapActivity {
	protected AppState mApp;
	// if you compile the app yourself you need to change the API key!
	public static final String API_KEY_DEBUG = "0PDkq2xA4PtT5-WMQw_izRXVe4PN-HBe-AMDP5g";
	public static final String API_KEY = "0PDkq2xA4PtQ2iB9kfC9Defl-yp7uGGlmQS0osQ";
	
	protected MapView mMapView;
	protected MyItemizedOverlay mCellOverlay;
	protected MyItemizedOverlay mWifiOverlay;
	
	protected boolean mShowCell = false;
	protected boolean mShowWifi = false;
	
	protected boolean mShowAccuracy = true;
	
	protected static final short MENU_ITEM_CHANGE_MODE = 1;
	protected static final short MENU_ITEM_TOGGLE_CELL = 2;
	protected static final short MENU_ITEM_TOGGLE_WIFI = 3;
	protected static final short MENU_ITEM_TOGGLE_ACCURACY = 4;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mApp = (AppState) getApplication();

        RelativeLayout mFrame = new RelativeLayout(this);
        
        mMapView = new MapView(this, API_KEY_DEBUG);
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.getController().setZoom(10);
        mMapView.setSatellite(false);
        
        mFrame.addView(mMapView);
        
        this.setContentView(mFrame);
        
        // cells
        int i = 0;
        if (mApp.mDbCell != null && mApp.mDbCell.getNumEntries() > 0) {
        	mShowCell = true;
        	Drawable drawableCell = this.getResources().getDrawable(R.drawable.marker_cell);
            mCellOverlay = new MyItemizedOverlay(drawableCell, this);
        	
        	for (LocationCacheEntrie entrie : mApp.mDbCell.getEntries()) {
        		if (entrie.getAccuracy() < 0) {
        			continue;
        		}
        		
        		GeoPoint point = new GeoPoint((int)(entrie.getLatitude() * 1e6),(int)(entrie.getLongitude() * 1e6));
        		
        		if (i++ < 1) {
        			// first entrie
        			mMapView.getController().setCenter(point);
        		}
        		
        		LocationOverlayItem overlayItem = new LocationOverlayItem(point, "Cell: " + entrie.getKey(), _getEntrieDescription(entrie));
        		overlayItem.setAccuracy(entrie.getAccuracy());
                mCellOverlay.addOverlay(overlayItem);
        	}
        	
        	if (mCellOverlay.size() > 0) {
        		mMapView.getOverlays().add(mCellOverlay);
        	} else {
        		mCellOverlay = null;
        	}
        }
        
        // wifi
        if (mApp.mDbWifi != null && mApp.mDbWifi.getNumEntries() > 0) {
        	mShowWifi = true;
        	Drawable drawableWifi = this.getResources().getDrawable(R.drawable.marker_wifi);
            mWifiOverlay = new MyItemizedOverlay(drawableWifi, this);
        	
        	for (LocationCacheEntrie entrie : mApp.mDbWifi.getEntries()) {
        		if (entrie.getAccuracy() < 0) {
        			continue;
        		}
        		GeoPoint point = new GeoPoint((int)(entrie.getLatitude() * 1e6),(int)(entrie.getLongitude() * 1e6));
        		
        		if (i++ < 1) {
        			// first entrie
        			mMapView.getController().setCenter(point);
        		}
        		
        		LocationOverlayItem overlayItem = new LocationOverlayItem(point, "WiFi: " + entrie.getKey(), _getEntrieDescription(entrie));
        		overlayItem.setAccuracy(entrie.getAccuracy());
                //overlayitem.setAccura
                mWifiOverlay.addOverlay(overlayItem);
        	}
        	
        	if (mWifiOverlay.size() > 0) {
        		mMapView.getOverlays().add(mWifiOverlay);
        	} else {
        		mWifiOverlay = null;
        	}
        }
    }
	
	protected String _getEntrieDescription(LocationCacheEntrie entrie)
	{
		return entrie.toString();
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
    	menu.add(0, MENU_ITEM_CHANGE_MODE, 0, mMapView.isSatellite() ? "Map" : "Satellite").setIcon(android.R.drawable.ic_menu_mapmode);
    	if (mApp.mDbCell != null && mApp.mDbCell.getNumEntriesWithPos() > 0) {
    		menu.add(0, MENU_ITEM_TOGGLE_CELL, 0, "Toggle Cell").setIcon(R.drawable.marker_cell);
    	}
    	if (mApp.mDbWifi != null && mApp.mDbWifi.getNumEntriesWithPos() > 0) {
    		menu.add(0, MENU_ITEM_TOGGLE_WIFI, 0, "Toggle WiFi").setIcon(R.drawable.marker_wifi);
    	}
    	menu.add(0, MENU_ITEM_TOGGLE_ACCURACY, 0, "Toggle Accuracy");
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
	        case MENU_ITEM_CHANGE_MODE:
	        	mMapView.setSatellite(!mMapView.isSatellite());
	            item.setTitle(mMapView.isSatellite() ? "Normal" : "Satellite");
	            return true;
	        case MENU_ITEM_TOGGLE_CELL:
	        	mShowCell = !mShowCell;
	        	_updateMap();
	        	return true;
	        case MENU_ITEM_TOGGLE_WIFI:
	        	mShowWifi = !mShowWifi;
	        	_updateMap();
	        	return true;
	        case MENU_ITEM_TOGGLE_ACCURACY:
	        	mShowAccuracy = !mShowAccuracy;
	        	mMapView.invalidate();
	        	return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    protected void _updateMap()
    {
    	mMapView.getOverlays().clear();
    	
    	if (mShowWifi && mWifiOverlay != null && mWifiOverlay.size() > 0) {
    		mMapView.getOverlays().add(mWifiOverlay);
    	}
    	if (mShowCell && mCellOverlay != null && mCellOverlay.size() > 0) {
    		mMapView.getOverlays().add(mCellOverlay);
    	}
    	mMapView.invalidate();
    }
	
	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
	
	public class MyItemizedOverlay extends ItemizedOverlay<LocationOverlayItem>
	{
		
		private Context mContext;
		private ArrayList<LocationOverlayItem> mOverlays = new ArrayList<LocationOverlayItem>();
		
		public MyItemizedOverlay(Drawable defaultMarker, Context context)
		{
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}
		
		public void addOverlay(LocationOverlayItem overlay)
		{
		    mOverlays.add(overlay);
		    populate();
		}
		
		@Override
		protected LocationOverlayItem createItem(int i)
		{
		  return mOverlays.get(i);
		}

		@Override
		public int size()
		{
			return mOverlays.size();
		}
		
		@Override
		public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow)
		{
			if (mShowAccuracy) {
				Paint paint = new Paint();
				paint.setColor(0x16FF0000);
				paint.setAntiAlias(true);
							
				for (LocationOverlayItem overlay : mOverlays) {
					int radius = (int) (mapView.getProjection().metersToEquatorPixels(overlay.getAccuracy()));
					GeoPoint geo = overlay.getPoint();
					Point point = new Point();
					mapView.getProjection().toPixels(geo, point);
					canvas.drawCircle(point.x, point.y, radius, paint);
				}
			}
			super.draw(canvas, mapView, shadow);
		}
		
		@Override
		protected boolean onTap(int index)
		{
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}
		
	}
	
}
