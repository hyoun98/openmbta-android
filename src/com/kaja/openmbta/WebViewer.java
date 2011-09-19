package com.kaja.openmbta;

import android.webkit.WebView;
import android.app.Activity;
import android.content.Intent;


import android.os.Bundle;




import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class WebViewer extends Activity {
	
	private String ext_URL;
	private String ext_title;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.webview);
        
        ext_URL = "http://openmbta.org/mobile";
        
        Bundle extras = getIntent().getExtras();
		if (extras !=null){
			ext_URL = extras.getString("sURL");
			ext_title = extras.getString("sTitle");
		}
        
		setTitle(ext_title);
       WebView webview = (WebView) findViewById(R.id.webview);
       webview.loadUrl(ext_URL);
       
    }
}