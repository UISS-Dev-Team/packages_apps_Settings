/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.os.IBinder;
import android.util.Log;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.net.Uri;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.MalformedURLException;


import android.content.SharedPreferences;
/**
 * Activity with the accessibility settings.
 */
public class DownloaderService extends Service {
              
	private MainTask timer;
	   public static int freq = 60;
	   public static String customURL = "http://redcad.org/members/tarak.chaari/testurl.txt";

           @Override
            public IBinder onBind(Intent intent)
           {
                    // TODO Auto-generated method stub
                    return null;
             }

            @Override
             public void onCreate()
           {

                     // Creating service
                      super.onCreate();
                     // Starting service
                      startService();
             }

	     @Override 
    	     public void onDestroy() 
	    {
	      super.onDestroy();
	      stopService();
	    }


		private void startService()
            {        	 
			timer = new MainTask(freq, this);                  
			timer.start();
			Toast.makeText(this, "Downloader Service Started with poll frequency "+freq,Toast.LENGTH_SHORT).show();
             }
	    
	     private void stopService()
	    {
	      if (timer != null) timer.stopPolling();
	      Toast.makeText(this, "Downloader Service Stopped!",Toast.LENGTH_SHORT).show();    
	    }
	    
      
            private class MainTask extends Thread
            { 
			
			private boolean running = true;
			private Context mContext;
						
	                public MainTask(int interval, Context ctx)
			{
				super();
				freq = interval;
				mContext = ctx;
			}
			
			public void stopPolling()
			{
				running =  false;
			}			
			
			public void changeFrequency(int interval)
			{
				
				freq = interval;
				Log.i("DownloaderService", "Poll frequency changed to "+freq);
				// Toast didn't worked, so commented it	
				//Toast.makeText(mContext, "Poll frequency changed to "+freq,Toast.LENGTH_SHORT).show();				
			}
			
			public void run() 
                         {
				while (running)
				{
					try{
						WifiManager wifiManager = (WifiManager) getSystemService("wifi");
						WifiInfo wifiInfo = wifiManager.getConnectionInfo();
						String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
						//String customURL = "http://chart.apis.google.com/chart?cht=qr&chs=300x300&chl="+macAddress;
						URL url = new URL(customURL);
		            			/** Creating an http connection to communcate with url */
		            			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		 	    			/** Connecting to url */
		           			urlConnection.connect();
						if (urlConnection.getResponseCode()==200)
						{
							BufferedReader fromServer = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
							String urlFromServer = fromServer.readLine();
							fromServer.close();
							try{
								URL u = new URL(urlFromServer);
								Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(urlFromServer));
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								mContext.startActivity(i);
							}
							catch(MalformedURLException notUrl)
							{
								//see if it is an interval
								try
								{	int freqFromServer = Integer.parseInt(urlFromServer);
									Log.i("DownloaderService", "about to change frequency to "+freqFromServer);
									if (freqFromServer == 0) stopPolling();
									else changeFrequency(freqFromServer);
								}
								catch(NumberFormatException notInterval)
								{
									//nothing to do in this case.
									Log.i("DownloaderService", "Can't understand response from server");
								}
							}
						}
						Thread.sleep(freq*1000);
					}
					catch(Exception e)
					{
						Log.e("DownloaderService", e.getMessage(), e);
					}
				}
				
                          }
             }
}
