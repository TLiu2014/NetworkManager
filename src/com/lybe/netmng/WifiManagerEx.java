/*
* -------------------------------------------------------
* Copyright (c) 2012 Tianwei Liu
* All rights reserved.
* 
* FileName: WifiManagerEx.java
* Description: class WifiManagerEx
* History: 
* 2012.4.12 Tianwei Liu, no other description.     
* -------------------------------------------------------
*/
package com.lybe.netmng;
import java.util.List;

import com.phone.energymng.netmng.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * WifiManagerEx: extends functions of class WiFiManager
 * @author : Tianwei Liu
 * @version : 2012.4.12
 */
public class WifiManagerEx {
	/**
	Define an object of class WifiManager
	*/
	private WifiManager wifiManager;
	/**
	Define an object of the Context
	*/
	private Context context;
	/**
	Define a list of available WiFi networks after scanning.
	*/
	private List<ScanResult> listScanResult;
	/**
	Define a list of remembered WiFi networks after scanning.
	*/
	private List<WifiConfiguration> listWifiCfg;
	/**
	Flag: Whether the device is connecting through WiFi, if so, return true, otherwise return false.
	 */
	private boolean isConnectFlag;
	/**
	The object of class ScanResult with the strongest signal after scanning for available WiFi networks.
	 */
	private ScanResult bestScanResult = null;
	/**
	The tag for class WifiManagerEx
	*/
	private static final String TAG = "WifiManagerEx";
	/**
	A static value used in messages sent to the BroadcastReceiver 
	indicates no available WiFi networks with a strong signal.
	*/
	private static final int DIALOG_NULL = 0;
	/**
	A static value used in messages sent to the BroadcastReceiver 
	indicates the WiFi network with the strongest signal is not remembered by the device.
	*/
	private static final int DIALOG_NOT_CONFIG = 1;
	/**
	A static value used in messages sent to the BroadcastReceiver 
	indicates the WiFi network with the strongest signal has been remembered by the device.
	*/
	private static final int DIALOG_CONFIG = 2;
	/**
	Construction function
	@param context: current context of application, to get the value of wifiManager
	*/
	public WifiManagerEx(Context context) 
	{
		this.wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		this.context = context;
		this.listWifiCfg = wifiManager.getConfiguredNetworks();
		isConnectFlag = false;
		Log.i(TAG, "WifiManagerEx Created");
	}
	/**
	Obtain the best available WiFi network, the one with the strongest signal
	@return the best available WiFi network,, if no available network, return null.
	*/
	private ScanResult getBestScanResult()
	{
		Log.i(TAG, "Method start: getBestScanResult");
	    ScanResult bestSignal = null;
	    if(listScanResult == null)
	    {
	    	Log.i(TAG, "listScanResult is null!");
	    	Log.i(TAG, "Method finish: getBestScanResult");
	    	return null;
	    }
	    else
	    {
	    	for (ScanResult result : listScanResult) 
		    {
		      if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level,result.level)<0)
		        bestSignal = result;
		    }
		    Log.i(TAG, "Method finish: getBestScanResult");
		    return bestSignal;
	    }
	}
	/**
	Obtain the configuration info of the best available WiFi network,
	Conditions: 1. The list of available networks after scanning is not null; 
	2. The list of remembered networks after scanning is not null;
	@return The configuration info of the best available WiFi network,
	If the best network is not remembered, return null.
	*/
	private WifiConfiguration getBestWcfg()
	{
		Log.i(TAG, "Method start: getBestWcfg");
		WifiConfiguration bestSignal = null;
		if(bestScanResult == null)
		{
			Log.i(TAG, "bestScanResult is null!");
			Log.i(TAG, "Method finish: getBestWcfg");
			return null;
		}
		else
		{
			if(listWifiCfg == null)
			{
				Log.i(TAG, "listWifiCfg is null!");
				Log.i(TAG, "Method finish: getBestWcfg");
				return null;
			}
			else
			{
				for (WifiConfiguration result : listWifiCfg) 
			    {
					if (result.SSID.equals("\""+bestScanResult.SSID+"\""))
					{
						bestSignal = result;
						break;						
					}
			    }
			    Log.i(TAG, "Method finish: getBestWcfg");
			    return bestSignal;
			}
		}
	}
	/**
	Connect to the best available WiFi network, the one with the strongest signal.
	*/
	public void connectBest(){
		Log.i(TAG, "Method start: connectBest");
		Intent intent = new Intent();
		//Scan for WiFi networks.
		this.scan();
		//Obtain scanning result, the list of available WiFi networks.
		listScanResult = wifiManager.getScanResults();
		//Obtain the list of remembered WiFi networks.
		listWifiCfg = wifiManager.getConfiguredNetworks();
		//Obtain the object of class ScanResult with the strongest signal after scanning for available WiFi networks.
		bestScanResult = getBestScanResult();
		if(listScanResult == null)//No WiFi networks available
		{
			Log.i(TAG, "listScanResult is null!");
			intent.putExtra("dialog", DIALOG_NULL);
		}
		else//Available WiFi networks exist.
		{
			WifiConfiguration bestWcfg = null;
			if(listWifiCfg == null)//If the list of remembered WiFi networks is empty, prompt the corresponding dialog.
			{
				Log.i(TAG, "listWifiCfg is null!");	
				intent.putExtra("dialog", DIALOG_NULL);	
				Log.i(TAG, "Method finish: connectBest");
			}
			else//The list of remembered WiFi networks is not empty.
			{
				Resources r = context.getResources();
				bestWcfg = getBestWcfg();
				if(bestWcfg == null)//If the best WiFi network is not in the list of remembered WiFi networks, prompt the corresponding dialog.
				{
					Log.i(TAG, "bestWcfg not exist!");	
					intent.putExtra("dialog", DIALOG_NOT_CONFIG);
					if(bestScanResult == null)
						intent.putExtra("message","\nSSID: "+(String)r.getString(R.string.unknown)
								+"\nBSSID: "+(String)r.getString(R.string.unknown));
					else
						intent.putExtra("message","\nSSID: "+bestScanResult.SSID+"\nBSSID: "+bestScanResult.BSSID);
					Log.i(TAG, "Method finish: connectBest");
				}
				else//If the best WiFi network is in the list of remembered WiFi networks, connect to it and prompt the corresponding dialog.
				{
					wifiManager.enableNetwork(bestWcfg.networkId, true);
					wifiManager.reconnect();
					isConnectFlag = true;	
					Log.i(TAG, "bestWiFiConfiguration connect succeed!");	
					intent.putExtra("dialog", DIALOG_CONFIG);
					if(wifiManager.getConnectionInfo() == null)
						intent.putExtra("message","\nSSID: "+(String)r.getString(R.string.unknown)
								+"\nBSSID: "+(String)r.getString(R.string.unknown));
					else 
						intent.putExtra("message","\nSSID: "+wifiManager.getConnectionInfo().getSSID()
								+"\nNetworkId: "+bestWcfg.networkId);
					Log.i(TAG, "Method finish: connectBest");
				}
			}
		}
		intent.setAction("dialog_show");
		context.sendBroadcast(intent);
	}
	/**
	Whether the device is connecting through WiFi.
	@return If the device is connecting through WiFi, return true, otherwise return false.
	*/
	public boolean isConnect()
	{
		return isConnectFlag;
	}
	/**
	Get the object of class WifiManager
	@return the object of class WifiManager
	*/
	public WifiManager getWifiManager()
	{
		return wifiManager;
	}
	/**
	Turn on WiFi
	 */
	public void openWifi() 
	{
		if (!wifiManager.isWifiEnabled()) 
		{
			wifiManager.setWifiEnabled(true);
		}
	}
	/**
	Turn off WiFi
	 */
	public void closeWifi() 
	{
		if (wifiManager.isWifiEnabled()) 
		{
			wifiManager.setWifiEnabled(false);
			isConnectFlag = false;
		}
	}
	/**
	Scan for available WiFi networks.
	 */
	public void scan() 
	{
		Log.i(TAG, "Method start: scan");
		wifiManager.startScan();
		listScanResult = wifiManager.getScanResults();
		if (listScanResult != null) 
		{
			Log.i(TAG, "WiFi available");
		} 
		else 
		{
			Log.i(TAG, "No WiFi available");
		}
		Log.i(TAG, "Method finish: scan");
	}
}
