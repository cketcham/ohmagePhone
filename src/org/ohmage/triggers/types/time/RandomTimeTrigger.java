/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.triggers.types.time;

import org.ohmage.R;
import org.ohmage.triggers.ui.TriggerListActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class RandomTimeTrigger extends TimeTrigger {
	protected static final String DEBUG_TAG = "RandomTimeTrigger";	

	private static final String TRIGGER_TYPE = "RandomTimeTrigger";
	
	@Override
	public String getTriggerTypeDisplayName(Context context) {
		return context.getString(R.string.trigger_random_time_display_name);
	}

	@Override
	public String getTriggerType() {
		return TRIGGER_TYPE;
	}
	
	@Override
	protected TimeTrigDesc getTimeTrigDesc() {
		return new RandomTimeTrigDesc();
	}
	
	@Override
	public void launchTriggerCreateActivity(Context context, final String campaignUrn, String[] actions, boolean adminMode) {
		
		RandomTimeTrigEditActivity.setOnExitListener(
					new RandomTimeTrigEditActivity.ExitListener() {
			
			@Override
			public void onDone(Context context, int trigId, 
							   String trigDesc, String actDesc) {
				
				Log.i(DEBUG_TAG, "TimeTrigger: Saving new trigger: " + trigDesc);
				addNewTrigger(context, campaignUrn, trigDesc, actDesc);
			}
		});
		
		Intent i = new Intent(context, RandomTimeTrigEditActivity.class);
		i.putExtra(TriggerListActivity.KEY_ACTIONS, actions);
		i.putExtra(RandomTimeTrigEditActivity.KEY_ADMIN_MODE, adminMode);
		context.startActivity(i);
	}

	@Override
	public void launchTriggerEditActivity(Context context, int trigId, String trigDesc, String actDesc, String[] mActions,
										  boolean adminMode) {
		
		RandomTimeTrigEditActivity.setOnExitListener(
				new RandomTimeTrigEditActivity.ExitListener() {
		
			@Override
			public void onDone(Context context, int trigId, 
							   String trigDesc, String actDesc) {
				
				updateTrigger(context, trigId, trigDesc, actDesc);
			}
		});
	
	
		Intent i = new Intent(context, RandomTimeTrigEditActivity.class);
		i.putExtra(RandomTimeTrigEditActivity.KEY_TRIG_ID, trigId);
		i.putExtra(RandomTimeTrigEditActivity.KEY_TRIG_DESC, trigDesc);
		i.putExtra(RandomTimeTrigEditActivity.KEY_ACT_DESC, actDesc);
		i.putExtra(RandomTimeTrigEditActivity.KEY_ADMIN_MODE, adminMode);
		i.putExtra(TriggerListActivity.KEY_ACTIONS, mActions);
		context.startActivity(i);
	}
}
