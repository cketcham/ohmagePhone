package edu.ucla.cens.andwellness.prompts.singlechoicecustom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.andwellness.R;
import edu.ucla.cens.andwellness.Utilities;
import edu.ucla.cens.andwellness.R.layout;
import edu.ucla.cens.andwellness.Utilities.KVLTriplet;
import edu.ucla.cens.andwellness.Utilities.KVPair;
import edu.ucla.cens.andwellness.prompts.AbstractPrompt;

import android.content.Context;
import android.content.Intent;
//import android.util.Log;
import edu.ucla.cens.systemlog.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class SingleChoiceCustomPrompt extends AbstractPrompt {
	
	private static final String TAG = "SingleChoiceCustomPrompt";
	
	private List<KVLTriplet> mChoices;
	//private String mSelectedKey;
	private int mSelectedIndex;
	
	public SingleChoiceCustomPrompt() {
		super();
		mSelectedIndex = -1;
	}
	
	public void setChoices(List<KVLTriplet> choices) {
		mChoices = choices;
	}
	
	/*public SingleChoicePrompt( 	String id, String displayType, String displayLabel,
								String promptText, String abbreviatedText, String explanationText,
								String defaultValue, String condition, 
								String skippable, String skipLabel,
								List<KVPair> properties) {
		
		super(id, displayType, displayLabel, promptText, abbreviatedText, explanationText, defaultValue, condition, skippable, skipLabel);
		
		mKVPairs = properties;
		//mSelectedKey = "RESPONSE_SKIPPED";
		mSelectedIndex = -1;
	}*/

	@Override
	protected Object getTypeSpecificResponseObject() {
		if (mSelectedIndex >= 0 && mSelectedIndex < mChoices.size()) {
			return Integer.decode(mChoices.get(mSelectedIndex).key);
		} else {
			return null;
		}
	}
	
	@Override
	protected void clearTypeSpecificResponseData() {
		mSelectedIndex = -1;
	}
	
	@Override
	protected Object getTypeSpecificExtrasObject() {
		JSONArray jsonArray = new JSONArray();
		for (KVLTriplet choice : mChoices) {
			JSONObject jsonChoice = new JSONObject();
			try {
				jsonChoice.put("choice_id", Integer.decode(choice.key));
				jsonChoice.put("choice_value", choice.label);
			} catch (NumberFormatException e) {
				Log.e(TAG, "NumberFormatException when trying to parse custom choice key", e);
				return null;
			} catch (JSONException e) {
				Log.e(TAG, "JSONException when trying to generate custom_choices json", e);
				return null;
			}
			jsonArray.put(jsonChoice);
		}
		return jsonArray;
	}
	
	/*@Override
	public String getResponseValue() {
		//return mSelectedKey;
		String value = super.getResponseValue();
		if (value != null) {
			return value;
		} else if (mSelectedIndex >= 0 && mSelectedIndex < mChoices.size()) {
			return mChoices.get(mSelectedIndex).key;
		} else {
			return "WTF";
		}
		
		if (!isDisplayed()) {
			return "NOT_DISPLAYED";
		} else if (isSkipped()) {
			return "SKIPPED";
		} else if (mSelectedIndex >= 0 && mSelectedIndex < mChoices.size()) {
			return mChoices.get(mSelectedIndex).key;
		} else {
			return "WTF";
		}
	}*/

	@Override
	public View getView(Context context) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListView listView = (ListView) inflater.inflate(R.layout.prompt_single_choice, null);
		
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		String [] from = new String [] {"value"};
		int [] to = new int [] {android.R.id.text1};
		
		List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < mChoices.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("key", mChoices.get(i).key);
			map.put("value", mChoices.get(i).label);
			data.add(map);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(context, data, R.layout.single_choice_list_item, from, to);
		
		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				((CheckedTextView) view).setText((String) data);
				return true;
			}
		});
		
		listView.setAdapter(adapter);
		
		if (mSelectedIndex >= 0 && mSelectedIndex < listView.getCount()) {
			listView.setItemChecked(mSelectedIndex, true);
		}
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				//Map<String, String> map = (HashMap<String, String>) parent.getItemAtPosition(position);
				//mSelectedKey = map.get("key");
				mSelectedIndex = position;
				//((SurveyActivity)context).setResponse(index, id, value)
			}
		});
		
		return listView;
	}

	@Override
	public void handleActivityResult(Context context, int requestCode,
			int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}
}