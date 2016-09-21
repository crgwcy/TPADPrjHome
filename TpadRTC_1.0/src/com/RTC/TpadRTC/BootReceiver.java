package com.RTC.TpadRTC;

import com.tpad.pad.activitys.PadStandbyActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
/*				try {
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
*/

				Intent intentBoot=new Intent(context,PadStandbyActivity.class);
				intentBoot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentBoot);	
			}
		}).start();

	}

}
