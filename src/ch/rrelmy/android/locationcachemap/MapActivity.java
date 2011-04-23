package ch.rrelmy.android.locationcachemap;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapActivity extends com.google.android.maps.MapActivity {
	protected AppState mApp;
	// if you compile the app yourself you need to change the API key!
	public static final String API_KEY_DEBUG = "0PDkq2xA4PtT5-WMQw_izRXVe4PN-HBe-AMDP5g";
	public static final String API_KEY = "0PDkq2xA4PtQ2iB9kfC9Defl-yp7uGGlmQS0osQ";
	
	protected MapView mapView;
	protected MyItemizedOverlay cellOverlay;
	protected MyItemizedOverlay wifiOverlay;
	
	protected boolean showCell = false;
	protected boolean showWifi = false;
	
	protected static final short MENU_ITEM_CHANGE_MODE = 1;
	protected static final short MENU_ITEM_TOGGLE_CELL = 2;
	protected static final short MENU_ITEM_TOGGLE_WIFI = 3;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mApp = (AppState) getApplication();

        mapView = new MapView(this, API_KEY_DEBUG);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(12);
        
        this.setContentView(mapView);
        
        // cells
        int i = 0;
        if (mApp.mDbCell != null && mApp.mDbCell.getNumEntries() > 0) {
        	showCell = true;
        	Drawable drawableCell = this.getResources().getDrawable(R.drawable.marker_cell);
            cellOverlay = new MyItemizedOverlay(drawableCell, this);
        	
        	for (LocationCacheEntrie entrie : mApp.mDbCell.getEntries()) {
        		if (entrie.getAccuracy() < 0) {
        			continue;
        		}
        		
        		GeoPoint point = new GeoPoint((int)(entrie.getLatitude() * 1e6),(int)(entrie.getLongitude() * 1e6));
        		
        		if (i++ < 1) {
        			// first entrie
        			mapView.getController().setCenter(point);
        		}
        		
        		OverlayItem overlayitem = new OverlayItem(point, "Cell: " + entrie.getKey(), _getEntrieDescription(entrie));
                cellOverlay.addOverlay(overlayitem);
        	}
        	
        	mapView.getOverlays().add(cellOverlay);
        }
        
        // wifi
        if (mApp.mDbWifi != null && mApp.mDbWifi.getNumEntries() > 0) {
        	showWifi = true;
        	Drawable drawableWifi = this.getResources().getDrawable(R.drawable.marker_wifi);
            wifiOverlay = new MyItemizedOverlay(drawableWifi, this);
        	
        	for (LocationCacheEntrie entrie : mApp.mDbWifi.getEntries()) {
        		if (entrie.getAccuracy() < 0) {
        			continue;
        		}
        		GeoPoint point = new GeoPoint((int)(entrie.getLatitude() * 1e6),(int)(entrie.getLongitude() * 1e6));
        		
        		if (i++ < 1) {
        			// first entrie
        			mapView.getController().setCenter(point);
        		}
        		
                OverlayItem overlayitem = new OverlayItem(point, "WiFi: " + entrie.getKey(), _getEntrieDescription(entrie));
                wifiOverlay.addOverlay(overlayitem);
        	}
        	
        	mapView.getOverlays().add(wifiOverlay);
        }
    }
	
	protected String _getEntrieDescription(LocationCacheEntrie entrie)
	{
		return entrie.toString();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_ITEM_CHANGE_MODE, 0, mapView.isSatellite() ? "Normal" : "Satellite");
    	if (mApp.mDbCell != null && mApp.mDbCell.getNumEntries() > 0) {
    		menu.add(0, MENU_ITEM_TOGGLE_CELL, 0, "Toggle Cell");
    	}
    	if (mApp.mDbWifi != null && mApp.mDbWifi.getNumEntries() > 0) {
    		menu.add(0, MENU_ITEM_TOGGLE_WIFI, 0, "Toggle WiFi");
    	}
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case MENU_ITEM_CHANGE_MODE:
	            mapView.setSatellite(!mapView.isSatellite());
	            item.setTitle(mapView.isSatellite() ? "Normal" : "Satellite");
	            return true;
	        case MENU_ITEM_TOGGLE_CELL:
	        	showCell = !showCell;
	        	_updateMap();
	        	return true;
	        case MENU_ITEM_TOGGLE_WIFI:
	        	showWifi = !showWifi;
	        	_updateMap();
	        	return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    protected void _updateMap()
    {
    	mapView.getOverlays().clear();
    	
    	if (showWifi) {
    		mapView.getOverlays().add(wifiOverlay);
    	}
    	if (showCell) {
    		mapView.getOverlays().add(cellOverlay);
    	}
    	mapView.invalidate();
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	public class MyItemizedOverlay extends ItemizedOverlay {
		
		private Context mContext;
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		
		public MyItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}
		
		public void addOverlay(OverlayItem overlay) {
		    mOverlays.add(overlay);
		    populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
		  return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
		
		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}
		
	}
	
}
