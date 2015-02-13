package com.app.chasebank;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.app.chasebank.entity.MDepartment;
import com.app.chasebank.entity.MyCompany;
import com.app.chasebank.framework.Act;
import com.app.chasebank.framework.AppController;
import com.app.chasebank.util.CommonUtils;
import com.app.chasebank.util.DatabaseHelper;
import com.app.chasebank.util.MCompanyAdapter;
import com.app.chasebank.util.MDepartAdapter;
import com.app.chasebank.util.Rounder;

public class SignUpActivity extends Act {
	public static ArrayList<MyCompany> companyList = new ArrayList<MyCompany>();
	private DatabaseHelper helper = null;
	
	private EditText textName;
	private TextView submit_btn;
	private ImageView signup_photo;
	private AsyncTask<Void, Void, Void> mSetProfile;
	private Spinner spinnerCompany;
	private MCompanyAdapter adap;	
	
	final static int REQUEST_CODE = 0;
	// keep track of camera capture intent
	final int CAMERA_CAPTURE = 3;
	private String selectedImagePath;
	private Uri selectedImageUri;
	// keep track of cropping intent
	final int PIC_CROP = 2;
	private Spinner department;
	protected MyCompany selectedCompany;
	private List<MDepartment> depts;
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.activity_signup);
		
		// TODO Set the font
		setFontSemiBold(((TextView)findViewById(R.id.signup_title)));
		setFontRegular(((TextView)findViewById(R.id.dialog_txt)));
		setFontRegular(((EditText)findViewById(R.id.user_name)));
		setFontRegular(((TextView)findViewById(R.id.user_submit_lead)));
		setFontRegular(((TextView)findViewById(R.id.textViewTerms)));
		
		initUI();
		getCompanies();
	}
	
	public void changeProfilePhoto(View v){
		// TODO Upload and select an image for the profile photo
		Intent imageGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		imageGalleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
		imageGalleryIntent.setType("image/*");
		// Intent 
		final Intent captureIntent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		Intent chooserIntent = Intent.createChooser(imageGalleryIntent, "Image Chooser");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] { captureIntent });
		startActivityForResult(chooserIntent, REQUEST_CODE);
	}
	
	public String getRealPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		try {
			switch (requestCode) {
			case REQUEST_CODE:
				if (android.os.Build.VERSION.SDK_INT < 13) {
					selectedImageUri = data.getData();
					// case: android 2.3 va chup hinh tu camera
					if (selectedImageUri == null) {
						final ContentResolver cr = getContentResolver();
						final String[] p1 = new String[] {
								MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN };
						Cursor c1 = cr.query(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");
						if (c1.moveToFirst()) {
							String uristringpic = "content://media/external/images/media/" + c1.getInt(0);
							Uri newuri = Uri.parse(uristringpic);
							selectedImageUri = newuri;
							Log.i("TAG", "newuri   " + newuri);
							signup_photo.setImageURI(newuri);
							performCrop();
							return;
						}
					}
				}

				if (resultCode == Activity.RESULT_OK) {

					// user is returning from capturing an image using the camera					
					if (requestCode == CAMERA_CAPTURE) {
						// get the Uri for the captured image
						selectedImageUri = data.getData();
						// carry out the crop operation
						performCrop();
					}
					
					// user is returning from cropping the image
					else if (requestCode == PIC_CROP) {
						// get the returned data
						Bundle extras = data.getExtras();
						// get the cropped bitmap
						Bitmap thePic = extras.getParcelable("data");
						// display the returned cropped image
						signup_photo.setImageBitmap(thePic);
						return;
					}

					// data gives you the image uri. Try to convert that to bitmap
					selectedImageUri = data.getData();
					selectedImagePath = getRealPath(selectedImageUri);
					
					try {
						signup_photo.setImageURI(selectedImageUri);
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
					
					performCrop();
					break;
				} else if (resultCode == Activity.RESULT_CANCELED) {
				}
				break;
				
			case PIC_CROP:
				if (resultCode == Activity.RESULT_OK) {
					// get the returned data
					Bundle extras = data.getExtras();
					// get the cropped bitmap
					Bitmap thePic = extras.getParcelable("data");
					
					// display the returned cropped image
					Uri cropImage = getImageUri(getBaseContext(), thePic);
					selectedImagePath = getRealPath(cropImage);
					Bitmap bitmap = Rounder.getRoundedShape(selectedImagePath, 256, 256);
					//img.setImageBitmap(thePic);
					signup_photo.setImageBitmap(bitmap);
					
					//btn.setVisibility(View.GONE);
					signup_photo.setVisibility(View.VISIBLE);
					signup_photo.setBackgroundResource(R.drawable.signup_shape);
					//btn.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
					
				} else if (resultCode == Activity.RESULT_CANCELED) {
					// Log.e(TAG, "Selecting picture cancelled");
				}
				break;

			}
		} catch (Exception e) {
			Log.e("error 1", "Exception in onActivityResult : " + e.getMessage());
		}
	}

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		return Uri.parse(path);
	}

	private void performCrop() {
		try {
			// call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(selectedImageUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	/**
	 * Get the list of companies from the downloaded data
	 */
	public void getCompanies(){
		getDepartments();
		getAllCompanies();
	}
	
	public void initUI() {
		textName = (EditText) findViewById(R.id.user_name); 
		submit_btn = (TextView) findViewById(R.id.user_submit_lead);
		signup_photo = (ImageView)findViewById(R.id.signup_photo);
		
		spinnerCompany = (Spinner) findViewById(R.id.user_company);
		department = (Spinner) findViewById(R.id.user_department);
		
		adap = new MCompanyAdapter(SignUpActivity.this, R.layout.company_spinner_item, companyList);		
		spinnerCompany.setAdapter(adap);
		
		spinnerCompany.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				MyCompany cmp = companyList.get(spinnerCompany.getSelectedItemPosition());
				if(cmp != null){
					List<MDepartment> dpt = MDepartment.sort(depts, cmp.getId());
					if(dpt != null) department.setAdapter(new MDepartAdapter(SignUpActivity.this, dpt));
				}	
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
			
		});
		
		//select index 0
		if(companyList.size() > 0) spinnerCompany.setSelection(0,  true);	
		
		/**
		 * Onclick Listener
		 */
		OnClickListener notilist = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/**
				 * Submit the content to the server, then close this dialog
				 */
				MDepartment departStr = null;
								
				if(textName.getEditableText().toString().equalsIgnoreCase("")) {
					//When name is null
					if(textName.getEditableText().toString().equalsIgnoreCase(""))
						textName.setError("name required!");	
				}else {
					selectedCompany = adap.getItem(spinnerCompany.getSelectedItemPosition());
					String nameStr = textName.getEditableText().toString();
					String departs = "";
					
					try {
						departStr = ((MDepartAdapter)department.getAdapter()).getItem(department.getSelectedItemPosition());
						departs = departStr.getId();
					}catch(Exception ex) {
						departs = "";								
					}
						
					if(selectedImagePath != null && !selectedImagePath.equals("")) {
						setProfile(getUserId(), nameStr, departStr);
					}else {
						toast("Sorry, you need to select a profile photo..!");
					}
				}
			}
		};		
		submit_btn.setOnClickListener(notilist);
	}
	
	/**
	 * Set the current user's profile
	 * 
	 * @param id User's ID 
	 * @param first_name User's First Name
	 * @param last_name User's Last Name
	 * @param company User's Affiliated Company
	 * @param phone_number User's Phone number
	 * @param department User's Department
	 */
	private void setProfile(final String id, final String first_name, final MDepartment department) {		
		mSetProfile = new AsyncTask<Void, Void, Void>() {
			boolean profileSet; 

			@Override
			protected void onPreExecute() {
				super.onPreExecute();	
				showDialog(true);
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				try {
					profileSet = CommonUtils.setProfile(id, first_name, selectedCompany.getId(), "", department.getId(), selectedImagePath);					
				} catch (Exception e) {
					e.printStackTrace();
					profileSet = false;
				}				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {	
				showDialog(false);
				if(profileSet) {
					setFirstLogin();
					//TODO Lets jump this step, see what happens
					setProfilePhoto(selectedImagePath);
					
					//TODO Save [name, companyName, companyID]
					setName(first_name);
					setCompanyName(selectedCompany.getName());
					setCompanyID(selectedCompany.getId());
					setDeptName(department.getName());
					SignUpActivity.this.finish();
					startActivity(new Intent(SignUpActivity.this, ProfileAct.class));
				}else {
					toast("Sorry, could not set your profile, please try again later..");
				}
				mSetProfile = null;
			}
			
		};
		
		mSetProfile.execute(null, null, null);
	}
	
	private void getAllCompanies(){
		String  serverUrl = CommonUtils.SERVER_URL +"companies.json";
		
		Cache cache = AppController.getInstance().getRequestQueue().getCache();
		Entry entry = cache.get(serverUrl);
		if(entry != null){
			try {
				String data = new String(entry.data, "UTF-8");
				JSONArray result = new JSONArray(data);
				processCompanies(result);
				
			} catch (UnsupportedEncodingException e) {      
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else{
			// Tag used to cancel the request
			String tag_json_obj = "json_obj_req";	
			JsonArrayRequest jsonReg = new JsonArrayRequest(serverUrl, 
					new Response.Listener<JSONArray>() {
				
				@Override
				public void onResponse(JSONArray result) {
					Log.d(TAG, result.toString());
					processCompanies(result);
					
				}
			}, 
			new Response.ErrorListener() {
				
				@Override
				public void onErrorResponse(VolleyError error) {
					VolleyLog.d(TAG, "Error: " + error.getMessage());
					
				}
			});
			
			// Adding request to request queue
			AppController.getInstance().addToRequestQueue(jsonReg, tag_json_obj);
		}
	}
	
	private void getDepartments(){
		String  serverUrl = CommonUtils.SERVER_URL +"departments.json";
		
		Cache cache = AppController.getInstance().getRequestQueue().getCache();
		Entry entry = cache.get(serverUrl);
		if(entry != null){
			try {
				String data = new String(entry.data, "UTF-8");
				Log.i(TAG, ": "+ data);
				JSONArray result = new JSONArray(data);
				processDepartments(result);
				
			} catch (UnsupportedEncodingException e) {      
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			// Tag used to cancel the request
			String tag_json_obj = "json_obj_req";	
			JsonArrayRequest jsonReg = new JsonArrayRequest(serverUrl, 
					new Response.Listener<JSONArray>() {
				
				@Override
				public void onResponse(JSONArray result) {
					Log.d(TAG, result.toString());
					processDepartments(result);
					
				}
			}, 
			new Response.ErrorListener() {
				
				@Override
				public void onErrorResponse(VolleyError error) {
					VolleyLog.d(TAG, "Error: " + error.getMessage());
					
				}
			});
			
			// Adding request to request queue
			AppController.getInstance().addToRequestQueue(jsonReg, tag_json_obj);
		}
	}

	protected void processCompanies(JSONArray result) {
		ArrayList<MyCompany> comp = CommonUtils.getMCompanies(result);
		companyList.clear();
		companyList.addAll(comp);
		
		adap = new MCompanyAdapter(SignUpActivity.this, R.layout.company_spinner_item, companyList);
		spinnerCompany.setAdapter(adap);
		spinnerCompany.setSelection(0);
	}
	
	protected void processDepartments(JSONArray result) {
		if(result != null) Log.i(TAG, result.toString());
		if(depts != null) depts.clear();
		depts = CommonUtils.getDepartments(result);
	}
}
