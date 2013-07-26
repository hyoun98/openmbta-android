package com.kaja.openmbta;

import android.content.Context;
import java.util.*;

import com.kaja.openmbta.ScheduleItem;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.*;

public class ScheduleAdapter extends ArrayAdapter<ScheduleItem> {

	  int resource;

	  public ScheduleAdapter(Context _context, 
	                             int _resource, 
	                             List<ScheduleItem> _items) {
	    super(_context, _resource, _items);
	    resource = _resource;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	   View row=convertView;
		ScheduleHolder holder=null;
		  ScheduleItem item = getItem(position);
				
				if (row==null) {		
					 String sinflater = Context.LAYOUT_INFLATER_SERVICE;
				      LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(sinflater);
					
					//LayoutInflater inflater=getLayoutInflater();
					
					row=inflater.inflate(resource, parent, false);
					holder=new ScheduleHolder(row);
					row.setTag(holder);
				}
				else {
					holder=(ScheduleHolder)row.getTag();
				}
				
				//holder.populateFrom(item.get(position));
				holder.populateFrom(item);
				
				return(row);

		}
		
		static class ScheduleHolder {
			private TextView name=null;
			private TextView desc1=null;
			private TextView desc2=null;
			private TextView desc3=null;
			private TextView desc4=null;
			private ImageView icon=null;
			
			ScheduleHolder(View row) {
				name=(TextView)row.findViewById(R.id.title);
				desc1=(TextView)row.findViewById(R.id.description1);
				desc2=(TextView)row.findViewById(R.id.description2);
				desc3=(TextView)row.findViewById(R.id.description3);
				desc4=(TextView)row.findViewById(R.id.description4);
				icon=(ImageView)row.findViewById(R.id.icon);
			}
			
			void populateFrom(ScheduleItem t) {
				name.setText(t.getName());
				desc1.setText(t.getDesc(0)); 
				desc2.setText(t.getDesc(1));
				desc3.setText(t.getDesc(2));
				desc4.setText(t.getDesc(3));
		
				if (t.getType().equals("subway")) {
					icon.setImageResource(R.drawable.subway);
				}
				else if (t.getType().equals("bus")) {
					icon.setImageResource(R.drawable.bus);
				}
				else if (t.getType().equals("ybus")) {
					icon.setImageResource(R.drawable.bus);
				}
				else if (t.getType().equals("boat")) {
					icon.setImageResource(R.drawable.boat);
				}
				else if (t.getType().equals("train")) {
					icon.setImageResource(R.drawable.train);
				}
				else if (t.getType().equals("alert")) {
						icon.setImageResource(R.drawable.alert);
				}
				else {
					icon.setImageResource(R.drawable.bus);
				}
			}
		}
	}