package ch.rrelmy.android.locationcachemap;

import android.content.Context;
import android.view.View;


public class DoubleSlider extends View {

	protected Context mContext;
	
	protected int mLimitMin;
	protected int mLimitMax;
	
	protected int mOffsetLeft;
	protected int mOffsetRight;
	
	public DoubleSlider(Context context) {
		super(context);
		mContext = context;
	}

	public void setLimits(int min, int max) {
		mLimitMin = Math.min(min, max);
		mLimitMax = Math.max(min, max);
		invalidate();
	}
	
	public int getLimitMin() {
		return mLimitMin;
	}

	public int getLimitMax() {
		return mLimitMax;
	}
	
	public int getOffsetLeft() {
		return mOffsetLeft;
	}
	
	public void setOffsetLeft(int offset) {
		mOffsetLeft = offset;
		invalidate();
	}
	
	public int getOffsetRight() {
		return mOffsetLeft;
	}
	
	public void setOffsetRight(int offset) {
		mOffsetRight = offset;
		invalidate();
	}
	
}
