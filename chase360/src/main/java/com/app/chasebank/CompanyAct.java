package com.app.chasebank;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.app.chasebank.bitmap.util.ImageFetcher;
import com.app.chasebank.entity.MyBranch;
import com.app.chasebank.entity.MyCompany;
import com.app.chasebank.entity.MyProduct;
import com.app.chasebank.fragment.CompanyDetailsPopup;
import com.app.chasebank.fragment.ContactsFragment;
import com.app.chasebank.framework.Act;
import com.app.chasebank.framework.AppController;
import com.app.chasebank.framework.LruBitmapCache;
import com.app.chasebank.util.CommonUtils;
import com.app.chasebank.util.DatabaseHelper;
import com.app.chasebank.util.DownloadService;
import com.app.chasebank.util.MyCategory;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CompanyAct extends Act {
	public static ArrayList<MyCompany> companyList = new ArrayList<MyCompany>();
	private static ProgressDialog pDialog;
	private DatabaseHelper helper = null;

	private ViewPager mPager;
	private CompanyAdapter adapter;
	private ListView list;
	private ImageFetcher mImageFetcher;

	public static final String EXTRA_IMAGE = "extra_image";	

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {	    	  
				int resultCode = bundle.getInt(DownloadService.RESULT);

				if (resultCode == DownloadService.RESULT_COMPANIES) {
					/**
					 * Companies update complete
					 */
					//toast("Companies successfully updated..");

					if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
						companyList.clear();
						for (Object[] data: DownloadService.companies) {
							MyCompany company = (MyCompany) data[0];

							companyList.add(company);
						}
					}

					// Refresh the adapter
					refreshAdapter();
				} else if (resultCode == DownloadService.RESULT_NETWORK_NOT_AVAILABLE) {
					/**
					 * Network not available
					 */
					toast("Sorry, Network is not available, please check your connection and try again.");
				} else if (resultCode == DownloadService.RESULT_CHECK_DB) {
					refreshAdapter();
				}
			}
		}
	};

	/**
	 * Get the list of companies from the downloaded data
	 */
	public void getCompanies(){
		companyList.clear();
		
		/**
		 * Check if the download service has any data, else load from the db
		 */
		if(DownloadService.companies == null || DownloadService.companies.size() <= 0) {
			// Load the company's information from the database
			if(helper == null) helper = new DatabaseHelper(getBaseContext());

			List<MyCompany> comp = helper.getAllCompanies();
			companyList.addAll(comp);

		}else {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];

				companyList.add(company);
			}
		}		
	}

	/**
	 * Check if the selected company has branches, so that we can just jump directly to products
	 * 
	 * @param companyID The Company Id
	 * @return boolean true if branches available, false otherwise
	 */
	public boolean hasBranches(String companyID) {
		ArrayList<MyBranch> branchList = new ArrayList<MyBranch>();
		if(helper == null) helper = new DatabaseHelper(getBaseContext());

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
			// Check if DB has any branches for this company
			List<MyBranch> branches = helper.getAllBranches(companyID);
			if(branches.size() > 0) return true;
			else return false;
		}

		return false;	
	}

	/**
	 * Check if the selected company has Product Categories.
	 * @param companyID The Selected Company ID
	 * @return boolean TRUE - has categories, FALSE - otherwise
	 */
	@SuppressWarnings("unchecked")
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
		}
		return false;	
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_company_layout);

		//init();

		adapter = new CompanyAdapter();

		list = (ListView) findViewById(R.id.lists);
		adapter = new CompanyAdapter();
		list.setAdapter(adapter);

		createCompanies();

		/**
		 * When the goto previous is selected / clicked
		 */
		View back = findViewById(R.id.product_app_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO back to selectCompany
				finish();
			}
		});

	}

    /*
	private void init() {
		com.app.chasebank.bitmap.util.ImageCache.ImageCacheParams cacheParams = 
				new com.app.chasebank.bitmap.util.ImageCache.ImageCacheParams(this, ProductAct.IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(this, 256);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(false);
		mImageFetcher.setLoadingImage(R.drawable.products);

	}*/

	/**
	 * We create some dummy products here
	 */
	private void createCompanies() {
		/**
		 * Lets get the list of companies first, either from db or from the server
		 */
		getCompanies(getBaseContext());
		getCompanies();
		
		for (MyCompany myCompany : companyList) {
			if(myCompany.getName().contains("Winton")) {
				myCompany.setLogo(R.drawable.icon_chasewinton_co_256);
			}
			else if(myCompany.getName().contains("Assurance")) {
				myCompany.setLogo(R.drawable.icon_chase_assurance);
			}
			else if(myCompany.getName().contains("Bank")) {
				myCompany.setLogo(R.drawable.icon_chasebank);
			}
			else if(myCompany.getName().contains("Rafiki")) {
				myCompany.setLogo(R.drawable.icon_rafiki);
			}
			else if(myCompany.getName().contains("Orchid")) {
				myCompany.setLogo(R.drawable.icon_orchid_capital);
			}
			else if(myCompany.getName().contains("LightHouse")) {
				myCompany.setLogo(R.drawable.icon_lighthouse);
			}
			else if(myCompany.getName().contains("Iman")) {
				myCompany.setLogo(R.drawable.icon_chase_iman);
			}
			else if(myCompany.getName().contains("GenCap")) {
				myCompany.setLogo(R.drawable.icon_gencap_trust);
			}
			else if(myCompany.getName().contains("Genghis")) {
				myCompany.setLogo(R.drawable.icon_genghis_capital);
			}
			else if(myCompany.getName().contains("Tulip")) {
				myCompany.setLogo(R.drawable.icon_tulip_healthcare);
			}
		}		
	}

	/**
	 * Refreshes the Adapter
	 */
	private void refreshAdapter() {	
		if(companyList == null || companyList.size() <= 0) {
			getCompanies();
		}
		
		for (MyCompany myCompany : companyList) {
			if(myCompany.getName().contains("Winton")) {
				myCompany.setLogo(R.drawable.icon_chasewinton_co_256);
			}
			else if(myCompany.getName().contains("Assurance")) {
				myCompany.setLogo(R.drawable.icon_chase_assurance);
			}
			else if(myCompany.getName().contains("Bank")) {
				myCompany.setLogo(R.drawable.icon_chasebank);
			}
			else if(myCompany.getName().contains("Rafiki")) {
				myCompany.setLogo(R.drawable.icon_rafiki);
			}
			else if(myCompany.getName().contains("Orchid")) {
				myCompany.setLogo(R.drawable.icon_orchid_capital);
			}
			else if(myCompany.getName().contains("LightHouse")) {
				myCompany.setLogo(R.drawable.icon_lighthouse);
			}
			else if(myCompany.getName().contains("Iman")) {
				myCompany.setLogo(R.drawable.icon_chase_iman);
			}
			else if(myCompany.getName().contains("GenCap")) {
				myCompany.setLogo(R.drawable.icon_gencap_trust);
			}
			else if(myCompany.getName().contains("Genghis")) {
				myCompany.setLogo(R.drawable.icon_genghis_capital);
			}
			else if(myCompany.getName().contains("Tulip")) {
				myCompany.setLogo(R.drawable.icon_tulip_healthcare);
			} 	
		}			
			
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
		//mImageFetcher.setExitTasksEarly(false);

		registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
	}

	@Override
	protected void onPause() {
		super.onPause();
		//mImageFetcher.setExitTasksEarly(true);
		//mImageFetcher.flushCache();

		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//mImageFetcher.closeCache();
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	public void getCompanies(Context context) {
		String  serverUrl = CommonUtils.SERVER_URL + "companies.json";
		
		Cache cache = AppController.getInstance().getRequestQueue().getCache();
		Entry entry = cache.get(serverUrl);
		if(entry != null){
			try {
				String response = new String(entry.data, "UTF-8");
				Log.i(TAG, "getCompanies-Cache: "+ response);				
				DownloadService.companies = CommonUtils.processCompanies(response);
				splitCompanies();
				refreshAdapter();

			} catch (UnsupportedEncodingException e) {      
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else{
			// Tag used to cancel the request
			String tag_json_obj = "json_obj_req";

			showDialog(true);

			JsonArrayRequest jsonObjReq = new JsonArrayRequest(serverUrl, new Response.Listener<JSONArray>() {
				
				@Override
				public void onResponse(JSONArray response) {
					Log.i(TAG, response.toString());				
					showDialog(false);
					try {
						DownloadService.companies = CommonUtils.processCompanies(response.toString());	
						splitCompanies();
						refreshAdapter();					
					} catch (JSONException e) {
						e.printStackTrace();
					}					
				}
			}, 
			new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					VolleyLog.d(TAG, "Error: " + error.getMessage());
					// hide the progress dialog
					showDialog(false);
				}
			});

			// Adding request to request queue
			AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
		}    			
	}

	private static void splitCompanies() {
		if(DownloadService.companies != null) {
			// Process Company Information
			if(DownloadService.companies.size() > 0) {
				Log.i(TAG, "Companies size: "+ DownloadService.companies.size());			
				for (Object[] data: DownloadService.companies) {
					MyCompany company = (MyCompany) data[0];
					DownloadService.branchList.addAll((ArrayList<MyBranch>) data[1]);
					DownloadService.productsList.addAll((ArrayList<MyProduct>) data[2]);

					if(data.length > 3) {
						DownloadService.categoryList.addAll((ArrayList<MyCategory>) data[3]);

						for (MyCategory category : (ArrayList<MyCategory>) data[3]) {
							Log.i(TAG+": CATEGORY", category.toString());
						}					
					}
				}				
			}		
		}
	}

	/**
	 * 	List adapter
	 * @author MATIVO-PC
	 *
	 */
	class CompanyAdapter extends BaseAdapter{
		private NetworkImageView logo;
		private String mImageUrl;

		public CompanyAdapter() {
		}

		@Override
		public int getCount() {
			return companyList.size();
		}

		@Override
		public MyCompany getItem(int position) {
			return companyList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View v, ViewGroup parent) {
			// Get company item from the arraylist at this position
			MyCompany company = getItem(position);

			if((position + 1) % 2 == 0){
				// Right
				v = getLayoutInflater().inflate(R.layout.new_holder_right,  parent, false);				
			}
			else if((position + 1) % 2 == 1){
				//Left
				v = getLayoutInflater().inflate(R.layout.new_holder_left,  parent, false);				
			}

			// Set the content to be shown, fetched from the company instance acquired above.
			if(company != null) {
				logo = (NetworkImageView) v.findViewById(R.id.new_holder_icon);
				TextView title = (TextView) v.findViewById(R.id.new_holder_title);
				TextView desc = (TextView) v.findViewById(R.id.new_holder_desc);

				logo.setImageResource(company.getLogo());
				title.setText(company.getName());
				desc.setText(company.getDescription());

				//set background for the texts #eeeeee
				View text_layout = v.findViewById(R.id.text_layout);
				text_layout.setBackgroundResource(R.color.new_holder_text_layout_background);

				// The fonts
				setFontRegular(desc);
				setFontSemiBold(title);

				/**
				 * Clicking the text layout open a dialog with 	more information
				 */
				text_layout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO a dialog....to be done
						CompanyDetailsPopup.newInstance(getItem(position).getName(), getItem(position)
								.getDescription())
								.show(getSupportFragmentManager(), "COMPANY_DETAILS");
					}
				});

				try {
                    int logoRes = company.getLogo();
                    if(logoRes != 0) {
                        logo.setImageResource(logoRes);
                        logo.setDefaultImageResId(logoRes);
                    }
                    else {
                        logo.setImageResource(R.drawable.products);
                        logo.setDefaultImageResId(R.drawable.products);
                    }
                    //Start the loader.
					ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(CompanyAct.this), new LruBitmapCache());
                    logo.setImageUrl(company.getImage_url(), imageLoader);

				}catch(Exception ex){
					ex.printStackTrace();
					//mImageFetcher.loadImage(company.getImage_url(), logo);
				}

				// On Click Listener
				logo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Open Branches Fragment
						Bundle bun = new Bundle();
						
						bun.putSerializable("company", companyList.get(position));						
						Intent intent = null;
						
						if(hasBranches(companyList.get(position).getId())) {
							intent = new Intent(CompanyAct.this, Branch.class);							

							intent.putExtras(bun);
							startActivity(intent);
						}
						else if(hasCategories(companyList.get(position).getId())) {
							intent = new Intent(CompanyAct.this, ProductCategories.class);
							
							intent.putExtras(bun);
							startActivity(intent);
						}
						else {
							if(hasProducts(companyList.get(position).getId())) {
								intent = new Intent(CompanyAct.this, ProductAct.class);
								bun.putSerializable("branch", null);
								
								intent.putExtras(bun);
								startActivity(intent);
							}else {
								// Lets redirect this quy to contacts
								bun.putSerializable("product", null);
								bun.putSerializable("branch", null);
								
								ContactsFragment conts = new ContactsFragment();
								conts.setArguments(bun);	           	
								switchScreen(conts);
							}
						}
					}
				});	

				return v;
			}else return null;
		}

	}

}
