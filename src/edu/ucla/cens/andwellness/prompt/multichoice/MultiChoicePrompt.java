package edu.ucla.cens.andwellness.prompt.multichoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import edu.ucla.cens.andwellness.R;
import edu.ucla.cens.andwellness.Utilities.KVLTriplet;
import edu.ucla.cens.andwellness.prompt.AbstractPrompt;

public class MultiChoicePrompt extends AbstractPrompt {
	
	private static final String TAG = "MultiChoicePrompt";
	
	private List<KVLTriplet> mChoices;
	private ArrayList<Integer> mSelectedIndexes;
	
	public MultiChoicePrompt() {
		super();
		mSelectedIndexes = new ArrayList<Integer>();
	}
	
	public void setChoices(List<KVLTriplet> choices) {
		mChoices = choices;
	}

	@Override
	protected void clearTypeSpecificResponseData() {
		if (mSelectedIndexes == null) {
			mSelectedIndexes = new ArrayList<Integer>();
		} else {
			mSelectedIndexes.clear();
		}
	}

	@Override
	protected Object getTypeSpecificResponseObject() {
		JSONArray jsonArray = new JSONArray();
		for (int index : mSelectedIndexes) {
			if (index >= 0 && index < mChoices.size())
				jsonArray.put(Integer.decode(mChoices.get(index).key));
		}
		return jsonArray;
	}
	
	@Override
	protected Object getTypeSpecificExtrasObject() {
		return null;
	}

	@Override
	public View getView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListView listView = (ListView) inflater.inflate(R.layout.prompt_multi_choice, null);
		
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		String [] from = new String [] {"value"};
		int [] to = new int [] {android.R.id.text1};
		
		List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < mChoices.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("key", mChoices.get(i).key);
			map.put("value", mChoices.get(i).label);
			data.add(map);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(context, data, R.layout.multi_choice_list_item, from, to);
		
		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				((CheckedTextView) view).setText((String) data);
				return true;
			}
		});
		
		listView.setAdapter(adapter);
		
		if (mSelectedIndexes.size() > 0) {
			for (int index : mSelectedIndexes) {
				if (index >= 0 && index < mChoices.size())
					listView.setItemChecked(index, true);
			}
		}
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				//SparseBooleanArray checkItemPositions = ((ListView)parent).getCheckedItemPositions();
				if (((ListView)parent).isItemChecked(position)) {
					mSelectedIndexes.add(Integer.valueOf(position));
				} else {
					mSelectedIndexes.remove(Integer.valueOf(position));
				}
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