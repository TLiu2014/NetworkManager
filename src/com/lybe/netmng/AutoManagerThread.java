/*
* -------------------------------------------------------
* Copyright (c) 2012 Tianwei Liu
* All rights reserved.
* 
* FileName: AutoManagerThread.java
* Description: class AutoManagerThread
* History:
* 2012.4.11 Tianwei Liu, no other description.
* -------------------------------------------------------
*/
package com.lybe.netmng;
import android.net.TrafficStats;
import android.util.Log;
/**
 * AutoManagerThread: derived from class Thread. 
 * The speed of current network are monitored when the thread runs.
 * If the speed is lower than the threshold value, scan for available WiFi networks automatically.
 * @author : Tianwei Liu
 * @version : 2012.4.11
 */
public class AutoManagerThread extends Thread{
	/**
	 * Define an object of class WifiManagerEx.
	 */
	private WifiManagerEx wifiMngEx= null;
	/**
	 * Speed of current network
	 */
	private double currentSpeed;
	/**
	 * Threshold value of speed: the value is set by users.
	 * When using network in WiFi/GSM mode, if the speed of current network is lower than the threshold value, network would be switched to GSM/WiFi mode.
	 * Unit: kB/s, B/millisecond 
	 */
	private int switchSpeed;
	/**
	 * Measurement interval time: the interval time between twice adjacent and arbitrary measurement of the speed of current network
	 * Unit: millisecond
	 */
	private int speedInterval;
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
	 * Flag of thread running: used to stop current thread.
	 */
	private boolean runFlag;
	/**
	 * Length of observed time
	 */
	private int sumInterval;
	/**
	 * Define tag of class AutoManagerThread
	 */
	private final static String TAG = "AutoManagerThread";
	/**
	Construction function
	@param wifiMngEx: an object of class WifiManagerEx  
	@param entranceSpeed: set by users.
	When using networks in non-WiFi mode, such as in GSM mode, if there is available WiFi network that is faster than the connecting one, connect to it automatically.
	@param exitSpeed: set by users.
	When using networks in WiFi mode, if the speed of current network is lower than the value of "exitSpeed", disconnect current WiFi connection automatically.
	*/
	AutoManagerThread(WifiManagerEx wifiMngEx, int switchSpeed, int interval)
	{
		this.wifiMngEx = wifiMngEx;
		this.switchSpeed = switchSpeed;
		this.interval = interval * 1000;
		//The default value of speedInterval is 30 seconds, for users.
//		speedInterval = 30000;
		//The default value of speedInterval is 1 second, for testing.
		speedInterval = 1000;
		runFlag = true;
	}
	/**
	Rewrite function run of class Thread, define operations when the thread is running.
	*/
	public void run()
	{
		Log.i(TAG, "Thread running...");
		while(runFlag && (speedInterval<=960000))//Interval time between two adjacent measurement is no more than 16 minutes.
		{
			Log.i(TAG, "switchSpeed: "+Integer.valueOf(switchSpeed).toString()+" kB/s(B/ms)");
			Log.i(TAG, "interval: "+Integer.valueOf(interval/1000).toString()+" s");
			Log.i(TAG, "speedInterval: "+Integer.valueOf(speedInterval/1000).toString()+" s");
			//calculate current speed
			speedCalculator();
			sumInterval+=speedInterval;
			Log.i(TAG, "currentSpeed: "+Double.valueOf(currentSpeed).toString()+" kB/s(B/ms)");
			if(this.currentSpeed < this.switchSpeed && runFlag)//The speed of current network is lower than threshold value, switch.
			{
				if(sumInterval < interval)
				{
					Log.i(TAG, "Observing...");
				}
				else
				{
					Log.i(TAG, "Over observation time length. Switch network connection mode...");
					if(!wifiMngEx.isConnect())//Current network mode is GSM
					{
							Log.i(TAG,"connecting to WiFi");
							if(!wifiMngEx.getWifiManager().isWifiEnabled())
								wifiMngEx.openWifi();
							if(runFlag)
								wifiMngEx.connectBest();
							if(!wifiMngEx.isConnect())
								speedInterval*=2;
					}
					else //Current network mode is WiFi
					{
						Log.i(TAG,"disconnecting to WiFi");
						wifiMngEx.closeWifi();
					}
				}
			}
			else //The speed of current network is higher than the threshold value.
			{
				Log.i(TAG,"No need to connect to "+(wifiMngEx.isConnect() ? "GSM" : "WiFi"));
			}	
		}
	}
	/**
	Rewrite function sleep of class Thread, define operations when the thread is sleeping.
	@param time: sleeping duration
	@throws The thread is activated before the sleeping duration runs out.
	*/
	public static void sleep(long time) throws InterruptedException
	{
		Thread.sleep(time);
		Log.i(TAG, "Thread sleeping...");
	}
	/**
	Calculate the speed of current network.
	@return the value of the speed of current network
	*/	
	private void speedCalculator()
	{
		double packetsStart = (double)TrafficStats.getTotalRxBytes();
		try 
		{
			Thread.sleep(speedInterval);
		} catch (InterruptedException e) {}
		currentSpeed = ((TrafficStats.getTotalRxBytes()-packetsStart)/speedInterval);
	}
	/**
	set the value of switchSpeed
	@param switchSpeed
	*/
	public void setSwitchSpeed(int switchSpeed)
	{
		Log.i(TAG, "Method start: setSwitchSpeed");
		this.switchSpeed = switchSpeed;
	}
	/**
	set the value of interval
	@param interval
	*/
	public void setInterval(int interval)
	{
		Log.i(TAG, "Method start: setInterval");
		this.interval = interval * 1000;
	}
	/**
	Stop the thread running.
	*/
	public void stopRun()
	{
		Log.i(TAG, "Thread stop");
		runFlag = false;
	}
}
