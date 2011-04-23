package ch.rrelmy.android.locationcachemap;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

// TODO better error/exception management
// TODO test devices without busybox

public class Main extends Activity {
	
	protected AppState mApp;
	
	public static final String LOG_TAG = "LocCacheMap";
	public static final String CACHE_DIR = "/data/data/com.google.android.location/files";
	public static final String TMP_DIR = "/data/local/tmp/";
	
	protected TableLayout mMainLayout;
	protected TextView mTextLayout;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mApp = (AppState) getApplication();
        
        if (!RootTools.isRootAvailable()) {
            exitWithDialog("Sorry, you need to root your device to be able to use this application.", "Exit");
        } else if (!RootTools.isAccessGiven()) {
        	exitWithDialog("You have to grant the root privilegs, otherwise the app can't run.", "Exit");
        } else {
        	// we hope that everything works here... :)
        	_runApp();
        }
    }

    protected void _runApp()
    {
    	// do some cool stuff!
    	mMainLayout = new TableLayout(this);
    	mMainLayout.setPadding(20, 20, 20, 20);

        _initDatabases();

        TableRow row = new TableRow(this);
        mMainLayout.addView(row);
        _addDbInfoToLayout(row, mApp.mDbWifi, "WiFi");
        _addDbInfoToLayout(row, mApp.mDbCell, "Cell");
        
        if (mApp.mDbWifi != null || mApp.mDbCell != null) {
        	Button btnText = new Button(this);
        	btnText.setText("View list");
        	btnText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_viewText();
				}
			});
        	
        	Button btnMap = new Button(this);
        	btnMap.setText("View on map");
        	btnMap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_viewMap();
				}
			});
        	
        	mMainLayout.addView(btnText);
        	mMainLayout.addView(btnMap);
        	
        	TextView info = new TextView(this);
        	info.setText(
        			"\n\n" +
        			"Disclaimer\n" + 
        			"The data displayed comes from 2 files on your device. " +
        			"This application does only display this data!\n\n" +
        			
        			"Credits\n" +
        			"packetlss (information about the cache files)\n" +
        			"Stericson (RootTools)"
        	);
        	mMainLayout.addView(info);
        }
        
        this.setContentView(mMainLayout);
    }
    
    protected void _viewText()
    {
    	Intent intent = new Intent();
    	intent.setClassName(this.getPackageName(), this.getPackageName() + ".TextActivity");
		startActivity(intent);
    }
    
    protected void _viewMap()
    {
    	Intent intent = new Intent();
    	intent.setClassName(this.getPackageName(), this.getPackageName() + ".MapActivity");
		startActivity(intent);
    }
    
    protected void _addDbInfoToLayout(LinearLayout layout, LocationCacheDatabase db, String name)
    {
    	TableLayout tbl = new TableLayout(this);
    	tbl.setPadding(0, 0, 50, 0);
    	layout.addView(tbl);
    	
    	TextView title = new TextView(this);
    	title.setTextSize(20);
    	title.setText(name);
    	tbl.addView(title);
		
    	TextView info = new TextView(this);
    	tbl.addView(info);
    	if (db != null) {
    		info.setText("Version: " + db.getVersion() + "\nEntries: " + db.getNumEntries());
    	} else {
    		info.setText("unavailable");
    	}
    	info.append("\n\n");
	}

	protected void _initDatabases()
    {
    	// copy cache files to the tmp directory
    	_copyToTemp();

    	// wifi database
        try {
        	mApp.mDbWifi = new LocationCacheDatabase(LocationCacheDatabase.TYPE_WIFI, TMP_DIR + "cache.wifi");
		} catch (Exception e) {
			mApp.mDbWifi = null; // needed?
			Log.e(LOG_TAG, "" + e.getMessage());
			Toast.makeText(this, "Couldn't initialize Wifi database", Toast.LENGTH_SHORT).show();
		}
		
		// cell database
		try {
			mApp.mDbCell = new LocationCacheDatabase(LocationCacheDatabase.TYPE_CELL, TMP_DIR + "cache.cell");
		} catch (Exception e) {
			mApp.mDbCell = null; // needed?
			Log.e(LOG_TAG, "" + e.getMessage());
			Toast.makeText(this, "Couldn't initialize Wifi database", Toast.LENGTH_SHORT).show();
		}
        
        // remove the tmp files
        _removeTempFiles();
    }
    
    protected void _copyToTemp()
    {
    	try {
    		_removeTempFiles();
    		
			// copy files to tmp && get access to them
			RootTools.sendShell("cp " + CACHE_DIR + "/cache.* " + TMP_DIR);
			RootTools.sendShell("chmod 0777 " + TMP_DIR + "cache.*");
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (RootToolsException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
    }
    
    protected void _removeTempFiles()
    {
    	// we really don't wanna let this files lay around publicly available!!
    	try {
    		// delete old files
    		// rm cache.* (dangerous?)
			RootTools.sendShell("rm " + TMP_DIR + "cache.*");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (RootToolsException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
    }
	
	protected void exitWithDialog(String message, String btnText)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(message);
		dialog.setPositiveButton(btnText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish(); }
		});  
		dialog.show();
	}
}