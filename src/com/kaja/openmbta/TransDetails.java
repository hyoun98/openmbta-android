package com.kaja.openmbta;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import android.app.Dialog;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class TransDetails extends Activity {
    
	
	  //private boolean addingNew = false;
	  private ArrayList<String> transItems;
	  private ListView myListView;
	  private ArrayAdapter<String> aa;
	  
	  static final private int TRANS_DIALOG = 1;
	  String selectedTrans;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        // Get references to UI widgets
    //    myListView = (ListView)findViewById(R.id.myListView);
        myListView = (ListView)findViewById(android.R.id.list);
          transItems = new ArrayList<String>();
        
        int resID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<String>(this, resID, transItems);
        loadItems();
        myListView.setAdapter(aa);
        
        
        myListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
            	selectedTrans = transItems.get(_index);
              showDialog(TRANS_DIALOG);
            }
          });
        
    }
    
    public Dialog onCreateDialog(int id) {
        switch(id) {
          case (TRANS_DIALOG) :
           LayoutInflater li = LayoutInflater.from(this);
            View transDetailsView = li.inflate(R.layout.dialog_details, null);

            AlertDialog.Builder transDialog = new AlertDialog.Builder(this);
            transDialog.setTitle("OpenMBTA");
            transDialog.setView(transDetailsView);
            return transDialog.create();
        }
        return null;
      }
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
      switch(id) {
        case (TRANS_DIALOG) :
          //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
          //String dateString = sdf.format(selectedQuake.getDate()); 
          //String transText =
           

          AlertDialog transDialog = (AlertDialog)dialog;
          transDialog.setTitle("OnPrepare");
          TextView tv = (TextView)transDialog.findViewById(R.id.dialogDetailsTextView);
          tv.setText(selectedTrans);

          break;
      }
    }
    
    
    private void loadItems(){
    	transItems.add(0, "Line 5-1 \nLine 5-2");
    	transItems.add(0, "Line4");
    	transItems.add(0, "Line3");
    	transItems.add(0, "Line2");
    	transItems.add(0, "Line1");

    	aa.notifyDataSetChanged();
    }
}