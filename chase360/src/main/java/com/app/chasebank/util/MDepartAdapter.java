package com.app.chasebank.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.chasebank.R;
import com.app.chasebank.entity.MDepartment;

public class MDepartAdapter extends ArrayAdapter<MDepartment>{
	private Context context;
	private List<MDepartment> depart;
	
	public MDepartAdapter(Context context, List<MDepartment> depart) {
		super(context, 0, depart);
		this.depart = depart;
		this.context = context;
	}
	
	@Override
	public MDepartment getItem(int position) {		
		return depart.get(position);
	}
		
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	} 
	
	public View getCustomView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, parent, false);
        
		TextView label=(TextView)convertView.findViewById(R.id.textSpinner);
		label.setText(getItem(position).getName());
		return convertView;
	}	
}
