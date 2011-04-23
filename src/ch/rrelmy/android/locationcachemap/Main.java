package ch.rrelmy.android.locationcachemap;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
	public static final String TMP_DIR = "/data/local/tmp/";
	
	protected TableLayout mMainLayout;
	protected TextView mTextLayout;
	protected TextView mInfo;
	
	protected TableLayout mBtnLockLayout;
	protected Button mBtnBlock;
	protected Button mBtnUnblock;
	
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
        	
        	// flush databases
        	Button btnFlush = new Button(this);
        	btnFlush.setText("Empty databases");
        	btnFlush.setTextColor(Color.RED);
        	btnFlush.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_showFlushDialog();
				}
			});
        	
        	mMainLayout.addView(btnFlush);
        }
        
        // block + unblock
        if (supportsBlocking()) {
        	// block
    		mBtnBlock = new Button(this);
    		mBtnBlock.setText("block cache files");
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
    		
        	if (_isBlocked()) {
        		// unblock
        		mBtnLockLayout.addView(mBtnUnblock);
        	} else {
        		// block
        		mBtnLockLayout.addView(mBtnBlock);
        	}
        } else {
        	TextView mBlockInfo = new TextView(this);
        	mBlockInfo.setText("blocking is only available if busybox is installed with lsattr and chattr applets!");
        	mBlockInfo.setTextColor(Color.RED);
        	mMainLayout.addView(mBlockInfo);
        }
        
        // info
        TextView mInfo = new TextView(this);
    	mInfo.setText(
    			"\n\n" +
    			"Disclaimer\n" + 
    			"The data displayed comes from 2 files on your device. " +
    			"This application does only display this data!\n\n" +
    			
    			"Credits\n" +
    			"packetlss (information about the cache files)\n" +
    			"Stericson (RootTools)"
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
			RootTools.sendShell("chattr +i " + CACHE_DIR + "/cache.wifi && chattr +i  " + CACHE_DIR + "/cache.cell");
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error please look at logcat output", Toast.LENGTH_LONG);
		}
    }
    
    protected boolean _isBlocked()
    {
    	try {
    		List<String> result = RootTools.sendShell("busybox lsattr " + CACHE_DIR + "| grep -i cache");
    		if (result.size() > 0) {
    			String res = result.get(0);
    			return res.substring(0, res.indexOf(" ")).contains("i");
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    protected void _unblock()
    {
    	try {
    		RootTools.sendShell("bosybox chattr -i " + CACHE_DIR + "/cache.wifi && busybox chattr -i  " + CACHE_DIR + "/cache.cell");
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
	
	public boolean supportsBlocking()
	{
		// busybox with lsattr and chattr applets is required
		
		if (RootTools.isBusyboxAvailable()) {
			
			// check for applets
			boolean hazLsAttr = false;
			boolean hazChAttr = false;
			try {
				List<String> result = RootTools.sendShell("busybox --list | grep -i attr");
				
				for (String applet : result) {
					Log.i(LOG_TAG, ">" + applet.trim() + "<");
					if (applet.trim().equals("chattr")) {
						Log.i(LOG_TAG, "cmp: " + applet.trim().equals("chattr"));
						hazChAttr = true;
					} else if (applet.trim().equals("lsattr")) {
						hazLsAttr = true;
					}
				}
				Log.i(LOG_TAG, "lsattr: " + hazLsAttr);
				Log.i(LOG_TAG, "chattr: " + hazChAttr);
				return (hazLsAttr && hazChAttr);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
}