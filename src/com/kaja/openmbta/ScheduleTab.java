package com.kaja.openmbta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.adapters.AdWhirlAdapter;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.util.AdWhirlUtil;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.GeoPoint;

import com.google.android.maps.OverlayItem;



public class ScheduleTab extends MapActivity implements Runnable, AdWhirlInterface{

	  MapController mapController;
	  KajaItemizedOverlay itemizedoverlay;
	  KajaItemizedOverlay itemizedoverlayImminent;
	  KajaItemizedOverlay itemizedoverlayFirstStops;
	  
	  private MyLocationOverlay myLocOverlay;
	  private MapView myMapView;
	  List<Overlay> mapOverlays;
	  private Location lastKnownLocation;
	  
	private ListView myListView;
	private ListView schedListView;

	private ScheduleAdapter aa;
	private ScheduleAdapter aa2;
	private ScheduleItem schedItem;
	
	public JSONObject jObject;
	private String ext_routeName;
	private String ext_headsign; 
	private String ext_transType;
	private String type_icon;
	private String closestName;
	private int closestSubIndex = -1;
	private int closestIndex = -1;
	private int closestStopId;
	private int closestOverlayId = -1;
	private Boolean MARKED_CLOSEST_STOP = false;
	private Boolean FIRST_STOP_EXISTS = false;
	private int[] firstStopIdArray;
	private int firstStopIndex=0;
	private final int OVERLAY_DEFAULT = 0;
	private final int OVERLAY_IMMINENT = 1;
	private final int OVERLAY_FIRSTSTOPS = 2;
	
	
	private int grid_width =0;
	private int schIndex = 0;
	private int totalGridWidth  = 0;
	private Boolean FIRSTTIME=true;
	private HashMap<String, String> bookmarkHash;
	private String bmFileName;
	private hashFile hf;
    private TabHost tabHost; 
	private ProgressDialog pd;
	private Resources res;
	
	private Thread thread;
	private boolean bLoading = false;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tab_main_single);
	    tabHost = (TabHost)findViewById(android.R.id.tabhost);
	    tabHost.setup();
    
		bookmarkHash = new HashMap<String, String>();
   	 	bmFileName = getString(R.string.bookmark_file);

		hf = new hashFile();
	
	    if (hf.doLoad(bmFileName, this) != null ) {
	    	bookmarkHash = hf.doLoad(bmFileName, this); 
	    }
		 		
	    //get info passed in from calling activity to build URI query.
		Bundle extras = getIntent().getExtras();
		if (extras !=null){
			ext_routeName = extras.getString("route");
			ext_headsign = extras.getString("headsign");
			ext_transType = extras.getString("transtype");
		}
			
		myListView = (ListView)findViewById(R.id.myList);
		schedListView = (ListView)findViewById(R.id.scheduleList);
	    
	
	   
	  res = getResources(); // Resource object to get Drawables
	
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
	    
	    
	    setupTab(R.id.myList, R.drawable.ic_tab_realtime, "Next Arrivals");
	    setupTab(R.id.mapLayout, R.drawable.ic_tab_map, "Map");
	    setupTab(R.id.scheduleLayout, R.drawable.ic_tab_schedule, "Schedule");
	    
	    tabHost.setCurrentTab(1);
	    
	    try {
			// Call Parse to get the data and load up the data to the adapter
			startThread(); // calls init in the background
			
			//loadSchedule();
		} catch (Exception e) {
			e.printStackTrace();
		}
 
	}

	private void refresh(){
			startActivity(getIntent()); 
			finish();
	}
	
	private void setupTab(final int tView, final int iView,  final String tag) {
	    View tabview = createTabView(tabHost.getContext(), iView, tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(tView);
	    tabHost.addTab(setContent);
	}
	
	
	
	private  View createTabView(final Context context, final int imgView, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		
		ImageView iv = (ImageView) view.findViewById(R.id.tabsIcon);
		iv.setImageDrawable(res.getDrawable(imgView));
		
		return view;
		}

	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void startThread(){
		//This method gets called to load up the data in the background.  It will put up a busy screen. 
		bLoading = true;
		 pd = ProgressDialog.show(this, "One moment please...", "Retrieving schedule information", true,
                 false);
		 
		  thread = new Thread(this);
		 thread.start();
	} 
	 public void run() {
		 try {
				// Call Parse to get the data and load up the data to the adapter
				init();
				//loadSchedule();
			} catch (Exception e) {
				e.printStackTrace();
			}
         handler.sendEmptyMessage(0);
 }
	
	  private Handler handler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
                  pd.dismiss();
                  //tv.setText(pi_string);
                  try { 
      				// 
                	 
      				loadNextArrivals();
      				loadSchedule();
      				initMap(); 
      				
      				  				
      				String locationProvider = LocationManager.NETWORK_PROVIDER;
      			// Or use LocationManager.GPS_PROVIDER
      				LocationManager locationManager = (LocationManager) ScheduleTab.this.getSystemService(Context.LOCATION_SERVICE);
      				lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
      				closestStopId = findClosestStop();
      				//closestStopId = 669;  //for debug purposes
      				loadMap();
      			 //loadMap also overlays the route except for imminent arrivals and first stops. 
      				
      				
      				mapOverlays.add(itemizedoverlay);
      				//only add the other overlays if there is anything to overlay.
      				
      				if (overlayImminent()){
      					 mapOverlays.add(itemizedoverlayImminent);
      				}
      				if (overlayFirstStops()){
      					mapOverlays.add(itemizedoverlayFirstStops);
      				}
      				
      				 initAdWhirl(); 
      				 // Now display the balloon over the closest stop
      				 if (MARKED_CLOSEST_STOP){
      					 		if (closestOverlayId == OVERLAY_DEFAULT){
      					 		   itemizedoverlay.displayBalloon(closestSubIndex);    
      					 		   }
      					 		else if (closestOverlayId == OVERLAY_IMMINENT) {
      					 			itemizedoverlayImminent.displayBalloon(closestSubIndex);
      					 		}
      					 		else if (closestOverlayId == OVERLAY_FIRSTSTOPS) {
      					 			itemizedoverlayFirstStops.displayBalloon(closestSubIndex);
      					 		}
     					
      					 }
      					 myListView.setSelection(closestIndex);
          				
      				 bLoading = false;
      			} catch (Exception e) {
      				e.printStackTrace();
      			}
          }
	  };
	
		private void init() throws Exception {
			
			String sURL = "";
		    String serverN = getString(R.string.server_name);
			String uriTrips = "";
		    String uriRoute = "";
		    String uriHeadSign = "";
		    String uriTrailer = ".json"; 
		    String uriDevice = "";
		    
		    
		    if (ext_transType.equals("Bus")){  type_icon = "bus";}
			   else if(ext_transType.equals("Subway")){  type_icon = "subway";}
			   else if(ext_transType.equals("Commuter Rail")){  type_icon = "train";}
			   else if(ext_transType.equals("Boat")){  type_icon = "boat";}
			   else {type_icon = "Boat";}
		    
		    int devMode = Integer.parseInt(getString(R.string.dev_mode));
		    //grid_width is the display with of the times in the UI
		    grid_width = Integer.parseInt(getString(R.string.grid_width));
			
		    uriDevice = "&device=" + getString(R.string.device_type);
		    
		    //set title text and color
		    setTitle("Schedule:  " + ext_routeName + " - " + ext_headsign);
		    if (isBookmarked()){
		    	 getWindow().setTitleColor(getResources().getColor(R.color.bmTitleColor));
		    }
		    else {
		    	 getWindow().setTitleColor(getResources().getColor(R.color.defaultTitleColor));
		    }
		    	
		    
			if (devMode == 1){
				serverN = getString(R.string.dev_server_name);
				uriTrips = "/routes/trips.";
				uriRoute = ".";
				uriHeadSign = ".";
				uriTrailer = ".json";
				ext_headsign = ext_headsign.substring(0,7);
				if (ext_routeName.length()>8){
					ext_routeName = ext_routeName.substring(0,8);
				}
			}
			else {
				uriTrips = "/trips.json?transport_type=";
				uriRoute = "&route_short_name=";
				uriHeadSign = "&headsign=";
				uriTrailer = "&first_stop=";
		
			}
			// now build the url
			sURL = serverN + uriTrips + ext_transType;
			sURL = sURL + uriRoute + ext_routeName;
			sURL = sURL + uriHeadSign + ext_headsign + uriTrailer + uriDevice;
			
			  URI uri = new URI(sURL.replace(" ", "%20"));
			jObject = getJSONfromURL(uri.toString());
	       // timeIndex = getCurrentTimeIndex();
			
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
		private void loadNextArrivals() throws Exception {
			String routeDetails = "";
		    int a=0;
	     
			try{
			JSONObject stopObject = jObject.getJSONObject("stops");
					
			// Get all the stops and put it in an array
			JSONArray stopsArray = jObject.getJSONArray("ordered_stop_ids");

			//Get Trip Number
			JSONObject stopInfoObject;
			
			int resID = R.layout.schedule_layout;
			ArrayList<ScheduleItem> schedItems = new ArrayList<ScheduleItem>();
			aa = new ScheduleAdapter(this, resID, schedItems);
			myListView.setAdapter(aa);
			myListView.setFastScrollEnabled(true);
			
			int size = stopsArray.length();
			for (a = 0; a < size; a++) {
				//routeName = "";
				routeDetails = "";
				String[] ArrivalArray;
				ArrivalArray = new String[4];
			
				stopInfoObject = stopObject.getJSONObject(Integer.toString((Integer) stopsArray.get(a)));
				String name = stopInfoObject.getString("name");
				String stopId = stopInfoObject.getString("stop_integer_id");
				String nextArrivals = (String) stopInfoObject.getJSONArray("next_arrivals").toString(); 	
				JSONArray nextArrivalArray = stopInfoObject.getJSONArray("next_arrivals");
			
				
				int isize = nextArrivalArray.length();
				if (isize > 4){isize=4;}
				
				//below for loop also handles case where the schedule has holes
				for(int i=0; i<isize; i++){
					if (nextArrivalArray.isNull(i)){
						ArrivalArray[i] = "-";
					}
					else {
						
						ArrivalArray[i] = nextArrivalArray.getJSONArray(i).get(0).toString();
					}
					
				}
				
				if (nextArrivalArray.length()<4){
					for (int j = nextArrivalArray.length(); j<4; j++){
						ArrivalArray[j] = "n/a";
					}
				}
				
				schedItem = new ScheduleItem(name, ArrivalArray, type_icon);
			
				aa.add(schedItem);
							
			}//end of for
			
				aa.notifyDataSetChanged();
			
			
			}  //end of try
			
			catch (JSONException je)
			{
				Log.e("JSONException", "Error::" + je.toString() + "a=" + Integer.toString(a));
				//e.printStackTrace();
			}
			catch (Exception e) 
			{
				Log.e("Error", "Error in code:" + e.toString());
	    		e.printStackTrace();
			
			}
		}

		private int getCurrentTimeIndex(){
			int index = 0;
			// this routine gets called near the start of the activity to get the index location of the next schedule
			
			try {
				//get the grid array
				JSONArray gridArray = jObject.getJSONArray("grid");
				JSONArray timesArray = gridArray.getJSONObject(0).getJSONArray("times");
				
				
				int i = -1;
				while (i < 0)
				{
					i = Integer.parseInt( timesArray.getJSONArray(index).get(1).toString());
					index++;
				}
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			return index;
		}
		
		private void loadSchedule() throws Exception {
			String routeDetails = "";
		    int a=0;
	     
	        //String serverN = getString(R.string.server_name) + "/routes/trips.bus.1.Inbound.json";
	        //jObject = getJSONfromURL(serverN); 
			if (FIRSTTIME){
				schIndex = getCurrentTimeIndex();
				FIRSTTIME=false;
			}
			
			try{
			JSONObject stopObject = jObject.getJSONObject("stops");
			JSONArray gridArray = jObject.getJSONArray("grid");
					
			int resID = R.layout.schedule_layout;
			ArrayList<ScheduleItem> schedItems = new ArrayList<ScheduleItem>();
			aa2 = new ScheduleAdapter(this, resID, schedItems);
			schedListView.setAdapter(aa2);
			schedListView.setFastScrollEnabled(true);
			int isize = 0;
			int size = gridArray.length();
			for (a = 0; a < size; a++) {
			
				routeDetails = "";
				String[] ArrivalArray;
				ArrivalArray = new String[grid_width];
							
				//get the stop info
				JSONArray timesArray = gridArray.getJSONObject(a).getJSONArray("times");
				String name = gridArray.getJSONObject(a).getJSONObject("stop").getString("name");
				String stopId = gridArray.getJSONObject(a).getJSONObject("stop").getString("stop_id");
				
				isize = timesArray.length();
				totalGridWidth = isize;
				
				//checking to see if there's enough info to display
				if (isize - schIndex > grid_width){
					isize=grid_width;
					}			
				else{
					isize = isize - schIndex;
				}
				if (timesArray.length()- schIndex <grid_width){
					for (int j = timesArray.length()- schIndex; j<grid_width; j++){
						ArrivalArray[j] = "n/a";
					}
				}
				
				//below for loop also handles case where the schedule has holes
				for(int i=0; i<isize; i++){
					if (timesArray.isNull(i+schIndex)){
						ArrivalArray[i] = "-";
					}
					else {
						
						ArrivalArray[i] = timesArray.getJSONArray(i+schIndex).get(0).toString();
					}
					
				}
								
				schedItem = new ScheduleItem(name, ArrivalArray, type_icon);
				aa2.add(schedItem);
							
			}//end of for
			
				aa2.notifyDataSetChanged();
			
				
			}  //end of try
			
			catch (JSONException je)
			{
				Log.e("JSONException", "Error::" + je.toString() + "a=" + Integer.toString(a));
				//e.printStackTrace();
			}
			catch (Exception e) 
			{
				Log.e("Error", "Error in code:" + e.toString());
	    		e.printStackTrace();
			
			}
			//schIndex = schIndex + grid_width ;
			
			
		}
		
		private void initMap(){
			   myMapView = (MapView) findViewById(R.id.myMapView);
			    
			    myMapView.setSatellite(false);
			   // myMapView.setStreetView(true);
			    myMapView.setBuiltInZoomControls(true);
			    myMapView.displayZoomControls(true);
			    
			    mapController = myMapView.getController();
		// Now let's get our location
			    LocationManager locationManager;
			    String context = Context.LOCATION_SERVICE;
			    locationManager = (LocationManager)getSystemService(context);

			    Criteria criteria = new Criteria();
			    criteria.setAccuracy(Criteria.ACCURACY_FINE);
			    criteria.setAltitudeRequired(false);
			    criteria.setBearingRequired(false);
			    criteria.setCostAllowed(true);
			    criteria.setPowerRequirement(Criteria.POWER_LOW);
			    String provider = locationManager.getBestProvider(criteria, true);

			    initMyLocation();
			    
		
			   mapOverlays = myMapView.getOverlays();
			   Drawable drawable = this.getResources().getDrawable(R.drawable.redpin);
				  Drawable drawableMe = this.getResources().getDrawable(R.drawable.purplepin);
				  Drawable drawableFirst = this.getResources().getDrawable(R.drawable.greenpin);
				  
				  itemizedoverlay = new KajaItemizedOverlay(drawable, myMapView);
				  itemizedoverlayImminent = new KajaItemizedOverlay(drawableMe, myMapView);
				  itemizedoverlayFirstStops = new KajaItemizedOverlay(drawableFirst, myMapView);
				  
				  
				  itemizedoverlay.setBalloonBottomOffset(17);
				  itemizedoverlayImminent.setBalloonBottomOffset(17);
				  itemizedoverlayImminent.setBalloonBottomOffset(17);
						  
			}
		private void initMyLocation() {
			myLocOverlay = new MyLocationOverlay(this, myMapView);
			myLocOverlay.enableMyLocation();
			myMapView.getOverlays().add(myLocOverlay);
	 
		}
		
		private boolean isFirstStop(String stopName, int stopId_){
			// returns true if the stop name provided is a 'first-stop'
			// also creates an array of first-stop ids to be used
			boolean rValue = false;
			try {
				JSONArray firstStopArray = jObject.getJSONArray("first_stop");
				
				for (int i=0; i < firstStopArray.length(); i++){
					if (firstStopArray.getString(i).equals(stopName)){
						rValue = true;
						if (firstStopIndex < firstStopArray.length()){
							firstStopIdArray[firstStopIndex] = stopId_;
							firstStopIndex++;
						}
					}
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return rValue;
			
		}
		private boolean isImminentStop(int stopId_){
			boolean rValue = false;
			
			try {
				JSONArray imminentArray = jObject.getJSONArray("imminent_stop_ids");
				for (int i=0; i < imminentArray.length(); i++){
					if (imminentArray.getInt(i) == stopId_){
						rValue = true;
					}
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return rValue;
		}
		private void loadMap() throws Exception {
			// This routine loads the first set of overlays. Excludes imminent stops and the first stop(s)
			//
			String routeDetails = "";
		//	setContentView(R.layout.label);
	        int a=0;
			try{
			JSONObject stopObject = jObject.getJSONObject("stops");
		
			// Get all the stops and put it in an array
			JSONArray stopsArray = jObject.getJSONArray("ordered_stop_ids");
			//JSONArray immimentArray = jObject.getJSONArray("imminent_stop_ids");
			JSONArray firstStopArray = jObject.getJSONArray("first_stop");
			JSONObject regionObject = jObject.getJSONObject("region");
			
			firstStopIdArray = new int[firstStopArray.length()]; //initializing this array to be used to load the first_stops
			
			// Set the center point
			Double cLat = regionObject.getDouble("center_lat")*1E6;
			Double cLng = regionObject.getDouble("center_lng")*1E6;
			GeoPoint cpoint = new GeoPoint(cLat.intValue(),cLng.intValue());
			mapController.setCenter(cpoint);
			
			//Set the size of the map
			Double spanLat = regionObject.getDouble("lat_span")*1E6;
			Double spanLng = regionObject.getDouble("lng_span")*1E6;
			mapController.zoomToSpan((int)Math.round(spanLat), (int)Math.round(spanLng));
			JSONObject stopInfoObject;
		    
			int item_index = 0; // index for the item display.
			int size = stopsArray.length();
			for (a = 0; a < size; a++) {
			
				routeDetails = "";
			
						
				stopInfoObject = stopObject.getJSONObject(stopsArray.get(a).toString());
				String name = stopInfoObject.getString("name");
				
				int stopId = stopInfoObject.getInt("stop_integer_id");
				
				if (!(isFirstStop(name, stopId) || isImminentStop(stopId))){
					
			
				String nextArrivals = "";
				JSONArray nextArrivalArray = stopInfoObject.getJSONArray("next_arrivals");
				
				for (int b=0; b< nextArrivalArray.length(); b++) {
					if (b > 0){
						nextArrivals = nextArrivals + ", " + nextArrivalArray.getJSONArray(b).getString(0);
					}
					else {
					nextArrivals = nextArrivals + " " + nextArrivalArray.getJSONArray(b).getString(0);
				
					}
				}
				
				Double geoLat = stopInfoObject.getDouble("lat")*1E6;
				Double geoLng = stopInfoObject.getDouble("lng")*1E6;
				
				GeoPoint point = new GeoPoint(geoLat.intValue(),geoLng.intValue());
				OverlayItem overlayitem = new OverlayItem(point, nextArrivals, "  " + name);
				itemizedoverlay.addOverlay(overlayitem);
				
				if (!MARKED_CLOSEST_STOP){
					if ( stopId == closestStopId){
						closestSubIndex = item_index;
						closestOverlayId = OVERLAY_DEFAULT;
						MARKED_CLOSEST_STOP = true;
					}
				}
				
				item_index++;
				}	
										
			}//end of for
			
			}  //end of try

		
			catch (JSONException je)
			{
				Log.e("JSONException", "Error::" + je.toString() + "a=" + Integer.toString(a));
				//e.printStackTrace();
			}
		}
		
		 public void imgNext_click(View v)
		  {
		      //Toast.makeText(this, "Next...", Toast.LENGTH_LONG).show();
		      try {
		    	  if (schIndex + grid_width < totalGridWidth - grid_width){
		    		  schIndex = schIndex + grid_width;
		    		  aa2.clear();
		    	    	loadSchedule();
		    	  }  
		    	  //else we are already at the bottom of the list and cannot go any further. 
		    	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  public void imgPrev_click(View v)
		  {
		   	  
		      try {
		    	  if (schIndex > 0){ //if we are already at 0 then don't do anything
		    	  if (schIndex - grid_width >= 0){
		    		  schIndex = schIndex - grid_width;
		    	  }
		    	  else{
		    		  schIndex=0;
		    	  }
		    	  aa2.clear();
		    	  loadSchedule();
		    	  }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
		  }
		  
		  private int findClosestStop() {
			  // this routine returns the index to the closet stop based on the current location
			  Location location = new Location("");
			  
			  float LastDistance;
			  closestIndex = 0;
			  int closestStopId_ = 0;
			  float closestDistance = 0;
						  					 
			  try {
				  JSONObject stopInfoObject;
			  JSONObject stopObject = jObject.getJSONObject("stops");
			  JSONArray stopsArray = jObject.getJSONArray("ordered_stop_ids");
				
			  JSONObject regionObject = jObject.getJSONObject("region");
				
				
			     Double  kLat = lastKnownLocation.getLatitude();
				 Double kLng = lastKnownLocation.getLongitude();
			  
				Double cLat = regionObject.getDouble("center_lat")*1E6;
				Double cLng = regionObject.getDouble("center_lng")*1E6;
				float rez[] = new float[3];
				float rez2[] = new float[3];
				  location.setLatitude(cLat);
				  location.setLongitude(cLng);
				
				  int size = stopsArray.length();
				  int b = 0;
					while (b < size){
						
						stopInfoObject = stopObject.getJSONObject(stopsArray.get(b).toString());
						Double geoLat = stopInfoObject.getDouble("lat")*1E6;
						Double geoLng = stopInfoObject.getDouble("lng")*1E6;
						Location.distanceBetween(kLat, kLng, geoLat/1E6, geoLng/1E6, rez);
						
						if (b==0){
							closestDistance = rez[0];
						}
						else if (b>0){
							if (closestDistance > rez[0]){
								closestDistance = rez[0];
								closestIndex = b;
								closestStopId_  = stopInfoObject.getInt("stop_integer_id");
								closestName = stopInfoObject.getString("name");
										
							}
						}
						
						b++;
					}
					
				  
			  } 
				
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("log_tag", "Error in finding closest stop: "+e.toString());
					return closestStopId_;
				}
				catch (Exception e){
					return closestStopId_;
				}
			  
			 
			  return closestStopId_;
			  
		  }
		  
		  private void overlayCenter() {
			  
			  JSONObject stopInfoObject;
			
			  try {
			  JSONObject stopObject = jObject.getJSONObject("stops");
				JSONArray stopsArray = jObject.getJSONArray("ordered_stop_ids");
				
					JSONObject regionObject = jObject.getJSONObject("region");
				
				
				stopInfoObject = stopObject.getJSONObject(Integer.toString((Integer) stopsArray.get(4)));
				String name = stopInfoObject.getString("name");
				String stopId = stopInfoObject.getString("stop_integer_id");
				String nextArrivals = (String) stopInfoObject.getJSONArray("next_arrivals").toString(); 
				
				Double geoLat = stopInfoObject.getDouble("lat")*1E6;
				Double geoLng = stopInfoObject.getDouble("lng")*1E6;
				
				Double cLat = regionObject.getDouble("center_lat")*1E6;
				Double cLng = regionObject.getDouble("center_lng")*1E6;
				GeoPoint point = new GeoPoint(cLat.intValue(),cLng.intValue());
				
				
				
				//GeoPoint point = new GeoPoint(geoLat.intValue(),geoLng.intValue());
				OverlayItem overlayitem = new OverlayItem(point, "Center", "Here");
				itemizedoverlayImminent.addOverlay(overlayitem);
			  } 
				
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  }

		  private boolean overlayImminent() {
			// this routine overlays the imminent stops 
			  JSONObject stopInfoObject;
				boolean ret_boolean = false;
			  try {
			  JSONObject stopObject = jObject.getJSONObject("stops");
				JSONArray stopsArray = jObject.getJSONArray("ordered_stop_ids");
				JSONArray imminentArray = jObject.getJSONArray("imminent_stop_ids");
					JSONObject regionObject = jObject.getJSONObject("region");
				
				
				int item_index = 0;
				
				for (int a = 0; a < imminentArray.length(); a++) {	
					
					stopInfoObject = stopObject.getJSONObject(imminentArray.getString(a));
					
					String name = stopInfoObject.getString("name");
					int stopId = stopInfoObject.getInt("stop_integer_id");
					
					String nextArrivals = "";
				
					if (!isFirstStop(name, stopId)) {
							ret_boolean = true; // there will be at least one overlay, so return true.
						
							JSONArray nextArrivalArray = stopInfoObject.getJSONArray("next_arrivals");
						
						for (int b=0; b< nextArrivalArray.length(); b++) {
							if (b > 0){
								nextArrivals = nextArrivals + ", " + nextArrivalArray.getJSONArray(b).getString(0);
							}
							else {
							nextArrivals = nextArrivals + " " + nextArrivalArray.getJSONArray(b).getString(0);
						
							}
						}
						//String nextArrivals = (String) stopInfoObject.getJSONArray("next_arrivals").toString(); 
						
						Double geoLat = stopInfoObject.getDouble("lat")*1E6;
						Double geoLng = stopInfoObject.getDouble("lng")*1E6;
						
						Double cLat = regionObject.getDouble("center_lat")*1E6;
						Double cLng = regionObject.getDouble("center_lng")*1E6;
						//GeoPoint point = new GeoPoint(cLat.intValue(),cLng.intValue());
						
						
						
						GeoPoint point = new GeoPoint(geoLat.intValue(),geoLng.intValue());
						OverlayItem overlayitem = new OverlayItem(point, nextArrivals, "  " + name);
						itemizedoverlayImminent.addOverlay(overlayitem);
						
						if (!MARKED_CLOSEST_STOP){
							if ( stopId == closestStopId){
								closestSubIndex = item_index;
								closestOverlayId = OVERLAY_IMMINENT;
								MARKED_CLOSEST_STOP = true;
							}
						}
						
						item_index++;
					}
				}	
				
			  } 
				
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 catch (Exception e){ 
					   Log.e("OVERLAY_IMMINENT", "Error in code:" + e.toString());
			   		e.printStackTrace();
				   }
				 return ret_boolean;
		  }
		  
		  private boolean overlayFirstStops() {
				// this routine overlays the imminent stops 
				  JSONObject stopInfoObject;
				  boolean ret_boolean = false;
				if (firstStopIdArray.length > 0){ //confirm that we found first_stops before doing anything
				  try {
					  ret_boolean = true;
					  JSONObject stopObject = jObject.getJSONObject("stops");
					  JSONObject regionObject = jObject.getJSONObject("region");
					
					
					int item_index = 0;
					
					for (int a = 0; a < firstStopIdArray.length; a++) {	
						
						stopInfoObject = stopObject.getJSONObject(Integer.toString(firstStopIdArray[a]));
						
						String name = stopInfoObject.getString("name");
						
						String nextArrivals = "";
					JSONArray nextArrivalArray = stopInfoObject.getJSONArray("next_arrivals");
							for (int b=0; b< nextArrivalArray.length(); b++) {
								if (b > 0){
									nextArrivals = nextArrivals + ", " + nextArrivalArray.getJSONArray(b).getString(0);
								}
								else {
								nextArrivals = nextArrivals + " " + nextArrivalArray.getJSONArray(b).getString(0);
							
								}
							}
							
							Double geoLat = stopInfoObject.getDouble("lat")*1E6;
							Double geoLng = stopInfoObject.getDouble("lng")*1E6;
							
							Double cLat = regionObject.getDouble("center_lat")*1E6;
							Double cLng = regionObject.getDouble("center_lng")*1E6;
						
							GeoPoint point = new GeoPoint(geoLat.intValue(),geoLng.intValue());
							OverlayItem overlayitem = new OverlayItem(point, nextArrivals, "  " + name);
							itemizedoverlayFirstStops.addOverlay(overlayitem);
							
							if (!MARKED_CLOSEST_STOP){
								if ( firstStopIdArray[a]==closestStopId){
									closestSubIndex = item_index;
									closestOverlayId = OVERLAY_FIRSTSTOPS;
									MARKED_CLOSEST_STOP = true;
								}
							}
							
							item_index++;
						
					}	
				  } 
					
					catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return ret_boolean; // notifying true/false depending on if we found stops to overlay
			  }
		  private void overlayOne(int stopIndex_) {
			  
			  JSONObject stopInfoObject;
			
			  try {
			  JSONObject stopObject = jObject.getJSONObject("stops");
				JSONArray stopsArray = jObject.getJSONArray("ordered_stop_ids");
				
					JSONObject regionObject = jObject.getJSONObject("region");
				
				
				stopInfoObject = stopObject.getJSONObject(stopsArray.get(stopIndex_).toString());
				
				String name = stopInfoObject.getString("name");
				String stopId = stopInfoObject.getString("stop_integer_id");
				
				String nextArrivals = "Next arrivals: ";
				JSONArray nextArrivalArray = stopInfoObject.getJSONArray("next_arrivals");
				
				for (int b=0; b< nextArrivalArray.length() - 1; b++) {
					if (b > 0){
						nextArrivals = nextArrivals + ", " + nextArrivalArray.getJSONArray(b).getString(0);
					}
					else {
					nextArrivals = nextArrivals + " " + nextArrivalArray.getJSONArray(b).getString(0);
				
					}
				}
				
				Double geoLat = stopInfoObject.getDouble("lat")*1E6;
				Double geoLng = stopInfoObject.getDouble("lng")*1E6;
				
				Double cLat = regionObject.getDouble("center_lat")*1E6;
				Double cLng = regionObject.getDouble("center_lng")*1E6;
				
				
				
				GeoPoint point = new GeoPoint(geoLat.intValue(),geoLng.intValue());
				OverlayItem overlayitem = new OverlayItem(point, name, nextArrivals);
				itemizedoverlayFirstStops.addOverlay(overlayitem);
			  } 
				
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  }
   
 public void initAdWhirl() {

	   //setContentView(R.layout.tab_main_single);
	   AdWhirlManager.setConfigExpireTimeout(1000 * 60 * 5);
	
	   AdWhirlLayout adWhirlLayout = (AdWhirlLayout)findViewById(R.id.adwhirl_layout);

	   //TextView textView = new TextView(this);
	   //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	   int diWidth = 320;
	   int diHeight = 52;
	   int density = (int) getResources().getDisplayMetrics().density;
	   try{
		   
	   adWhirlLayout.setAdWhirlInterface(this);
	   adWhirlLayout.setMaxWidth((int)(diWidth * density));
	   adWhirlLayout.setMaxHeight((int)(diHeight * density)); 
	   
	   //layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	   //textView.setText("Below AdWhirlLayout");

	  // LinearLayout layout = (LinearLayout)findViewById(R.id.layout_main);
	 //RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout_main);
	   
	 /*  if (layout == null) {
         Log.e("AdWhirl", "Layout is null!");
         return;
       }*/


	   //layout.setGravity(Gravity.CENTER_HORIZONTAL);
	   //layout.addView(adWhirlLayout, layoutParams);
	  // layout.addView(textView, layoutParams);
	   //layout.invalidate();
	   }
	   catch (Exception e){ 
		   Log.e("ADWHIRL", "Error in code:" + e.toString());
   		e.printStackTrace();
	   }

	   AdWhirlAdapter.setGoogleAdSenseAppName("OpenMBTA");
	   AdWhirlAdapter.setGoogleAdSenseCompanyName("Kaja Software");
	  // AdWhirlAdapter.setGoogleAdSenseChannel("xxxxxxx");
	   //AdWhirlTargeting.setKeywords("business, commuters");

     
  }
  
public void adWhirlGeneric() {
	    Log.e(AdWhirlUtil.ADWHIRL, "In adWhirlGeneric()");
	  }

 
 @Override
 public boolean onCreateOptionsMenu(Menu menu) {
       return true;
 }
 
 public boolean onPrepareOptionsMenu(Menu menu) {
	
	 
	 menu.clear();
	 MenuInflater inflater = getMenuInflater();
	 if (isBookmarked()){
		//  display bookmarked menu
	     inflater.inflate(R.menu.schedule_menu_bm, menu);
	 }
	 else {
	  //not bookmarked, display normal menu
	     inflater.inflate(R.menu.schedule_menu, menu);
	 }   
	     
	     return true;
	 
	
 }
 
 @Override
 public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
     switch (item.getItemId()) {
     case R.id.bookmark:
        
    	 Toast.makeText(this, "Bookmark added", Toast.LENGTH_LONG).show();
    	 bookmarkHash.put(ext_routeName + ";" + ext_headsign, ext_transType);
    	 hf.doSave(bookmarkHash, bmFileName, this);
    	 getWindow().setTitleColor(getResources().getColor(R.color.bmTitleColor));
    	
    	 return true;
     case R.id.bookmark_del:
    	 Toast.makeText(this, "Bookmark deleted", Toast.LENGTH_LONG).show();
    	 bookmarkHash.remove(ext_routeName + ";" + ext_headsign);
    	 hf.doSave(bookmarkHash, bmFileName, this);
    	 getWindow().setTitleColor(getResources().getColor(R.color.defaultTitleColor));
    	 return true;
    case R.id.refresh:
     	refresh();
     	return true;
    case R.id.help: 
        
    
    	Intent i = new Intent(ScheduleTab.this, WebViewer.class); 
    	// pass in some data to next Activity
    	i.putExtra ("sURL", "http://openmbta.org/help/map/" + type_icon +  "?version=3");
    	i.putExtra("sTitle", "OpenMBTA Help");
		ScheduleTab.this.startActivity(i);
    	return true;
   
    	
     default:
         return super.onOptionsItemSelected(item);
     }
 }
 
 private boolean isBookmarked(){
  //checks to see if the current route/headsign(direction) is bookmarked
	 
	 if (bookmarkHash.get(ext_routeName + ";" + ext_headsign)==null){
		 return false;
	 }
	 else {
		 return true;	 
	 }
 }
 
 public void onPause() {
	 // use has left activity; disable location tracking
	 super.onPause();
	 //only disable the gps if it has full loaded.
	 if (!bLoading){
		 myLocOverlay.disableMyLocation();
		 myLocOverlay = null;
	 }
	 
 }
 
 public void onResume() {
	 super.onResume();
	 if (!FIRSTTIME){
		 initMyLocation();
	 }
 }
 
/* 
 public void onDestroy(){
	 super.onDestroy();
	 myLocOverlay.disableMyLocation();
 }*/
 
 }