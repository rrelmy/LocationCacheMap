package ch.rrelmy.android.locationcachemap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class LocationOverlay extends Overlay {
	private GeoPoint mSourcePoint;
	private float mAccuracy;

	public LocationOverlay() {
		super();
	}

	public void setSource(GeoPoint geoPoint, float accuracy) {
		mSourcePoint = geoPoint;
		mAccuracy = accuracy;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
		Projection projection = mapView.getProjection();
		Point center = new Point();

		int radius = (int) (projection.metersToEquatorPixels(mAccuracy));
		projection.toPixels(mSourcePoint, center);

		Paint accuracyPaint = new Paint();
		accuracyPaint.setAntiAlias(true);
		accuracyPaint.setStrokeWidth(2.0f);
		accuracyPaint.setColor(0xff6666ff);
		accuracyPaint.setStyle(Style.STROKE);

		canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

		accuracyPaint.setColor(0x186666ff);
		accuracyPaint.setStyle(Style.FILL);
		canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

	}
}
