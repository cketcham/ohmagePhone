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

import java.util.LinkedHashMap;

import org.ohmage.R;
import org.ohmage.triggers.base.TriggerActionDesc;
import org.ohmage.triggers.config.TrigUserConfig;
import org.ohmage.triggers.ui.TriggerListActivity;
import org.ohmage.triggers.utils.TimePickerPreference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RandomTimeTrigEditActivity extends PreferenceActivity 
							implements View.OnClickListener, 
									   OnPreferenceClickListener, 
									   OnPreferenceChangeListener, 
									   DialogInterface.OnMultiChoiceClickListener, 
									   DialogInterface.OnClickListener {
	
	private static final String DEBUG_TAG = "RandomTimeTrigEditActivity";
	
	public static final String KEY_TRIG_DESC = "trig_desc";
	public static final String KEY_ACT_DESC = "act_desc";
	public static final String KEY_TRIG_ID = "trig_id";
	public static final String KEY_ADMIN_MODE = "admin_mode";
	private static final String KEY_SAVE_DAYS = "days";
	private static final String KEY_SAVE_REPEAT_STATUS = "repeat_status";
	
	private static final String PREF_KEY_START_TIME = "interval_start_time";
	private static final String PREF_KEY_END_TIME = "interval_end_time";
	private static final String PREF_KEY_REPEAT_DAYS = "repeat_days";
	private static final String PREF_KEY_ACTIONS = "actions";
	
	private static final int DIALOG_ID_REPEAT_SEL = 0;
	private static final int DIALOG_ID_INVALID_TIME_ALERT = 1;
	private static final int DIALOG_ID_ACTION_SEL = 2;
	private static final int DIALOG_ID_NO_SURVEYS_SELECTED = 3;

	private RandomTimeTrigDesc mTrigDesc;
	private TriggerActionDesc mActDesc;
	private String[] mDays;
	private boolean[] mRepeatStatus;
	private boolean mAdminMode = false;
	private AlertDialog mRepeatDialog = null;
	private String[] mActions;
	private boolean[] mActSelected = null;

	public interface ExitListener {
		
		public void onDone(Context context, int trigId, String trigDesc, String actDesc);
	}
	
	private static ExitListener mExitListener = null;
	private int mTrigId = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		addPreferencesFromResource(R.xml.trig_random_time_edit_preferences);
		setContentView(R.layout.trigger_editor);
		
		mTrigDesc = new RandomTimeTrigDesc();
		mActDesc = new TriggerActionDesc();
		
		if(getIntent().hasExtra(TriggerListActivity.KEY_ACTIONS)) {
			mActions = getIntent().getStringArrayExtra(TriggerListActivity.KEY_ACTIONS);
		}
		else {
			Log.e(DEBUG_TAG, "TimeTrigEditActivity: Invoked with out passing surveys");
			finish();
			return;
		}
		
		PreferenceScreen screen = getPreferenceScreen();
		int prefCount = screen.getPreferenceCount();
		for(int i = 0; i < prefCount; i++) {
			screen.getPreference(i).setOnPreferenceClickListener(this);
			screen.getPreference(i).setOnPreferenceChangeListener(this);
		}
		
		((Button) findViewById(R.id.trig_edit_done)).setOnClickListener(this);
		((Button) findViewById(R.id.trig_edit_cancel)).setOnClickListener(this);
		
		String config = null;
		String action = null;
		
		if(savedInstanceState != null) {
			config = savedInstanceState.getString(KEY_TRIG_DESC);
			action = savedInstanceState.getString(KEY_ACT_DESC);
		} 
		else {
			config = getIntent().getStringExtra(KEY_TRIG_DESC);
			action = getIntent().getStringExtra(KEY_ACT_DESC);
		}
		
		mAdminMode = getIntent().getBooleanExtra(KEY_ADMIN_MODE, false);
				
		if(config != null) {	
			mTrigId = getIntent().getIntExtra(KEY_TRIG_ID, 0);
			
			if(mTrigDesc.loadString(config) && mActDesc.loadString(action)) {
				initializeGUI();
			}
			else {
				getPreferenceScreen().setEnabled(false);
				Toast.makeText(this, R.string.trigger_invalid_settings,
								Toast.LENGTH_SHORT).show();
			}
		}
		
		if(savedInstanceState == null) {
			LinkedHashMap<String, Boolean> repeatList = mTrigDesc.getRepeat();	
			mDays = repeatList.keySet()
			   					.toArray(new String[repeatList.size()]);
			mRepeatStatus = new boolean[mDays.length];
			updateRepeatStatusArray();
		}
		else {
			mDays = savedInstanceState.getStringArray(KEY_SAVE_DAYS);
			mRepeatStatus = savedInstanceState.getBooleanArray(KEY_SAVE_REPEAT_STATUS);
		}
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		udateTriggerDesc();
		outState.putString(KEY_TRIG_DESC, mTrigDesc.toString());
		outState.putString(KEY_ACT_DESC, mActDesc.toString());
		outState.putStringArray(KEY_SAVE_DAYS, mDays);
		outState.putBooleanArray(KEY_SAVE_REPEAT_STATUS, mRepeatStatus);
	}
	
	public static void setOnExitListener(ExitListener listener) {
		
		mExitListener = listener;
	}
	
	@SuppressWarnings("unused")
	private void initializeGUI() {
		TimePickerPreference startPref = (TimePickerPreference) getPreferenceScreen()
							.findPreference(PREF_KEY_START_TIME);
		TimePickerPreference endPref = (TimePickerPreference) getPreferenceScreen()
							.findPreference(PREF_KEY_END_TIME);
		
		startPref.setTime(mTrigDesc.getRangeStart());
		endPref.setTime(mTrigDesc.getRangeEnd());
		
		updateRepeatPrefStatus();
		updateActionsPrefStatus();

		Preference repeatPref = getPreferenceScreen()
								.findPreference(PREF_KEY_REPEAT_DAYS);
		
		if(!mAdminMode && !TrigUserConfig.editTimeTriggerRepeat) {
			
			repeatPref.setEnabled(false);
		}
		
		Preference actionsPref = getPreferenceScreen().findPreference(PREF_KEY_ACTIONS);

		if(!mAdminMode && !TrigUserConfig.editTriggerActions) {
		
			actionsPref.setEnabled(false);
		}
		
		((Button) findViewById(R.id.trig_edit_done))
					.setEnabled(mAdminMode || TrigUserConfig.editTimeTrigger);
	}

	@SuppressWarnings("unused")
	private void udateTriggerDesc() {
		TimePickerPreference startPref = (TimePickerPreference) getPreferenceScreen()
				.findPreference(PREF_KEY_START_TIME);
		TimePickerPreference endPref = (TimePickerPreference) getPreferenceScreen()
				.findPreference(PREF_KEY_END_TIME);
		mTrigDesc.setRangeStart(startPref.getTime());
		mTrigDesc.setRangeEnd(endPref.getTime());
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		
		case R.id.trig_edit_done:
			if(mExitListener != null) {
				udateTriggerDesc();

				if(!mTrigDesc.validate()) {
					// if the time settings are invalid, tell the user and abort
					showDialog(DIALOG_ID_INVALID_TIME_ALERT);
					return;
				}
				else if (mActDesc.getSurveys().length <= 0) {
					// if no surveys were selected, tell the user and abort
					showDialog(DIALOG_ID_NO_SURVEYS_SELECTED);
					return;
				}
				
				// since we didn't hit any issues, we must be good; invoke the exit handler which stores the trigger
				mExitListener.onDone(this, mTrigId, mTrigDesc.toString(), mActDesc.toString());
			}
			break;
		}
		
		finish();
	}
	
	private void updateRepeatPrefStatus() {
		Preference repeatPref = getPreferenceScreen()
		 						  	.findPreference(PREF_KEY_REPEAT_DAYS);
		repeatPref.setSummary(mTrigDesc.getRepeatDescription());
	}
	
	private void updateRepeatStatusArray() {
		LinkedHashMap<String, Boolean> repeatList = mTrigDesc.getRepeat();
		
		for(int i = 0; i < mDays.length; i++) {
			mRepeatStatus[i] = repeatList.get(mDays[i]);
		}
	}
	
	private void updateActionsPrefStatus() {
		Preference actionsPref = getPreferenceScreen()
		 						  	.findPreference(PREF_KEY_ACTIONS);
		if (mActDesc.getSurveys().length > 0) {
			actionsPref.setSummary(stringArrayToString(mActDesc.getSurveys()));
		} else {
			actionsPref.setSummary(R.string.trigger_no_actions);
		}
		
	}
	
	private String stringArrayToString(String [] strings) {
		if (strings.length == 0) {
			return "";
		}
		String string = "";
		for (String s : strings) {
			string = string.concat(s).concat(", ");
		}
		return string.substring(0, string.length() - 2);
	}
	
	private Dialog createRepeatSelDialog() {
		
		updateRepeatStatusArray();
		mRepeatDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.trigger_time_select_days)
					.setPositiveButton(R.string.done, this)
					.setNegativeButton(R.string.cancel, this)
					.setMultiChoiceItems(mDays, mRepeatStatus, this)
					.create();
		
		return mRepeatDialog;
	}
	
	private Dialog createInvalidTimeAlert() {
		return new AlertDialog.Builder(this)
					.setTitle(R.string.trigger_time_invalid_settings)
					.setNegativeButton(R.string.cancel, null)
					.setMessage(R.string.trigger_time_invalid_text)
					.create();
	}
	
	private Dialog createNoSurveysSelectedAlert() {
		return new AlertDialog.Builder(this)
					.setTitle(R.string.trigger_time_no_survey_selected)
					.setNegativeButton(R.string.cancel, null)
					.setMessage(R.string.trigger_time_no_survey_selected_text)
					.create();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_ID_REPEAT_SEL:
			return createRepeatSelDialog();
		case DIALOG_ID_INVALID_TIME_ALERT:
			return createInvalidTimeAlert();
		case DIALOG_ID_NO_SURVEYS_SELECTED:
			return createNoSurveysSelectedAlert();
		case DIALOG_ID_ACTION_SEL:
			return createEditActionDialog();
		}
		
		return null;
	}
	
	@Override
	public boolean onPreferenceClick(Preference pref) {
		
		if(pref.getKey().equals(PREF_KEY_REPEAT_DAYS)) {
			removeDialog(DIALOG_ID_REPEAT_SEL);
			showDialog(DIALOG_ID_REPEAT_SEL);
		} else if(pref.getKey().equals(PREF_KEY_ACTIONS)) {
			removeDialog(DIALOG_ID_ACTION_SEL);
			showDialog(DIALOG_ID_ACTION_SEL);
		} 
		
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
	
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		
		mRepeatStatus[which] = isChecked;
		
		int repeatCount = 0;
		for(int i = 0; i < mRepeatStatus.length; i++) {
			if(mRepeatStatus[i]) {
				repeatCount++;
			}
		}
		
		if(mRepeatDialog != null) {
			if(mRepeatDialog.isShowing()) {
				mRepeatDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							 .setEnabled(repeatCount != 0);
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		if(which == DialogInterface.BUTTON_POSITIVE) {
			
			for(int i = 0; i < mDays.length; i++) {
				mTrigDesc.setRepeatStatus(mDays[i], mRepeatStatus[i]);
			}
			
		}
		
		dialog.dismiss();
		updateRepeatPrefStatus();
	}
	
	private Dialog createEditActionDialog() {
		
		if(mActSelected == null) {
			mActSelected = new boolean[mActions.length];
			for(int i = 0; i < mActSelected.length; i++) {
				mActSelected[i] = mActDesc.hasSurvey(mActions[i]);
			}
		}
		
		AlertDialog.Builder builder = 
	 			new AlertDialog.Builder(this)
			   .setTitle(R.string.trigger_select_actions)
			   .setNegativeButton(R.string.cancel, null)
			   .setMultiChoiceItems(mActions, mActSelected, 
					   new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, 
										boolean isChecked) {
					
					mActSelected[which] = isChecked;
				}
			});

		if(mAdminMode || TrigUserConfig.editTriggerActions) {
			 builder.setPositiveButton(R.string.done,
					 new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mActDesc.clearAllSurveys();
					
					for(int i = 0; i < mActSelected.length; i++) {
						if(mActSelected[i]) {
							mActDesc.addSurvey(mActions[i]);
						}
					}
					dialog.dismiss();
					updateActionsPrefStatus();
//					handleActionSelection(mDialogTrigId, desc);
				}
			});
		}

	
		return builder.create();
	}
	
/*	public void handleActionSelection(int trigId, TriggerActionDesc desc) {

		String prevActDesc = mDb.getActionDescription(trigId);
		TriggerActionDesc prevDesc = new TriggerActionDesc();
		prevDesc.loadString(prevActDesc);
		
		mDb.updateActionDescription(trigId, desc.toString());
		mCursor.requery();
		
		Notifier.refreshNotification(this, mCampaignUrn, true);
		
		if(desc.getCount() == 0 && prevDesc.getCount() !=0) {
			toggleTrigger(trigId, false);
		}
		
		if(desc.getCount() != 0 && prevDesc.getCount() == 0) {
			toggleTrigger(trigId, true);
		}
		
	}*/
}
	
