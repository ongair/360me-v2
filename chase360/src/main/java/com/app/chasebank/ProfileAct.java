package com.app.chasebank;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.app.chasebank.framework.Act;
import com.app.chasebank.framework.Rounder;
import com.app.chasebank.util.CommonUtils;
import com.app.chasebank.util.DatabaseHelper;
import com.app.chasebank.util.NotiMessage;
/**
 * This activity creates and manages the profile page of the user
 * Also acts as the home page, from where users can create their Leads.
 * 
 * @author MATIVO-PC
 *
 */
public class ProfileAct extends Act {
	public ArrayList<NotiMessage> notiList = new ArrayList<NotiMessage>();
	private NotiAdapter adapter;
	private DatabaseHelper db;
	private ListView listv;
	private Timer t;
	private TextView textPoints;
	private ImageView profile;
	private boolean isPhotoFound = true;

	@Override
	protected void onCreate(Bundle onSavedInstance) {
		super.onCreate(onSavedInstance);
		setContentView(R.layout.activity_profile);
		db = new DatabaseHelper(this);
		
		setFontSemiBold((TextView)findViewById(R.id.profile_logo_title));
		setFontRegular((TextView)findViewById(R.id.profile_app_title));
		setFontSemiBold((TextView)findViewById(R.id.profile_name));
		setFontRegular((TextView)findViewById(R.id.profile_role));
		setFontRegular((TextView)findViewById(R.id.profile_points));
		
		setFontRegular((TextView)findViewById(R.id.lead_section_add_lead));
		setFontRegular((TextView)findViewById(R.id.lead_section_my_leads));
		setFontRegular((TextView)findViewById(R.id.lead_section_notification));
		
		textPoints = (TextView)findViewById(R.id.profile_points);
		
		init();
		
		initAdapter();
		
		setupProfilePhoto();
		
		getPoints();
		
	}
	
	private void getPoints() {
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... params) {
				String user = getUserId();				
				return CommonUtils.getPoints(user);
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				super.onPostExecute(result);
				if(result != null) {
					try {
						String points_available = result.getString("points_available");
						String image_url = result.getString("image_url");
						
						textPoints.setText("Miles: "+points_available);
						
						if(image_url != null && !isPhotoFound) {
							Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
							    @Override
							    public void onResponse(Bitmap bitmap) {
							        if(bitmap != null) {
							        	Bitmap bitmaps = Rounder.getRoundedBitmap(bitmap, 160, 160);
										profile.setImageBitmap(bitmaps);
							        }
							    }
							};
							
							new ImageRequest(
							        image_url,
							        listener,
							        160,
							        160,
							        null,
							        null);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				}
			}
			
		}.execute();
	}
	
	private void setupProfilePhoto() {
		profile = (ImageView)findViewById(R.id.signup_photo);
		if(getProfilePhoto() != null && !getProfilePhoto().equals("")) {
			Log.i(TAG, getProfilePhoto());
			try {
				Bitmap bitmap = Rounder.getRoundedShape(getProfilePhoto(), 160, 160);
				profile.setImageBitmap(bitmap);
			}catch(Exception ex) {
				ex.printStackTrace();
				Bitmap bitmap = Rounder.getRoundedShape(R.drawable.icon_upload_32, this, 160, 160);
				profile.setImageBitmap(bitmap);
				isPhotoFound  = false;	
			}
		}
		
		//Load[username / name; department]
		String department = getDeptName();
		String company = getCompanyName();
		((TextView)findViewById(R.id.profile_role)).setText(company+" | "+department);
		String name = getName();
		((TextView)findViewById(R.id.profile_name)).setText(name);		
	}

	private void initAdapter() {
		//TODO Create and set the Adapter
		adapter = new NotiAdapter();
		listv = (ListView) findViewById(R.id.list_profile);
		listv.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();	
		try {
			t = new Timer();

			t.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					try {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								refreshNotifications();
							}
						});		
					}catch(Exception ex) {ex.printStackTrace();}
				}
			}, 30000, 60000);	
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void refreshNotifications() {
		// TODO Get from db
		notiList.clear();
		List<NotiMessage> nots = db.getAllNotification();
		notiList.addAll(nots);

		if(adapter != null) adapter.notifyDataSetChanged();	
	}

	private void compineNotifications() {
		try {
			// TODO Get from db
			List<NotiMessage> nots = db.getAllNotification();
			notiList.addAll(nots);

			if(adapter != null) adapter.notifyDataSetChanged();	
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		t.cancel();
	}
	
	private void init() {		
		OnClickListener list = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.lead_section_add_lead:
					//TODO Load the Add Lead module::
					Intent intent = new Intent(ProfileAct.this, CompanyAct.class);
					//ProfileAct.this.finish();
					startActivity(intent);
					break;
				case R.id.lead_section_my_leads:
					startActivity(new Intent(ProfileAct.this, LeadSummaryActivity.class));
					//ProfileAct.this.finish();
					break;
				case R.id.lead_section_notification:

					break;

				default:
					break;
				}
			}
		};

		((TextView) findViewById(R.id.lead_section_notification)).setOnClickListener(list);
		((TextView) findViewById(R.id.lead_section_add_lead)).setOnClickListener(list);
		((TextView) findViewById(R.id.lead_section_my_leads)).setOnClickListener(list);
		
		/*
		 * Set up the notifications list
		 */
		listv = (ListView)findViewById(R.id.list_profile);
		adapter = new NotiAdapter();
		listv.setAdapter(adapter);
		
		compineNotifications();
	}

	class NotiAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return notiList.size();
		}

		@Override
		public NotiMessage getItem(int position) {
			return notiList .get(position);
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
			v = getLayoutInflater().inflate(R.layout.timeline_item_headline,  parent, false);				
			
			// Set the content to be shown, fetched from the company instance acquired above.
			if(getItem(position) != null) {
				TextView textTimeline_hdlineTitle = (TextView) v.findViewById(R.id.textTimeline_hdlineTitle);
				TextView textTimeline_hdline = (TextView) v.findViewById(R.id.textTimeline_hdline);
				TextView textTimeline_hdlineDate = (TextView) v.findViewById(R.id.textTimeline_hdlineDate);
				
				//textTimeline_hdlineTitle.setVisibility(View.INVISIBLE);
				textTimeline_hdlineTitle.setText(getItem(position).getSender());
				textTimeline_hdline.setText(Html.fromHtml(getItem(position).getMessage()));
				textTimeline_hdlineDate.setText(getItem(position).getTimeDuration());
				
				// The fonts
				setFontRegular(textTimeline_hdline);
				setFontRegular(textTimeline_hdlineDate);
				setFontSemiBold(textTimeline_hdlineTitle);
				return v;
			}else return v;
		}
	}

}
