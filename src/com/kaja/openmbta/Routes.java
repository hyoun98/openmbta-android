package com.kaja.openmbta;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.adwhirl.util.AdWhirlUtil;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URI;
import java.io.UnsupportedEncodingException;

 
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.adapters.AdWhirlAdapter;
import com.adwhirl.util.AdWhirlUtil;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;

import java.util.ArrayList;




import android.util.Log;

public class Routes extends ListActivity implements Runnable, AdWhirlInterface {
	
	
	private MergeAdapter mAdapter=null;
	private String transType; 
	private ArrayList<transItem> schedItems;
	private ArrayList<transItem> fullList;
	private ListView[] myListView;
	
	static final private int TRANS_DIALOG = 1;


	private JSONObject jObject;
	private String _transType="";
    private ProgressDialog pd;
	
	private String result;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fullList = new ArrayList<transItem>();
		
		//get the extras
		Bundle extras = getIntent().getExtras();
		if (extras !=null){
			transType = extras.getString("transtype");
		}
	
		
		try {
			
			startThread();
			//init();
			//parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		

	}
	
	public void onListItemClick(ListView parent, View v,
			int _index, long id) {
    	Log.d("test", String.valueOf(_index));
            
    			Intent i = new Intent(Routes.this, ScheduleTab.class); 
            	i.putExtra ("route", fullList.get(_index).getRoute());
            	i.putExtra ("headsign", fullList.get(_index).getName());
            	i.putExtra ("transtype", _transType);
            	
            	Routes.this.startActivity(i);
            	
            
            
     }
	private void startThread(){
		//This method gets called to load up the data in the background.  It will put up a busy screen. 
		 pd = ProgressDialog.show(this, "One moment please...", "Retrieving route information", true,
                 false);

		 Thread thread = new Thread(this);
		 thread.start();
	} 
	 public void run() {
		 try {
				// Call Parse to get the data and load up the data to the adapter
				init();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
         handler.sendEmptyMessage(0);
 }
	
	  private Handler handler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
                  pd.dismiss();
                  
                  try {
      				loadRoutes();
      				initAdWhirl();
      				
      			} catch (Exception e) {
      				e.printStackTrace();
      			}
      			
          }
	  };
	
	private void init() throws Exception {
		   String serverN = getString(R.string.server_name);
		   String path = "";
		   
		   
		   
		   String encodedurl = URLEncoder.encode("Commuter Rail","UTF-8");
		   
		   if (transType.equals("bus")){  _transType = "Bus";}
		   else if(transType.equals("subway")){  _transType = "Subway";}
		   else if(transType.equals("train")){  _transType = "Commuter Rail";}
		   else if(transType.equals("boat")){  _transType = "Boat";}
		   else {_transType = "Boat";}
		   
		   
		   int devMode = Integer.parseInt(getString(R.string.dev_mode));
		   setTitle(_transType + " routes");
		    
		   if  (devMode == 1){
			   //build the string
			   path = "/routes/" + _transType + ".json";
			   serverN = getString(R.string.dev_server_name);
		   }
		   else{
			   //production mode
			   path = "/routes/" + _transType + ".json";
		   }
		   
		   String uriDevice = "?device=" + getString(R.string.device_type);
		   String uriString = serverN + path + uriDevice;
		   URI uri = new URI(uriString.replace(" ", "%20"));
		  
       	
		   jObject = getJSONfromURL(uri.toString());
		 
		 
	}
	private void loadRoutes() throws Exception {
		String routeDetails = "";

        int a=0;
        
        setContentView(R.layout.main);
		mAdapter = new MergeAdapter();
	
		try{
		//JSONObject dataObject = jObject.getJSONObject("data");
		JSONArray dataArray = jObject.getJSONArray("data");
		
	//	JSONArray fieldArray = dataArray.getJSONArray
		
		String routeName;
		int size = dataArray.length();
		myListView = new ListView[size];
		
		for (a = 0; a < size; a++) {
			routeName = "";
			routeDetails = "";
			
			routeName = dataArray.optJSONObject(a).getString("route_short_name").toString();
		    routeDetails = routeDetails + "routeName: " + routeName;
		    mAdapter.addView(buildLabel(routeName));
			JSONArray hsArray = dataArray.getJSONObject(a).getJSONArray("headsigns");
			
			int resID = R.layout.routes_row;
			myListView[a] = (ListView)findViewById(android.R.id.list);
			
			schedItems = new ArrayList<transItem>();
			transAdapter aa = new transAdapter(this, resID, schedItems);
			myListView[a].setAdapter(aa);
			
			for (int i = 0; i < 2; i++) {
				String headsign ="";
				String remTrips = "";
				String type = "";
				
				if (hsArray.optJSONArray(i) != null) {
							
							headsign = hsArray.getJSONArray(i).optString(0);
							remTrips = hsArray.getJSONArray(i).optString(1) + " trips remaining today";
							if (hsArray.getJSONArray(i).optString(2) != ""){
								//type = "y" + transType;
								// above is a future feature to provide indication that info is real time by showing a colored icon
								type = transType;
							}
							else{
								//type = "bus";
								type = transType;
							}
				
					transItem tObject = new transItem(routeName, headsign, remTrips, type);
					fullList.add(tObject);
					aa.add(tObject);	
					aa.notifyDataSetChanged();
				}
			}
			
			mAdapter.addAdapter(aa);
			
		
			
		}
		setListAdapter(mAdapter);
		}
			
		catch (JSONException je)
		{
			Log.e("JSONException", "Error::" + je.toString() + "a=" + Integer.toString(a));
		}
		
	}
	
	public static JSONObject getJSONfromURL(String url){
		    //initialize
		
		    InputStream is = null;
		    String result = "";
		    JSONObject jArray = null;
			 
		    //http post
		try{
		        HttpClient httpclient = new DefaultHttpClient();
		        HttpPost httppost = new HttpPost(url);
		        HttpResponse response = httpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        is = entity.getContent();
		
		    }catch(Exception e){
		        Log.e("log_tag", "Error in http connection "+e.toString());
		    }
		    //convert response to string
		    try{
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		        StringBuilder sb = new StringBuilder();
		        String line = null;
		        while ((line = reader.readLine()) != null) {
		            sb.append(line + "\n");
		        }
		        is.close();
		        result=sb.toString();
		    }catch(Exception e){
		        Log.e("log_tag", "Error converting result "+e.toString());
		    }
		 
		    //try parse the string to a JSON object
		    try{
		            jArray = new JSONObject(result);
		    }catch(JSONException e){
		        Log.e("log_tag", "Error parsing data "+e.toString());
		    }
		 
		    return jArray;
		}

	
	public String getSchedule()
    {
    	String returnString="";
 
    	try {
			URL openMBTA = new URL(
					"http://openmbta.org/routes/Bus.json");
			URLConnection tc = openMBTA.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					tc.getInputStream()));
 
			String line;
			while ((line = in.readLine()) != null) {
			
				returnString = returnString + line;
				}
    	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return returnString;
    }
	
	private View buildLabel(String labelText) {
		TextView result=new TextView(this);
		result.setTextColor( 0xFFFFFFFF );
		result.setBackgroundColor(0xFF6E6E6E);
		//result.setTextColor(getResources().getColor(R.color.main_paper));
		result.setTextSize(2,14);
		
		//TextView result = (TextView)findViewById(R.id.label);
		result.setText("  " + labelText);
		transItem tObject = new transItem(labelText, labelText, "", "");
		fullList.add(tObject);
		
		return(result);
	}
	
	 public void initAdWhirl() {

	  	   setContentView(R.layout.main);
	  	   AdWhirlManager.setConfigExpireTimeout(1000 * 60 * 5);
	  	
	  	   AdWhirlLayout adWhirlLayout = (AdWhirlLayout)findViewById(R.id.adwhirl_layout);

	  	   TextView textView = new TextView(this);
	  	   RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	  	   int diWidth = 320;
	  	   int diHeight = 52;
	  	   int density = (int) getResources().getDisplayMetrics().density;
	  	   try{
	  		   
	  	   adWhirlLayout.setAdWhirlInterface(this);
	  	   adWhirlLayout.setMaxWidth((int)(diWidth * density));
	  	   adWhirlLayout.setMaxHeight((int)(diHeight * density));
	  	   
	  	   layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	  	   textView.setText("Below AdWhirlLayout");

	  	  // LinearLayout layout = (LinearLayout)findViewById(R.id.layout_main);
	  	 RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout_main);
	  	   
	  	   if (layout == null) {
	           Log.e("AdWhirl", "Layout is null!");
	           return;
	         }

 
	  	   //layout.setGravity(Gravity.CENTER_HORIZONTAL);
	  	   //layout.addView(adWhirlLayout, layoutParams);
	  	  // layout.addView(textView, layoutParams);
	  	   layout.invalidate();
	  	   }
	  	   catch (Exception e){
	  		   Log.e("ADWHIRL", "Error in code:" + e.toString());
	     		e.printStackTrace();
	  	   }
	  
	  	   AdWhirlAdapter.setGoogleAdSenseAppName("OpenMBTA");
	  	   AdWhirlAdapter.setGoogleAdSenseCompanyName("Kaja Software");
	  	  // AdWhirlAdapter.setGoogleAdSenseChannel("xxxxxxx");
	  	  // AdWhirlTargeting.setKeywords("My keywords");
	 
	       
	    }
	    
	 public void adWhirlGeneric() {
		    Log.e(AdWhirlUtil.ADWHIRL, "In adWhirlGeneric()");
		  }

	
}