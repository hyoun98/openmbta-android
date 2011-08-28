package com.kaja.openmbta;

import android.content.Context;
import java.util.*;

import com.kaja.openmbta.transItem;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.*;

public class transAdapter extends ArrayAdapter<transItem> {

	  int resource;

	  public transAdapter(Context _context, 
	                             int _resource, 
	                             List<transItem> _items) {
	    super(_context, _resource, _items);
	    resource = _resource;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	   View row=convertView;
		transHolder holder=null;
		  transItem item = getItem(position);
				
				if (row==null) {		
					 String sinflater = Context.LAYOUT_INFLATER_SERVICE;
				      LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(sinflater);
					
					//LayoutInflater inflater=getLayoutInflater();
					
					row=inflater.inflate(resource, parent, false);
					holder=new transHolder(row);
					row.setTag(holder);
				}
				else {
					holder=(transHolder)row.getTag();
				}
				
				//holder.populateFrom(item.get(position));
				holder.populateFrom(item);
				
				return(row);

		}
		
		static class transHolder {
			private TextView name=null;
			private TextView desc=null;
			private ImageView icon=null;
			
			transHolder(View row) {
				name=(TextView)row.findViewById(R.id.title);
				desc=(TextView)row.findViewById(R.id.description);
				icon=(ImageView)row.findViewById(R.id.icon);
			}
			
			void populateFrom(transItem t) {
				name.setText(t.getName());
				desc.setText(t.getDesc());
		
				if (t.getType().toLowerCase().equals("subway")) {
					icon.setImageResource(R.drawable.subway);
				
				}
				else if (t.getType().toLowerCase().equals("bus")) {
					icon.setImageResource(R.drawable.bus);
				}
				else if (t.getType().equals("ybus")) {
					icon.setImageResource(R.drawable.bus);
				}
				else if (t.getType().toLowerCase().equals("boat")) {
					icon.setImageResource(R.drawable.boat);
				}
				else if (t.getType().toLowerCase().equals("commuter rail")) {
					icon.setImageResource(R.drawable.train);
				}
				else if (t.getType().toLowerCase().equals("train")) {
					icon.setImageResource(R.drawable.train);
				}
				else if (t.getType().equals("alert")) {
						icon.setImageResource(R.drawable.alert);
				}
				else if (t.getType().equals("tweet")) {
					icon.setImageResource(R.drawable.tweet);
			}
				else {
					icon.setImageResource(R.drawable.boat);
				}
			}
		}
	}