package com.app.chasebank;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
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
import com.app.chasebank.util.DatabaseHelper;
import com.app.chasebank.util.DownloadService;
import com.app.chasebank.util.MyCategory;

public class ProductAct extends Act {
	public static ArrayList<MyProduct> productsList = new ArrayList<MyProduct>();	
	private ViewPager mPager;
	public static  MyBranch selBranch;
	public static MyCompany selCompany;

	public static final String IMAGE_CACHE_DIR = "images";
	public static final String EXTRA_IMAGE = "extra_image";

	private ImageFetcher mImageFetcher;
	private DatabaseHelper helper;
	private ListView lists;
	private CompanyAdapter adapter;
	private TextView product_app_logo_title;
	private MyCategory selCategory;

	/**
	 * Get the Products for the given company
	 * @param companyID Company ID
	 */
	private void getProduct(String companyID, String category_id) {
		productsList.clear();

		if(helper == null) helper = new DatabaseHelper(getBaseContext());
		
		if(DownloadService.companies != null && DownloadService.companies.size() > 0) {
			for (Object[] data: DownloadService.companies) {
				MyCompany company = (MyCompany) data[0];

				if(company.getId().equalsIgnoreCase(companyID)) {
					ArrayList<MyProduct> prdct = (ArrayList<MyProduct>) data[2];
					if(category_id != null) {
						for (MyProduct myProduct : prdct) {
							if(myProduct.getCategory().equals(category_id)) 
								productsList.add(myProduct);
						}
					}else {
						productsList.addAll(prdct);
					}
				}
			}
		}else {
			// Check if DB has any branches for this company
			List<MyProduct> products = helper.getAllProducts(companyID);
			productsList.addAll(products);
		}

		/**
		 * Log all the products
		 */
		for(MyProduct product: productsList) {
			Log.i(TAG+": PRODUCTS", product.toString());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_product_layout);
		
		selCompany = (MyCompany) getIntent().getExtras().getSerializable("company");
		selBranch = (MyBranch) getIntent().getExtras().getSerializable("branch");
		selCategory = (MyCategory) getIntent().getExtras().getSerializable("category");
		product_app_logo_title = (TextView) findViewById(R.id.product_app_logo_title);

		if (selCompany != null) {
			product_app_logo_title.setText(selCompany.getName().toUpperCase());
			if(selCategory != null)
				getProduct(selCompany.getId(), selCategory.getId());
			else 
				getProduct(selCompany.getId(), null);
		}

		//init();

		lists = (ListView) findViewById(R.id.lists);
		adapter = new CompanyAdapter();
		lists.setAdapter(adapter);

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
				new com.app.chasebank.bitmap.util.ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(this, 256);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(true);
		mImageFetcher.setLoadingImage(R.drawable.products);

	}*/

	@Override
	public void onResume() {
		super.onResume();
		//mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//mImageFetcher.setExitTasksEarly(true);
		//mImageFetcher.flushCache();
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

    /*
	private void initImageLoading() {
		com.app.chasebank.bitmap.util.ImageCache.ImageCacheParams cacheParams = 
				new com.app.chasebank.bitmap.util.ImageCache.ImageCacheParams(this, ProductAct.IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(this, 256);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(true);
		mImageFetcher.setLoadingImage(R.drawable.products);
	}*/

    /**
     * Product List Adapter.
     */
	class CompanyAdapter extends BaseAdapter {
		public String mImageUrl;
		public NetworkImageView logo;
		public ImageFetcher mImageFetcher;

		@Override
		public int getCount() {
			return productsList.size();
		}

		@Override
		public MyProduct getItem(int position) {
			return productsList.get(position);
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
			// TODO Auto-generated method stub
			MyProduct product = productsList.get(position);
			Log.i(TAG, "POSITION SCREEN S:"+position);

			v = getLayoutInflater().inflate(R.layout.new_holder_left,  parent, false);				

			if(product != null) {
				logo = (NetworkImageView)
						v.findViewById(R.id.new_holder_icon);
				TextView title = (TextView) v.findViewById(R.id.new_holder_title);
				TextView desc = (TextView) v.findViewById(R.id.new_holder_desc);
				View text_layout = v.findViewById(R.id.text_layout);
				
				// The font
				setFontRegular(desc);
				setFontSemiBold(title);
				
				try {
					logo.setImageResource(R.drawable.products);
                    logo.setDefaultImageResId(R.drawable.products);
					//ImageLoader loader = new ImageLoader(AppController.getInstance(), )
                    ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(ProductAct.this), new LruBitmapCache());
                    logo.setImageUrl(product.getUrl(), imageLoader);
				}catch(Exception ex){ex.printStackTrace(); }

				title.setText(product.getName());
				desc.setText(product.getDescription());

				//Set Fonts
				setFontRegular(title);
				setFontRegular(desc);

				/**
				 * Clicking the text layout open a dialog with 	more information
				 */
				if(getItem(position).getDescription() != null && !getItem(position).getDescription().equals("")) {
					text_layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO a dialog....to be done
							CompanyDetailsPopup.newInstance(getItem(position).getName(), getItem(position)
									.getDescription())
									.show(getSupportFragmentManager(), "PRODUCT_DETAILS");
						}
					});
				}

				//onclick listener
				logo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Bitmap bitmap = null;
						BitmapDrawable test = null;
						try {test = (BitmapDrawable) logo.getDrawable(); } catch(Exception ex){ex.printStackTrace(); }

						try {				
							if(test != null) bitmap = test.getBitmap();							
						}catch(Exception ex){ex.printStackTrace(); }

						Bundle bun = new Bundle();
						bun.putSerializable("product", productsList.get(position));
						bun.putSerializable("company", selCompany);
						bun.putSerializable("branch", selBranch);
						bun.putSerializable("category", selCategory);

						ContactsFragment conts = new ContactsFragment();
						conts.setArguments(bun);	       	
						switchScreen(conts);
					}
				});		

			}
			return v;
		}

	}

}