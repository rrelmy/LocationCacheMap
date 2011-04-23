package ch.rrelmy.android.locationcachemap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TextActivity extends Activity {
	protected AppState mApp;
	protected TableLayout mLayout;
	protected ScrollView mScroll;
	protected TableLayout mContent;
	protected Button mBtnBack;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mApp = (AppState) getApplication();
        
        // layout
        mLayout = new TableLayout(this);
        mScroll = new ScrollView(this);
        mContent = new TableLayout(this);

        // back btn
        mBtnBack = new Button(this);
        mBtnBack.setText("back");
        mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
        mLayout.addView(mScroll);
        mLayout.addView(mBtnBack);
        mScroll.addView(mContent);
        
        setContentView(mLayout);
        
        _addDatabase(mApp.mDbCell);
        _addDatabase(mApp.mDbWifi);
    }
	
	protected void _addDatabase(LocationCacheDatabase db)
	{	
		if (db == null || db.getNumEntries() < 1) {
			return;
		}

		LinearLayout layout = new LinearLayout(this);
		
		TextView title = new TextView(this);
		title.setTextSize(20);
		title.setText(db.getType());
		
		TableRow titleRow = new TableRow(this);
		titleRow.setPadding(20, 20, 20, 20);
		titleRow.addView(title);
		
		TableRow contentRow = new TableRow(this);
		contentRow.setPadding(0, 20, 20, 20);
		
		TextView content = new TextView(this);
		contentRow.addView(content);
		
		for (LocationCacheEntrie entrie : db.getEntries()) {
			content.append(entrie.toString() + "\n");
		}
		
		layout.addView(titleRow);
		layout.addView(contentRow);
		
		mContent.addView(layout);
	}
	
}
