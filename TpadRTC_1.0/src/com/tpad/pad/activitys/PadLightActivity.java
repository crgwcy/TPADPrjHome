package com.tpad.pad.activitys;
import com.RTC.TpadRTC.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class PadLightActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pad_light);
		((ImageButton)findViewById(R.id.pad_light_ret_btn_id)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pad_light, menu);
		return true;
	}*/
	@Override	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK )  
		{
		  finish();
		}	
		return true;
	};
}
