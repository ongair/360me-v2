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
import com.app.chasebank.entity.MyCompany;

public class MCompanyAdapter extends ArrayAdapter<MyCompany>{
	private Context context;
	private List<MyCompany> itemComps;
	private int layout;
	
	public MCompanyAdapter(Context context, int layout, List<MyCompany> company) {
		super(context, layout, company);
		this.itemComps = company;
		this.context = context;
		this.layout = layout;
	}
	
	@Override
	public MyCompany getItem(int position) {		
		return itemComps.get(position);
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
		convertView = LayoutInflater.from(context).inflate(layout, parent, false);
        
		TextView label=(TextView)convertView.findViewById(R.id.text_company_item);
		label.setText(getItem(position).getName());
		return convertView;
	}	
}
