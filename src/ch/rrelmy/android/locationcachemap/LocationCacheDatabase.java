package ch.rrelmy.android.locationcachemap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class LocationCacheDatabase {

	public static final String TYPE_CELL = "cell";
	public static final String TYPE_WIFI = "wifi";
	
	protected String mType;
	protected String mFilename;
	protected short mVersion;
	protected short mNumEntries;
	protected LocationCacheEntrie[] mEntries;
	
	protected short mNumEntriesWithPos = 0;
	
	public LocationCacheDatabase(String type, String filename) throws Exception
	{
		mType = type;
		mFilename = filename;
		
		// init
		_init();
	}
	
	protected void _init() throws Exception
	{		
		try {
			RandomAccessFile in = new RandomAccessFile(mFilename, "r");
			if (in.length() < 4) {
				throw new Exception("empty cache file");
			}
			
			mVersion = in.readShort();
			mNumEntries = in.readShort();
			
			mEntries = new LocationCacheEntrie[mNumEntries];
			
			for (short i = 0; i < mNumEntries; ++i) {
				LocationCacheEntrie entrie = new LocationCacheEntrie();
				
				// keyLength short
				short keyLength = in.readShort();
				// key ${keyLength} bytes
				String key = "";
				for (int y = 0; y < keyLength; ++y) {
					key += (char) in.readByte();
				}
				entrie.setKey(key);
				
				// accuracy int
				entrie.setAccuracy(in.readInt());
				if (entrie.getAccuracy() >= 0) {
					mNumEntriesWithPos++;
				}
				
				// confidence int
				entrie.setConfidence(in.readInt());
				
				// lat double
				entrie.setLatitude(in.readDouble());

				// lng double
				entrie.setLongitude(in.readDouble());
				
				// time long
				entrie.setDate(in.readLong());
				
				mEntries[i] = entrie;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			e.printStackTrace();
			Log.e(Main.LOG_TAG, "" + e.getMessage());
			throw e;
		}
	}
	
	public short getVersion()
	{
		return mVersion;
	}
	
	public short getNumEntries()
	{
		return mNumEntries;
	}
	
	public short getNumEntriesWithPos()
	{
		return mNumEntriesWithPos;
	}
	
	public LocationCacheEntrie[] getEntries()
	{
		return mEntries;
	}
	
	public String getType()
	{
		return mType;
	}
	
	public void writeGpx(BufferedWriter out) throws IOException
	{	
		for (LocationCacheEntrie entrie : getEntries()) {
			if (entrie.getAccuracy() < 0) {
				continue;
			}
			out.write(entrie.toGpx(getType()));
		}
		
	}
	
}
