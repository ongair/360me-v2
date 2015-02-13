package com.app.chasebank.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class MDepartment implements Serializable{
	private static final long serialVersionUID = -6388052693047107501L;
	private static final String TAG = "MDepartment";
	private String id, name, company_id;
	
	public MDepartment(String id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	public MDepartment(String pid, String pname, String company_id) {
		this.setId(pid);
		this.setName(pname);
		this.setCompany_id(company_id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "MDepartment:{id: "+id+", name: "+name+", company: "+company_id+"}";
	}

	public String getCompany_id() {
		return company_id;
	}

	public void setCompany_id(String company_id) {
		this.company_id = company_id;
	}
	
	/**
	 * Sort the list of departments and return the 
	 * @param depts List<MDepartment>
	 * @param company String 
	 * @return List<MDepartment>
	 */
	public static List<MDepartment> sort(List<MDepartment> depts, String company){
		List<MDepartment> deptz = new ArrayList<MDepartment>();
		for (MDepartment mDepartment : depts) {
			if(mDepartment.getCompany_id() == null || mDepartment.getCompany_id().equals("null") || mDepartment.getCompany_id().equals(company) || 					 
					mDepartment.getCompany_id().equals("")){
				deptz.add(mDepartment);
			}
		}
		Log.i(TAG, "----------------------------\nCOMPANY: "+company);
		for (MDepartment mDepartment : deptz) {
			Log.i(TAG, mDepartment.toString());
		}
		return deptz;
	}
	
}
