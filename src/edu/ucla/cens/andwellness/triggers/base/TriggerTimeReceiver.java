package edu.ucla.cens.andwellness.triggers.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

/*
 * Time/Time-zone change listener. Resets all the time stamps and 
 * restarts the triggers. 
 */
public class TriggerTimeReceiver extends BroadcastReceiver{

	private static final String DEBUG_TAG = "TriggerFramework";
	
	/*
	private static void clearTriggerTimeStamp(TriggerDB db, int trigId, String rtDesc) {
		TriggerRunTimeDesc desc = new TriggerRunTimeDesc();
		
		if(desc.loadString(rtDesc)) {
			desc.setTriggerTimeStamp(TriggerRunTimeDesc.INVALID_TIMESTAMP);
			db.updateRunTimeDescription(trigId, desc.toString());
		}
	}
	*/
	
	private static void handleTimeChange(Context context) {
		TriggerTypeMap trigMap = new TriggerTypeMap();
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		Cursor c = db.getAllTriggers();
		
		if(c.moveToFirst()) {
			do {
				int trigId = c.getInt(
							 c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
				String trigDesc = c.getString(
								  c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_DESCRIPT));
				String trigType = c.getString(
				 		   		  c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_TYPE));
				String actDesc = c.getString(
 		   		  				 c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_ACTION_DESCRIPT));
				
				//TODO enable this later
				//clearTriggerTimeStamp(db, trigId, rtDesc);
				
				TriggerBase trig = trigMap.getTrigger(trigType);
				if(trig != null) {
					TriggerActionDesc aDesc = new TriggerActionDesc();
					if(aDesc.loadString(actDesc) && aDesc.getCount() > 0) {
						trig.resetTrigger(context, trigId, trigDesc);
					}
				}
			} while(c.moveToNext());
		}
		
		c.close();
		db.close();
	}
	
	@Override
	public void onReceive(Context context, Intent i) {
		if(i.getAction().equals(Intent.ACTION_TIME_CHANGED) || 
		   i.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
			
			Log.i(DEBUG_TAG, "TriggerTimeReceiver: " + i.getAction());
			
			handleTimeChange(context);
		}
	}
}