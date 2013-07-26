package com.kaja.openmbta;

import org.json.JSONArray;
import org.json.JSONObject;

 
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;

public class JsonParser extends Activity {
	
	private TextView tv;
	private JSONObject jObject;
	private String jString = "{\"menu\": {\"id\": \"file\", \"value\": \"File\", \"popup\": { \"menuitem\": [ {\"value\": \"New\",   \"onclick\": \"CreateNewDoc()\"}, {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},	{\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}]}}}";
    private String result;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void parse() throws Exception {
		
		setContentView(R.layout.textview);
        tv = (TextView)findViewById(R.id.myTV);

		jObject = new JSONObject(jString);

		JSONObject menuObject = jObject.getJSONObject("menu");
		String attributeId = menuObject.getString("id");
		System.out.println(attributeId);
		result = "AttrId: " + attributeId + "\n";
		//tv.setText("AttrId:" + attributeId);
		
		String attributeValue = menuObject.getString("value");
		System.out.println(attributeValue);
		result = result +  "AttrVal:" + attributeValue + "\n";

		JSONObject popupObject = menuObject.getJSONObject("popup");
		JSONArray menuitemArray = popupObject.getJSONArray("menuitem");

		for (int i = 0; i < 3; i++) {
			System.out.println(menuitemArray.getJSONObject(i)
					.getString("value").toString());
			result = result +  "value: " + menuitemArray.getJSONObject(i).getString("value").toString() + "\n";
			System.out.println(menuitemArray.getJSONObject(i).getString(
					"onclick").toString());
			result = result +  "Onclick: " + menuitemArray.getJSONObject(i).getString("onclick").toString() + "\n";		}
		
		tv.setText(result);
	}
}