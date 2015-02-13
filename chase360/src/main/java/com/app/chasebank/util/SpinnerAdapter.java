package com.app.chasebank.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.chasebank.Branch;
import com.app.chasebank.R;
import com.app.chasebank.entity.MyBranch;

public class SpinnerAdapter extends ArrayAdapter<String>{
	private Context context;
	private String[] branchIds;
	private String[] names;
	
	public SpinnerAdapter(Context context, List<MyBranch> branchs) {
		super(context, 0, getBranchesArray(branchs).get(1));
		branchIds = getBranchesArray(branchs).get(0);
		names = getBranchesArray(branchs).get(1);
		this.context = context;
	}
	
	@Override
	public String getItem(int position) {		
		return names[position];
	}
	
	public String getItemID(int position) {
		return branchIds[position];
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
		label.setText(names[position]);
		return convertView;
	}	
	
	/**
	 * Returns a two-dimensional array of the branches and their ids
	 * @param branchList A list of MyBranch instances
	 * @return String[][]
	 */
	public static List<String[]> getBranchesArray(List<MyBranch> branchList) {
		List<String[]> data = new ArrayList<String[]>();
		String[] names = new String[branchList.size()];
		String[] branchIds = new String[branchList.size()];
		
		int c = 0;
		for (MyBranch branch : branchList) {
			//TODO set the name and id of the branch
			branchIds[c] = branch.getId();
			names[c] = branch.getName();
			c++;
		}
		data.add(branchIds);
		data.add(names);
		return data;
	}
	
}
