package com.android.launcher2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class HomescreenConfigurationReceiver extends BroadcastReceiver {

	public static final String OPTIONS_TYPE = "type";
	public static final String OPTIONS_TITLE = "title";//Not used
	public static final String OPTIONS_PACKAGE_NAME = "packageName";
	public static final String OPTIONS_CLASS_NAME = "className";
	public static final String OPTIONS_CONTAINER = "container";
	public static final String OPTIONS_SCREEN = "screen";
	public static final String OPTIONS_X = "x";
	public static final String OPTIONS_Y = "y";
	public static final String OPTIONS_ROWS = "rows";
	public static final String OPTIONS_COLUMNS = "cols";
	
	/**
	 * The protocol version of this service is incorrect
	 */
	public static final String RESULT_INVALID_VERSION = "RESULT_INVALID_VERSION";
	
	public static final String RESULT_MISSING_REQUIRED_PARAMETER = "RESULT_MISSING_REQUIRED_PARAMETER";
	
	public static final String RESULT_INVALID_JSON = "RESULT_INVALID_JSON";

	/**
	 * Unknown email failure
	 */
	public static final String RESULT_UNKNOWN = "RESULT_UNKNOWN";

	/**
	 * Email account creation success
	 */
	public static final int RESULT_CODE_SUCCESS = 0x0;

	/**
	 * Email account creation failure
	 */
	public static final int RESULT_CODE_FAILURE = 0x1;

	private static final Float SUPPORTED_VERSION = 1.0f;//TODO: supported version


	@Override
	public void onReceive(Context context, Intent intent) {
		List<Bundle> bundles = null;
		if (intent.hasExtra("homescreen")) {
			bundles = fromIntentToBundles(intent);
			if(bundles.isEmpty()) {

			}
		} else {		    
			sendResponse(context, null, true);
			return;
		}

			
		boolean hasError = false;
		
		JSONArray messages = new JSONArray();
				
		for(Bundle options : bundles) {
			if (hasRequiredOptions(options)) {
				int containerType = options.getInt(OPTIONS_CONTAINER) > 0 ? LauncherSettings.Favorites.CONTAINER_DESKTOP
						: LauncherSettings.Favorites.CONTAINER_HOTSEAT;

				installWidget(context, intent, options.getInt(OPTIONS_SCREEN, -1),
						containerType, options.getInt(OPTIONS_X, -1),
						options.getInt(OPTIONS_Y, -1),
						options.getInt(OPTIONS_ROWS, -1),
						options.getInt(OPTIONS_COLUMNS, -1), true);
				messages.put(buildResponse(options, true, 0, "OK"));
			} else {
				hasError = true;
				messages.put(buildResponse(options, true, 0, RESULT_MISSING_REQUIRED_PARAMETER));
			}			
		}
		
		sendResponse(context, messages, hasError);		
	}
	
	private void sendResponse(Context context, JSONArray items, boolean isSuccess) {
		Intent result = new Intent(
				"com.android.homescreen.CONFIGURE_HOMESCREEN_RESULT");
		result.putExtra("success", isSuccess);		
		result.putExtra("version", "1.0");//TODO - hard-coded
		result.putExtra("intent", "com.android.homescreen.CONFIGURE_HOMESCREEN");
		
		if(items != null) {
			Log.d("Homescreen", items.toString());
			result.putExtra("homescreen", items.toString());		
		} else {
			result.putExtra("errorCode", 0);
			result.putExtra("errorMessage", RESULT_MISSING_REQUIRED_PARAMETER);			
		}
		
		context.sendBroadcast(result);
	}
	
	private JSONObject buildResponse(Bundle options, boolean isSuccess, int errorCode, String errorMessage) {
		
		try {
			JSONObject payload = new JSONObject(options.getString("jsonObject"));
			payload.put("success", (isSuccess ? RESULT_CODE_SUCCESS : RESULT_CODE_FAILURE) );
			payload.put("errorCode", errorCode);
			payload.put("errorMessage", errorMessage);
			return payload;
		} catch (JSONException e) {
			
		}
		return null;
	}

	private boolean hasRequiredOptions(Bundle options) {
		boolean hasOptions = options.containsKey(OPTIONS_TYPE)
				&& options.containsKey(OPTIONS_PACKAGE_NAME)
				&& options.containsKey(OPTIONS_CLASS_NAME)
				&& options.containsKey(OPTIONS_CONTAINER);
		
		if(hasOptions && options.getInt(OPTIONS_CONTAINER) > 0 ) {
			 return options.containsKey(OPTIONS_SCREEN);
		}
		
		return hasOptions;
	}
	
	private List<Bundle> fromIntentToBundles(Intent intent) {
		try {
			JSONArray bundledArray = new JSONArray(
					intent.getStringExtra("homescreen"));
			
			// TODO: better to use homescreens
			if (bundledArray != null) {
			    return fromJsonToBundles(bundledArray);  
			}			
			
		} catch (JSONException e) {		    
		    e.printStackTrace();
		}		
		return new ArrayList<Bundle>();
	}
	private List<Bundle> fromJsonToBundles(JSONArray items) {
		ArrayList<Bundle> bundles = new ArrayList<Bundle>();
	
	        try {
	            for ( int index = 0; index < items.length(); index++ ) {
	                JSONObject item = items.getJSONObject( index );
	                Bundle bundle = new Bundle();
	                
	                bundle.putString("jsonObject", item.toString());
	                addOptionString(OPTIONS_TYPE, item, bundle);
	                addOptionString(OPTIONS_PACKAGE_NAME, item, bundle);                
	                addOptionString(OPTIONS_CLASS_NAME, item, bundle);
	                
	                addOptionInt(OPTIONS_CONTAINER, item, bundle);
	                addOptionInt(OPTIONS_SCREEN, item, bundle);
	                addOptionInt(OPTIONS_X, item, bundle);
	                addOptionInt(OPTIONS_Y, item, bundle);
	                addOptionInt(OPTIONS_ROWS, item, bundle);
	                addOptionInt(OPTIONS_COLUMNS, item, bundle);
	                
	                bundles.add(bundle);
	            }
	        } catch ( JSONException je ) {
	        	
	        }

		return bundles;
		
	}
	
	private void addOptionString(String option, JSONObject item, Bundle bundle ) {
        if(!item.isNull(OPTIONS_TYPE)) {
        	try {
				bundle.putString(OPTIONS_TYPE, item.getString(OPTIONS_TYPE));
			} catch (JSONException e) {
		
			}
        }     
	}

	private void addOptionInt(String option, JSONObject item, Bundle bundle ) {
        if(!item.isNull(OPTIONS_TYPE)) {
        	try {
				bundle.putInt(OPTIONS_TYPE, item.getInt(OPTIONS_TYPE));
			} catch (JSONException e) {
		
			}
        }     
	}

	private LauncherAppWidgetInfo installWidget(Context context, Intent data, int screen,
			int container, int xCoOd, int yCoOd, int spanX, int spanY,
			boolean notify) {
		LauncherApplication app = (LauncherApplication) context
				.getApplicationContext();
		return app.getModel().addAppWidget(context, data,
				container, screen, xCoOd, yCoOd, spanX, spanY, true);
	}
}
