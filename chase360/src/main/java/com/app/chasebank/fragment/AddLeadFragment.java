package com.app.chasebank.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.chasebank.LeadSummaryActivity;
import com.app.chasebank.R;
import com.app.chasebank.entity.MyBranch;
import com.app.chasebank.entity.MyCompany;
import com.app.chasebank.entity.MyContact;
import com.app.chasebank.entity.MyProduct;
import com.app.chasebank.framework.Screen;
import com.app.chasebank.util.DownloadService;
import com.app.chasebank.util.MyCategory;
import com.app.chasebank.util.SpinnerAdapter;

public final class AddLeadFragment extends Screen {
	public static ArrayList<MyContact> contactList = new ArrayList<MyContact>();
	private MyCompany selCompany;
	private MyProduct selProduct;
	private MyContact selContact;

	private View v;
	private EditText detailsText;
	private Button addleadBtn;
	private EditText emailText;

	private Bitmap bitmap = null;
	private TextView product_app_logo_title;
	private Spinner spinnerBranch;
	private SpinnerAdapter adapter;
	private MyCategory selCategory;

	public static MyBranch selBranch;
	private boolean isManual = false;
	private EditText textName;
	private EditText textPhone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			if(getArguments().containsKey("company")) 
				selCompany = (MyCompany) getArguments().getSerializable("company");

			if(getArguments().containsKey("product")) 
				selProduct = (MyProduct) getArguments().getSerializable("product");

			if(getArguments().containsKey("contact")) 
				selContact = (MyContact) getArguments().getSerializable("contact");

			if(getArguments().containsKey("branch")) 
				selBranch = (MyBranch) getArguments().getSerializable("branch");

			if(getArguments().containsKey("category")) 
				selCategory = (MyCategory) getArguments().getSerializable("category");

			if(getArguments().containsKey("isManual") && getArguments().getBoolean("isManual")){
				isManual = getArguments().getBoolean("isManual");
			}
		}catch(Exception ex){ex.printStackTrace();}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		v = getLayoutInflater(savedInstanceState).inflate(R.layout.activity_add_lead_new, container, false);

		init();

		return v;
	}

	private void init() {
		//Login views
		detailsText = (EditText)v.findViewById(R.id.adddet_additional_details);
		emailText = (EditText)v.findViewById(R.id.addet_emailaddress);
		textName = (EditText)v.findViewById(R.id.textName);
		textPhone = (EditText)v.findViewById(R.id.textPhone);

		addleadBtn = (Button)v.findViewById(R.id.add_lead_btn);

		spinnerBranch = (Spinner)v.findViewById(R.id.spinnerBranch); 
		// TODO Get the list of branches for the current selected company
		if(!hasBranches(selCompany.getId())) spinnerBranch.setVisibility(View.GONE);
		else {
			ArrayList<MyBranch> branchList = getBranches(selCompany.getId(), selCompany.getName());			
			spinnerBranch.setAdapter(new SpinnerAdapter(getParent(), branchList));
			if(selBranch != null){
				spinnerBranch.setSelection(getBranchPosition(selBranch, branchList));
			}
		}

		product_app_logo_title = (TextView)v.findViewById(R.id.product_app_logo_title);		
		if(selCompany != null) {
			product_app_logo_title.setText(selCompany.getName().toUpperCase());
			((EditText)v.findViewById(R.id.textCompany)).setText(selCompany.getName());
		}
		if(!isManual && selContact != null){
			if(selContact.getEmail() != null && selContact.getEmail().size() > 0 
					&& !selContact.getEmail().get(0).equalsIgnoreCase("")) {
				emailText.setText(selContact.getEmail().get(0));
			}
			if(selContact.getName() != null) textName.setText(selContact.getName());
			if(selContact.getPhone() != null) textPhone.setText(selContact.getPhone());

		}

		if(selProduct != null) {
			((EditText)v.findViewById(R.id.textProduct)).setText(selProduct.getName());
		}else {
			((EditText)v.findViewById(R.id.textProduct)).setVisibility(View.GONE);
		}

		addleadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try{
					//TODO If isManual is true, check the email, name and phone number of the contact
					String name = textName.getText().toString(),
							phone = textPhone.getText().toString(),
							email = emailText.getText().toString();
					if(isManual) {
						if(validate(name, phone, email)) {
							//Open Lead Activity
							Intent intent = new Intent(getActivity(), LeadSummaryActivity.class);

							MyContact newContact = new MyContact(name, phone);
							//Create a bundle and put everything inside
							Bundle bun = new Bundle();
							bun.putSerializable("company", selCompany);
							bun.putSerializable("product", selProduct);
							bun.putSerializable("contact", newContact);
							bun.putSerializable("branch", selBranch);
							bun.putParcelable("contactPhoto", bitmap);

							intent.putExtra("details", detailsText.getText().toString());
							intent.putExtra("email", email);					
							intent.putExtras(bun);

							startActivity(intent);
						}
					}else {
						if(validate(name, phone, email)){
							//TODO Change the contact info, check if name | phone is null
							if(selContact.getName() == null || selContact.getName().equals(""))
								selContact.setName(name);

							if(selContact.getPhone() == null || selContact.getPhone().equals(""))
								selContact.setPhone(phone);

							//Open Lead Activity
							Intent intent = new Intent(getActivity(), LeadSummaryActivity.class);
							//Create a bundle and put everything inside
							Bundle bun = new Bundle();
							bun.putSerializable("company", selCompany);
							bun.putSerializable("product", selProduct);
							bun.putSerializable("contact", selContact);
							bun.putSerializable("branch", selBranch);
							bun.putParcelable("contactPhoto", bitmap);

							intent.putExtra("details", detailsText.getText().toString());
							intent.putExtra("email", email);					
							intent.putExtras(bun);

							startActivity(intent);
						}
					}


				}catch(Exception ex) {
					ex.printStackTrace();

					Intent intent = new Intent(getActivity(), LeadSummaryActivity.class);
					startActivity(intent);
				}
			}

			/**
			 * Check if the name, phone Number and email address entered are valid for upload
			 * @param name Name of the contact
			 * @param phone The phone number of the contact
			 * @param email The Email address of the contact
			 * @return boolean true if it passes the test.
			 */
			private boolean validate(String name, String phone, String email) {
				if(name == null || name.equals("")){
					textName.setError("Contact Name is required!");
					return false;
				}
				if(phone == null || phone.equals("")){
					textPhone.setError("Phone Number is invalid!");
					return false;
				}				
				if(email == null || email.equals("")){
					emailText.setError("Email Address is required!");
					return false;
				}

				if(phone.startsWith("07") && phone.length()==10 || phone.startsWith("2547") && phone.length()==12 || 
						phone.startsWith("+254") && phone.length()==13) {
					//Nothing here
				}else {
					textPhone.setError("Phone number is invalid!");
					return false;
				}
				return true;
			}
		});

		//Set Fonts
		getParent().setFontRegular(detailsText);
		getParent().setFontRegular(emailText);
		getParent().setFontRegular(addleadBtn);

		getParent().setFontRegular(textName);
		getParent().setFontRegular(textPhone);
		getParent().setFontRegular((TextView)v.findViewById(R.id.textManual));
		getParent().setFontRegular((TextView)v.findViewById(R.id.new_holder_desc));
		getParent().setFontRegular((TextView)v.findViewById(R.id.product_app_title));			
	}

	/**
	 * 
	 * @param selBranch
	 * @param branchList
	 * @return int
	 */
	private int getBranchPosition(MyBranch selBranch, ArrayList<MyBranch> branchList) {
		int pos = 0;
		for (MyBranch myBranch : branchList) {
			if(myBranch.getId().equals(selBranch.getId())) return pos;
			pos++;
		}
		return 0;
	}

	public boolean hasBranches(String companyID) {
		ArrayList<MyBranch> branchList = new ArrayList<MyBranch>();

		if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];

				if(company.getId().equalsIgnoreCase(companyID)) {
					branchList.addAll((ArrayList<MyBranch>) data[1]);

					if (branchList.size() > 0) return true;
					else return false;					
				}
			}
		}else {
			return false;
		}

		return false;	
	}

	private ArrayList<MyBranch> getBranches(String companyID, String companyName) {
		ArrayList<MyBranch> branchList = new ArrayList<MyBranch>();
		if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];

				if(company.getId().equalsIgnoreCase(companyID)) {
					branchList.addAll((ArrayList<MyBranch>) data[1]);
				}
			}

			for (MyBranch myBranch : branchList) {
				myBranch.setCompany(companyName);
				Log.i(TAG+": BRANCH", myBranch.toString());			
			}
			return branchList;
		} else {
			return null;
		}		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			/**
			 * Attempt to logout of google +
			 */
			getParent().attemptLogout();
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}		
}
