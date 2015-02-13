package com.app.chasebank;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.chasebank.entity.MyBranch;
import com.app.chasebank.entity.MyCompany;
import com.app.chasebank.entity.MyProduct;
import com.app.chasebank.fragment.ContactsFragment;
import com.app.chasebank.framework.Act;
import com.app.chasebank.util.DatabaseHelper;
import com.app.chasebank.util.DownloadService;
import com.app.chasebank.util.MyCategory;

public class Branch extends Act {
	public static ArrayList<MyBranch> branchList = new ArrayList<MyBranch>();	

	private ViewPager mPager;
	private ListView listBranch;
	private BranchAdapter adapter;
	private DatabaseHelper helper;
	public static MyCompany selCompany;

	/**
	 * Get the list of Branches for the given company
	 * 
	 * @param companyID The Company ID
	 */
	private void getBranches(String companyID, String companyName) {
		branchList.clear();
		if(helper == null) helper = new DatabaseHelper(getBaseContext());

		if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];

				if(company.getId().equalsIgnoreCase(companyID)) {
					branchList.addAll((ArrayList<MyBranch>) data[1]);
				}
			}

		} else {
			// Check if DB has any branches for this company
			List<MyBranch> branches = helper.getAllBranches(companyID);
			branchList.addAll(branches);
		}

		/**
		 * Set the branchList
		 */
		for (MyBranch myBranch : branchList) {
			myBranch.setCompany(companyName);
			Log.i(TAG+": BRANCH", myBranch.toString());			
		}
	}

	/**
	 * Check if the selected company has Products, so that we can just jump directly to Contacts
	 * 
	 * @param companyID The Company ID 
	 * @return boolean TRUE - has products, FALSE - otherwise
	 */
	private boolean hasProducts(String companyID) {
		ArrayList<MyProduct> productsList = new ArrayList<MyProduct>();
		if(helper == null) helper = new DatabaseHelper(getBaseContext());

		if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];

				if(company.getId().equalsIgnoreCase(companyID)) {
					productsList.addAll((ArrayList<MyProduct>) data[2]);

					if (productsList.size() > 0) return true;
					else return false;
				}
			}
		}else {
			// Check if DB has any branches for this company
			List<MyProduct> products = helper.getAllProducts(companyID);
			if(products.size() > 0) return true;
			else return false;
		}	

		return false;
	}

	/**
	 * Check if the selected company has Product Categories.
	 * @param companyID The Selected Company ID
	 * @return boolean TRUE - has categories, FALSE - otherwise
	 */
	public boolean hasCategories(String companyID) {
		ArrayList<MyCategory> categoryList = new ArrayList<MyCategory>();
		if(helper == null) helper = new DatabaseHelper(getBaseContext());
		
		if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];
				
				if(company.getId().equalsIgnoreCase(companyID)) {
					if(data.length > 3) {
						categoryList.addAll((ArrayList<MyCategory>) data[3]);

						if (categoryList.size() > 0) return true;
						else return false;	
					}else return false;					
				}
			}
		}else {
			// Check if DB has any branches for this company
			List<MyBranch> branches = helper.getAllBranches(companyID);
			if(branches.size() > 0) return true;
			else return false;
		}
		return false;	
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_branch);

		selCompany = (MyCompany) getIntent().getExtras().getSerializable("company");

		if (selCompany != null) {
			getBranches(selCompany.getId(), selCompany.getName());
			((TextView)findViewById(R.id.product_app_logo_title)).setText(selCompany.getName().toUpperCase());
			setFontSemiBold(((TextView)findViewById(R.id.product_app_logo_title)));
		}
		
		initUI();
	}

	private void initUI() {
		listBranch = (ListView) findViewById(R.id.branch_list);
		adapter = new BranchAdapter();
		listBranch.setAdapter(adapter);
		
		ImageView back = (ImageView) findViewById(R.id.product_app_back);
		
		OnClickListener notilist = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Launch the Lead activity for now
				switch (v.getId()) {
				case R.id.product_app_back:
					finish();
					break;

				default:
					break;
				}
			}
		};
		
		back.setOnClickListener(notilist);
		
	}

	public class BranchAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return branchList.size();
		}
		
		@Override
		public MyBranch getItem(int position) {
			return branchList.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View v, ViewGroup parent) {
			v = getLayoutInflater().inflate(R.layout.list_branch_holder, parent, false);
			
			//Name and company
			TextView name = (TextView) v.findViewById(R.id.list_branch_name);

			//Set Text / Details
			name.setText(getItem(position).getName());

			//Set Fonts
			setFontSemiBold(name);

			// Onclick listener: Open Products
			v.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle bun = new Bundle();
					bun.putSerializable("company", selCompany);	
					bun.putSerializable("branch", branchList.get(position));
					
					Intent intent = null;					
					if(hasCategories(selCompany.getId())) {
						// TODO open the categories
						intent = new Intent(Branch.this, ProductCategories.class);
						
						intent.putExtras(bun);
						Branch.this.finish();
						startActivity(intent);
					}else if(hasProducts(selCompany.getId())) {
						// TODO Open the Products
						intent = new Intent(Branch.this, ProductAct.class);
						
						intent.putExtras(bun);
						Branch.this.finish();
						startActivity(intent);
					}else {
						// TODO Open the contacts
						bun.putSerializable("product", null);
						
						ContactsFragment conts = new ContactsFragment();
						conts.setArguments(bun);	            	
						switchScreen(conts);
					}
				}
			});
			return v;
		}		
	}	
}
