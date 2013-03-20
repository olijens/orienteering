package com.example.orienteering;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.text.format.Time;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.example.orienteering.IntentIntegrator;
import com.example.orienteering.IntentResult;

public class MainActivity extends Activity {
	//TODO manage lifecycle and have a menu item to input server into a variable.
	//Also have the app remember the server name.
	
	public final static String EXTRA_MESSAGE = "com.example.orienteering.MESSAGE";
	public ArrayList<Long> times = new ArrayList<Long>();
	public long starttime;
	public long stoptime;
	public int postnr = 0;
	public ArrayList<String> posts = new ArrayList<String>();
	private String serverName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
	 	Context context = this;
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_WORLD_READABLE);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		System.out.println("Server pref is: " + sharedPref.getString("server", "")); 
		if(!sharedPref.contains("server")){
				alert.setTitle("Server name");
				//alert.setMessage("Server");

				// Set an EditText view to get user input 
				final EditText input = new EditText(this);
				alert.setView(input);
		
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					serverName = input.getText().toString();
					// Do something with value!
				    }
				});
		
				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					// Canceled.
				    }
				});
				alert.show();
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("server", serverName);
				editor.commit();
		} else {
				serverName = sharedPref.getString("server", "");
		}
  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_settings:
					startActivity(new Intent(this, Preferences.class));
					return true;
			default:
					return super.onOptionsItemSelected(item);
		}
	}

	public void takePic(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }
    
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    public static boolean isNumeric(String str) {
    	return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Time time = new Time(Time.getCurrentTimezone());
    	time.setToNow();
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	 	Context context = this;
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_WORLD_READABLE);
		serverName = sharedPref.getString("server", "");

    	if (scanResult != null) {
    	    // handle scan result
    		//Intent intent2 = new Intent(this, DisplayMessageActivity.class);
    		//intent2.putExtra(EXTRA_MESSAGE, scanResult.getContents());
   			//startActivity(intent2);
    		String contents = scanResult.getContents();
    		if (contents != null){
    			if(scanResult.getContents().equals("Start")) {
    				starttime = System.currentTimeMillis();
    				times.add(System.currentTimeMillis());
    				System.out.println(starttime);
    			}
    			else if(scanResult.getContents().equals("Stop")) {
    				stoptime = System.currentTimeMillis();
    				times.add(System.currentTimeMillis());
    				//Intent intent3 = new Intent(this, DisplayMessageActivity.class);
    				//intent3.putExtra(EXTRA_MESSAGE, String.valueOf(stoptime - starttime));
    				//startActivity(intent3);
    				
    				//TODO: Display the results in a ListView on the phone 
    				//Intent intent4 = new Intent(this, DisplayResults.class);
    				//intent4.putExtra(EXTRA_MESSAGE, String.valueOf(stoptime-starttime));
    				
    				//Send the results out to a server (my local server for testing, 192.168.1.67)
    				httpClient data = new httpClient();
					data.execute();
    			    
    				System.out.println(stoptime);
    			}
    			else {
    				times.add(System.currentTimeMillis());
    				postnr += 1;
    				posts.add("Post" + String.valueOf(postnr));
    			}
    		}
    	}
    	// else continue with any other code you need in the method
    }
    
    private class httpClient extends AsyncTask<String, Void, String> {
    	private String content = null;
    	protected String doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();
			//httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false);
			//httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false);
			HttpPost httppost = new HttpPost("http://"+serverName+"/save.php");
			System.out.println("ServerName is: " + serverName);
			httppost.setHeader("Content-Type","application/x-www-form-urlencoded");

    		try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        //nameValuePairs.add(new BasicNameValuePair("id", "text"));
		        nameValuePairs.add(new BasicNameValuePair("text", "Cool"));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        response.getEntity().writeTo(out);
		        out.close();
		        content = out.toString();
		        System.out.println("here is content: " + content);
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("ClientProtocolException");
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("IOException");
		    }
    		
        	return content;
    	}
    }
}
