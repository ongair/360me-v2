package com.app.chasebank.push;

import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.app.chasebank.LeadActivity;
import com.app.chasebank.ProfileAct;
import com.app.chasebank.R;
import com.app.chasebank.util.NotiMessage;
import com.infobip.push.AbstractPushReceiver;
import com.infobip.push.PushNotification;

public class ChasePushReceiver extends AbstractPushReceiver {
	
	private static final String TAG = "ChasePushReceiver";
    public static final int NOTIFICATION_ID = 1;
       
	@Override
	public void onRegistered(Context context) {
		/**
		 * Registration successful
		 */
		Log.i(TAG, "onRegistered: ");
		Toast.makeText(context, "Successfully registered.", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onNotificationReceived(PushNotification notification, Context context) { 
		String notis = notification.getAdditionalInfo();		
		
		/**
		 * Notification Successful
		 */
		Log.i(TAG, "onNotificationReceived-AdditionalInfo: "+notification.getAdditionalInfo());
		try {
			JSONObject obj = new JSONObject(notis);
			if(obj.has("notification_type") && obj.get("notification_type").equals("MilesAwarded")){
				String reason = obj.getString("reason");
				String miles = obj.getString("miles");
				String totalMiles = obj.getString("total_miles");
				sendNotification(context, reason, miles, totalMiles);
			}
			else if(obj.has("notification_type") && obj.get("notification_type").equals("NewLead")){
				String user_id = obj.getString("user_id");
				String lead_id = obj.getString("lead_id");
				String company_id = obj.getString("company_id");
				String product_id = obj.getString("product_id");
				String lead_name = obj.getString("lead_name");
				String lead_email = obj.getString("lead_email");
				String lead_phone_number = obj.getString("lead_phone_number");
				
				sendAssignedNotification(context, lead_id, company_id, product_id, lead_name, lead_email, lead_phone_number);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Show a Notification on the screen.
	 * @param context Context
	 * @param reason The reason for the award
	 * @param miles 
	 * @param id
	 * @param totalMiles
	 */
	private void sendNotification(Context context, final String reason, final String miles, final String totalMiles) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent intent = null;
        intent = new Intent(context, ProfileAct.class);
        
        Bundle bun = new Bundle();
        bun.putString("miles", miles);
        bun.putCharSequence("total", totalMiles);
        intent.putExtras(bun);
        
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        
        NotiMessage notis = new NotiMessage("360 Miles Points", "You have been awarded <strong>"+miles+" points</strong> for "+reason+". " +
        		"You new 360 Miles Total Points is: </strong>"+totalMiles+"</strong>", 
        						"0 mins ago.");
        try { new com.app.chasebank.util.DatabaseHelper(context).createNotification(notis); }catch(Exception ex){ex.printStackTrace(); }
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setContentTitle("360 Me")
	        .setTicker(miles+" for "+reason)
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText("You have been awarded "+miles+" points for "+reason+". You new 360 Miles Total Points is: "+totalMiles))
        .setContentText("You have been awarded "+miles+" points for "+reason+". You new 360 Miles Total is: "+totalMiles)
        .setLights(Color.RED, 2000, 1000)
        .setVibrate(new long[]{100, 200, 100, 300});
        
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
                
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

	/**
	 * Send A Notification for a lead that has been assigned.
	 * @param context	Context
	 * @param leadId Lead Id
	 * @param companyID Company ID
	 * @param productID Product ID
	 * @param leadName	Lean Name
	 * @param leadEmail Lead Email address
	 * @param leadPhone lead phone number
	 */
	 
	private void sendAssignedNotification(Context context, String leadId, String companyID, String productID, String leadName,
			String leadEmail, String leadPhone) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent intent = null;
        intent = new Intent(context, LeadActivity.class);
        
        Bundle bun = new Bundle();
        bun.putString("type", "notification");
        bun.putString("leadId", leadId);
        bun.putCharSequence("companyID", companyID);
        bun.putCharSequence("productID", productID);
        bun.putCharSequence("leadName", leadName);
        bun.putCharSequence("leadEmail", leadEmail);
        bun.putCharSequence("leadPhone", leadPhone);
        intent.putExtras(bun);
        
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        
        NotiMessage notis = new NotiMessage("New Lead Assigned", "Hey, you've been assigned a new lead. " +
        		"<br />Name: <strong>"+leadName+"</strong>, " +
        				"Email: <strong>"+leadEmail+"</strong> ;Phone: <strong>"+leadPhone+"</strong>", 
        						"0 mins ago.");
        try { new com.app.chasebank.util.DatabaseHelper(context).createNotification(notis); }catch(Exception ex){ex.printStackTrace(); }
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setContentTitle("360 Me")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText("Hey, you've been assigned a new lead. Name: "+leadName+", email: "+leadEmail))
	        .setContentText("Hey, you've been assigned a new lead. Name: "+leadName+", email: "+leadEmail)
        .setLights(Color.RED, 2000, 1000)
        .setVibrate(new long[]{100, 200, 100, 300});
        
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
                
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
	
	@Override
	public void onUnregistered(Context context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int reason, Context context) {
		// TODO Auto-generated method stub
		
	}
	
}
