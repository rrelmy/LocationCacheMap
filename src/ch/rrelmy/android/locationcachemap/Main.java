package ch.rrelmy.android.locationcachemap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
	public static final String TMP_DIR = "/data/tmp/";
	
	public static final short MENU_ITEM_ABOUT = 1;
	
	protected TableLayout mMainLayout;
	protected TextView mTextLayout;
	protected TextView mInfo;
	
	protected TableLayout mBtnLockLayout;
	protected Button mBtnFlush;
	protected Button mBtnBlock;
	protected Button mBtnUnblock;
	
	protected short mImmutable = -1;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
             
        mApp = (AppState) getApplication();
        
        if (!RootTools.isRootAvailable()) {
            exitWithDialog("Sorry, you need to root your device to be able to use this application.", "Exit");
        } else if (!RootTools.isAccessGiven()) {
        	exitWithDialog("You have to grant root privilegs, otherwise the app won't work.", "Exit");
        } else {
        	// we hope that everything works here... :)
        	_runApp();
        }
    }

    protected void _runApp()
    {
    	// build the layout
    	mMainLayout = new TableLayout(this);
    	mMainLayout.setPadding(20, 20, 20, 20);

    	// load databeses
        _initDatabases();

        TableRow row = new TableRow(this);
        mMainLayout.addView(row);
        _addDbInfoToLayout(row, mApp.mDbWifi, "WiFi");
        _addDbInfoToLayout(row, mApp.mDbCell, "Cell");
        
        if (mApp.mDbWifi != null || mApp.mDbCell != null) {
        	Button btnText = new Button(this);
        	btnText.setText("view list");
        	btnText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_viewText();
				}
			});
        	mMainLayout.addView(btnText);
        	
        	if (
        			(mApp.mDbWifi != null && mApp.mDbWifi.getNumEntriesWithPos() > 0) || 
        			(mApp.mDbCell != null && mApp.mDbCell.getNumEntriesWithPos() > 0)
        	) {
        		// show on map
	        	Button btnMap = new Button(this);
	        	btnMap.setText("view map");
	        	btnMap.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_viewMap();
					}
				});
	        	mMainLayout.addView(btnMap);
	        	
	        	// export gpx
	        	Button btnExport = new Button(this);
	        	btnExport.setText("export gpx to sdcard");
	        	btnExport.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						exportGpx();
					}
				});
	        	
	        	mMainLayout.addView(btnExport);
        	} else {
        		TextView mapInfo = new TextView(this);
        		mapInfo.setPadding(0, 10, 0, 10);
        		mapInfo.setText("Map not available.\nThere is some data but no gps data attached with it.");
        		mMainLayout.addView(mapInfo);
        	}
        	
        	// flush databases
        	mBtnFlush = new Button(this);
        	mBtnFlush.setText("empty caches");
        	mBtnFlush.setTextColor(Color.RED);
        	mBtnFlush.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_showFlushDialog();
				}
			});
        	
        	mMainLayout.addView(mBtnFlush);
        	getGpxContent();
        }
        
        // block + unblock
        if (RootTools.isBusyboxAvailable()) {
        	// block
    		mBtnBlock = new Button(this);
    		mBtnBlock.setText("empty & block caches");
    		mBtnBlock.setTextColor(Color.RED);
    		mBtnBlock.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_showBlockDialog();
				}
			});
    		
    		// unblock
    		mBtnUnblock = new Button(this);
    		mBtnUnblock.setText("unblock cache files");
    		mBtnUnblock.setTextColor(Color.RED);
    		mBtnUnblock.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_unblock();
					
					mBtnLockLayout.removeView(mBtnUnblock);
					mBtnLockLayout.addView(mBtnBlock);
					Toast.makeText(mApp, "cache files unblocked", Toast.LENGTH_SHORT).show();
				}
			});
    		
    		// layout
    		mBtnLockLayout = new TableLayout(this);
    		mMainLayout.addView(mBtnLockLayout);
    		
    		Log.i(LOG_TAG, "cache blocked: " + _isBlocked());
        	if (_isBlocked()) {
        		// unblock
        		mBtnLockLayout.addView(mBtnUnblock);
        	} else {
        		// block
        		mBtnLockLayout.addView(mBtnBlock);
        	}
        } else {
        	TextView mBlockInfo = new TextView(this);
        	mBlockInfo.setText("blocking is only available if busybox is installed!");
        	mBlockInfo.setTextColor(Color.RED);
        	mMainLayout.addView(mBlockInfo);
        }
        
        // info
        mInfo = new TextView(this);
        mInfo.setPadding(0, 20, 0, 0);
        mInfo.setText(
        		"If no data is found you most likely disabled «Use wireless networks» under «Location & Security» settings or you already have deleted it.\n" +
        		"If you disable this option no data will be recorded but it needs longer to search for your location on Maps etc.\n" +
        		"\n" +
        		"Blocking with this application does not disable «Use wireless networks», the location lock remains fast and no data will be leftover on your device."
        );
        mMainLayout.addView(mInfo);
        
    	ScrollView scrollView = new ScrollView(this);
    	scrollView.addView(mMainLayout);
    	
        this.setContentView(scrollView);
    }
    
    protected void _showFlushDialog()
    {
    	if (_isBlocked()) {
    		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        	dialog.setTitle("Warning");
    		dialog.setMessage(
    				"You need to unlock the cache files first!"
    		);
    		
    		dialog.setPositiveButton("Ok", null);
    		dialog.show();
    		return;
    	}
    	
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	dialog.setTitle("Warning");
		dialog.setMessage(
				"This will REBOOT your device automatically!\n" +
				"The cache files will be deleted and the device will be rebooted.\n" +
				"If the cache does show the same entries again, try this action while in Airplane mode"
		);
		
		dialog.setPositiveButton("Let's do it!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// do that
				_flushCaches();
			}
		});
		dialog.setNegativeButton("Stop", null);
		dialog.show();
    }
    
    protected void _showBlockDialog()
    {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	dialog.setTitle("Warning");
		dialog.setMessage(
				"Cache will be deleted!\n" + 
				"This action can be undone, but the cache will be empty.\n" + 
				"This could have impact on GPS Location performance! If you have problems report them!"
		);
		
		dialog.setPositiveButton("Let's do it!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// do that
				_flushAndBlockCaches();
				mBtnLockLayout.removeView(mBtnBlock);
				mBtnLockLayout.addView(mBtnUnblock);
				if (mBtnFlush != null) {
					mMainLayout.removeView(mBtnFlush);
				}
				Toast.makeText(mApp, "cache files blocked", Toast.LENGTH_SHORT).show();
			}
		});
		dialog.setNegativeButton("Stop", null);
		dialog.show();
    }
    
    protected void _flushCaches()
    {
    	// hardcore -> delete them
    	//-rw------- system   system                361 2011-04-23 12:00 cache.wifi
    	//-rw------- system   system               1189 2011-04-23 12:00 cache.cell
    	try {
			RootTools.sendShell("rm " + CACHE_DIR + "/cache.wifi && rm  " + CACHE_DIR + "/cache.cell");
			RootTools.sendShell("reboot");
			Toast.makeText(this, "REBOOT YOUR DEVICE NOW!", Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "" + e.getMessage());
			Toast.makeText(this, "Error flushing the caches", Toast.LENGTH_LONG).show();
		}
    }
    
    protected void _flushAndBlockCaches()
    {
    	try {
    		RootTools.sendShell("rm " + CACHE_DIR + "/cache.wifi && rm  " + CACHE_DIR + "/cache.cell");
        	RootTools.sendShell("touch " + CACHE_DIR + "/cache.wifi && touch  " + CACHE_DIR + "/cache.cell");
        	if (supportsImmutable()) {
        		RootTools.sendShell("chattr +i " + CACHE_DIR + "/cache.wifi && chattr +i  " + CACHE_DIR + "/cache.cell");
        	}
        	RootTools.sendShell("chmod 0000 " + CACHE_DIR + "/cache.wifi && chmod 0000  " + CACHE_DIR + "/cache.cell");
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error please look at logcat output", Toast.LENGTH_LONG);
		}
    }
    
    protected boolean _isBlocked()
    {
    	try {
    		List<String> result;
    		if (supportsImmutable()) {
    			// immutable
    			Log.i(LOG_TAG, "immutable blocking");
	    		result = RootTools.sendShell("busybox lsattr " + CACHE_DIR + "| grep -i cache");
	    		if (result.size() > 0) {
	    			String res = result.get(0);
	    			return res.substring(0, res.indexOf(" ")).contains("i");
	    		}
    		} else {
    			// chmodded
    			Log.i(LOG_TAG, "chmod blocking");
    			result = RootTools.sendShell("ls -l " + CACHE_DIR + "| grep -i cache");
    			if (result.size() > 0) {
    				String res = result.get(0);
    				Log.i(LOG_TAG, res);
    				return !res.substring(0, 4).contains("w");
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    protected void _unblock()
    {
    	try {
    		if (supportsImmutable()) {
    			RootTools.sendShell("busybox chattr -i " + CACHE_DIR + "/cache.wifi && busybox chattr -i  " + CACHE_DIR + "/cache.cell");
    		}
    		RootTools.sendShell("rm " + CACHE_DIR + "/cache.wifi && rm  " + CACHE_DIR + "/cache.cell");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
    	intent.setClassName(this.getPackageName(), this.getPackageName() + ".ShowMapActivity");
		startActivity(intent);
    }
    
    protected void _viewAbout()
    {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	dialog.setTitle("About");
    	
    	TextView about = new TextView(this);
    	about.setPadding(10, 10, 10, 10);
    	about.setText(Html.fromHtml(
				"<p><b>Disclaimer</b><br />" + 
    			"The data displayed comes from 2 files on your device. " +
    			"This application does only display this data!</p>" +
    			
    			"<p>License: GPLv3<br />" +
    			"Source: <a href=\"https://github.com/rrelmy/LocationCacheMap\">github.com</a><br />" +
    			"Contact: <a href=\"mailto:remyboehler@gmail.com\">remyboehler@gmail.com</a></p>" +
    			
    			"<p><b>Credits</b><br />" +
    			"<a href=\"https://twitter.com/packetlss\">packetlss</a> (<a href=\"https://github.com/packetlss/android-locdump\">android-locdump</a>)<br />" +
    			"<a href=\"https://twitter.com/Stericson\">Stericson</a> (<a href=\"https://code.google.com/p/roottools/\">RootTools</a>)</p>"
		));
    	about.setMovementMethod(LinkMovementMethod.getInstance());
    	
		dialog.setView(about);

		dialog.show();
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
    		info.setText("no data found");
    	}
    	info.append("\n");
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
			//Toast.makeText(this, "Couldn't initialize Wifi database", Toast.LENGTH_SHORT).show();
		}
		
		// cell database
		try {
			mApp.mDbCell = new LocationCacheDatabase(LocationCacheDatabase.TYPE_CELL, TMP_DIR + "cache.cell");
		} catch (Exception e) {
			mApp.mDbCell = null; // needed?
			Log.e(LOG_TAG, "" + e.getMessage());
			//Toast.makeText(this, "Couldn't initialize Wifi database", Toast.LENGTH_SHORT).show();
		}
        
        // remove the tmp files
        _removeTempFiles();
    }
    
    protected void _copyToTemp()
    {
    	// create temporary directory if it does not exist
    	try {
    		File tmpDir = new File(TMP_DIR);
    		if (!tmpDir.exists()) {
    			RootTools.sendShell("mkdir " + TMP_DIR + " && chmod 0777 " + TMP_DIR);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
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
	
	public boolean supportsImmutable()
	{
		if (mImmutable == -1) {
			mImmutable = 0;
			// busybox with lsattr and chattr applets is required
			
			if (RootTools.isBusyboxAvailable()) {
	
				// check for applets
				boolean hazLsAttr = false;
				boolean hazChAttr = false;
				try {
					List<String> result = RootTools.sendShell("busybox --list | grep -i attr");
					
					for (String applet : result) {
						if (applet.trim().equals("chattr")) {
							hazChAttr = true;
						} else if (applet.trim().equals("lsattr")) {
							hazLsAttr = true;
						}
					}
					mImmutable = (short) (hazLsAttr && hazChAttr ? 1 : 0);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return mImmutable == 1;
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_ITEM_ABOUT, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case MENU_ITEM_ABOUT:
	            _viewAbout();
	            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public void exportGpx()
    {
    	try {
    	    File root = Environment.getExternalStorageDirectory();
    	    if (root.canWrite()){
    	        // write
    	    	File gpxfile = new File(root, "location-cache-export-" + (new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date()).toString()) + ".gpx");
    	        FileWriter gpxwriter = new FileWriter(gpxfile);
    	        BufferedWriter out = new BufferedWriter(gpxwriter);
    	        out.write(getGpxContent());
    	        out.close();
    	        
    	        // dialog
    	        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	    	dialog.setTitle("Export");
    			dialog.setMessage(
    					"GPX file exported to:\n" +
    					gpxfile.getAbsolutePath()
    			);
    			dialog.setPositiveButton("Ok", null);

    			dialog.show();
    	    } else {
    	    	throw new IOException("not writeable");
    	    }
    	} catch (IOException e) {
    		e.printStackTrace();
    	    Log.e(LOG_TAG, "Could not write file " + e.getMessage());
    	    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
	
    public String getGpxContent()
    {
    	// export a valid gpx file
    	
    	String content = "";
    	short numEntries = 0;
    	
    	
    	if (mApp.mDbCell != null && mApp.mDbCell.getNumEntriesWithPos() > 0) {
    		numEntries += mApp.mDbCell.getNumEntriesWithPos();
    		content += mApp.mDbCell.toGpxEntries();
    	}
    	
    	if (mApp.mDbWifi != null && mApp.mDbWifi.getNumEntriesWithPos() > 0) {
    		numEntries += mApp.mDbWifi.getNumEntriesWithPos();
    		content += mApp.mDbWifi.toGpxEntries();
    	}
    	
    	content =
    			"<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" creator=\"Location Cache Map\">\n" +
    				"<metadata>\n" + 
    					"<name>Android Location Cache</name>\n" +
    					"<desc>total entries: " + numEntries + "</desc>\n" +
    				"</metadata>\n" +
    				"<trk>\n" +
    					"<trkseg>\n" +
    					
    					content +
    					
    					"</trkseg>\n" +
    				"</trk>\n" +
    			"</gpx>\n";
    	
    	return content;
    }
    
}