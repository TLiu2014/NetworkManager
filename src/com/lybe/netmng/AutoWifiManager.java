/*
* -------------------------------------------------------
* Copyright (c) 2012 Tianwei Liu
* All rights reserved.
* 
* FileName: AutoWifiManager.java
* Description: class AutoWifiManager
* History: 
* 2012.3.24 Tianwei Liu, no other description.     
* -------------------------------------------------------
*/
package com.lybe.netmng;
import android.util.Log;
/**
 * AutoWifiManager: class for automatic management of WiFi
 * @author : Tianwei Liu 
 * @version : 2012.4.7
 */
public class AutoWifiManager {
	/**
	 * Define an object of class WifiManagerEx, used for operations of WiFi management.
	 */
	private WifiManagerEx wifiMngEx= null;
	/**
	 * Define an object of class AutoManagerThread, used for operations of monitoring networks and automatic management.
	 */
	private AutoManagerThread autoMngThread = null;
	/**
	 * flag: Whether the automatic networks management is on or not. 
	 */
	private boolean isAutoFlag;
	/**
	 * Threshold value of speed: the value is set by users.
	 * When using network in WiFi/GSM mode, if the speed of current network is lower than the threshold value, network would be switched to GSM/WiFi mode.
	 * Unit: kB/s, B/millisecond 
	 */
	private int switchSpeed;
	/**
	 * Observation time: the value is set by users.
	 * If the speed of current network is lower than the threshold time, 
	 * but lasts no longer than the value of observation time, 
	 * keep on observing, 
	 * and switch network mode when it lasts longer than observation time.
	 * Unit: millisecond
	 */
	private int interval;
	/**
	 * The tag for class AutoWifiManager
	 */
	private final static String TAG = "AutoWifiManager";
	/**
	Construction function
	@param wifiMngEx: an object of class WifiManagerEx
	*/
	public AutoWifiManager(WifiManagerEx wifiMngEx)
	{
		this.wifiMngEx = wifiMngEx;
		isAutoFlag = false;
    	Log.i(TAG, "AutoWifiManager Created");
	}
	/**
	set the value of switchSpeed
	@param switchSpeed
	*/
	public void setSwitchSpeed(int switchSpeed)
	{
		Log.i(TAG, "Method start: setSwitchSpeed");
		this.switchSpeed = switchSpeed;
		if(autoMngThread!=null)
			this.autoMngThread.setSwitchSpeed(switchSpeed);
	}
	/**
	set the value of interval
	@param interval
	*/
	public void setInterval(int interval)
	{
		Log.i(TAG, "Method start: setInterval");
		this.interval = interval;
		if(autoMngThread!=null)
			this.autoMngThread.setInterval(interval);
	}
	/**
	Whether the automatic networks management is on or not. 
	@return If the automatic networks management is on, return true, otherwise return false. 
	*/
	public boolean isAuto()
	{
		Log.i(TAG, "Method start: isAuto");
		return isAutoFlag;
	}
	/**
	set the value of "auto", the flag for Whether the automatic networks management is on or not. 
	@param auto 
	*/
	private void setAuto(boolean auto)
	{
		Log.i(TAG, "Method start: setAuto");
		this.isAutoFlag=auto;
	}
	/**
	Switch on/off automatic networks management.
	*/
	public void switchAuto()
	{
		Log.i(TAG, "Method start: switchAuto");
		setAuto(!isAutoFlag);
		if(isAutoFlag) onAuto();
		else offAuto();
	}
	/**
	Switch on automatic networks management.
	*/
	public void onAuto()
	{
		Log.i(TAG, "Method start: onAuto");
		setAuto(true);
		autoMngThread = new AutoManagerThread(wifiMngEx, switchSpeed, interval);
		autoMngThread.start();		
	}
	/**
	Switch off automatic networks management.
	*/
	public void offAuto()
	{
		Log.i(TAG, "Method start: offAuto");
		setAuto(false);
		autoMngThread.stopRun();
	}
}
