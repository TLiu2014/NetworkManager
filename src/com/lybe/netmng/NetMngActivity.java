/*
* -------------------------------------------------------
* Copyright (c) 2012 Tianwei Liu
* All rights reserved.
* 
* FileName: NetMngActivity.java
* Description: class NetMngActivity
* History: 
* 2012.5.16 Tianwei Liu, no other description.     
* -------------------------------------------------------
*/
package com.lybe.netmng;

import com.phone.energymng.netmng.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * NetMngActivity: derived from class Activity, the main GUI
 * @author : Tianwei Liu
 * @version : 2012.5.16
 */
public class NetMngActivity extends Activity {
    /**
	The button to submit the threshold value of speed
	*/
    private Button submitSpeedBtn = null;
    /**
	The button to submit the value of observation time
	*/
    private Button submitIntervalBtn = null;
    /**
	The Test Editor to set the threshold value of speed 
	*/
    private EditText editSwitchSpeed = null;
    /**
	The Test Editor to set the value of observation time 
	*/
    private EditText editInterval = null;
	private TextView tvSSTitle = null;
	private TextView tvITitle = null;
	private TextView tvExit = null;
	private CheckBox cbSwi = null;
	private CheckBox cbRib = null;
	private AlertDialog.Builder dialog_ss = null;
	private AlertDialog.Builder dialog_i = null;
    /**
	The dialog prompts no available WiFi networks.
	 */
	private AlertDialog.Builder dialog_not_cfg = null;
	/**
	The dialog prompts that the device has connected to some assigned WiFi network in the list.
	 */
	private AlertDialog.Builder dialog_cfg = null;
    /**
	 * Define an object of class WifiManagerEx, used for operations of WiFi management.
	 */  
    private WifiManagerEx wifiMngEx= null;
    /**
	 * Define an object of class AutoWifiManager, used for operations of automatic management.
	 */
	private AutoWifiManager auto = null;	
	/**
	 * A BroadcastReceiver to prompt dialogs
	 */
	private BroadcastReceiver broadcastReceiver = null;
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
     * Rewrite function onCreate, called when the object of class NetMngActivity created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
        mngInit();
        attrInit();
        dialogInit();
        bcInit();
    }
    /** 
     * Rewrite function Destroy, called when the object of class NetMngActivity deleted.
     * In addition to default operations, turn off automatic management and unregister broadcast receiver.
     */
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if(auto.isAuto())
    		auto.offAuto();
    	this.unregisterReceiver(broadcastReceiver);
    }
    /** 
     * Initialize class WifiManagerEx and its object.
     */
    private void mngInit()
    {
    	wifiMngEx= new WifiManagerEx(this.getApplicationContext());
    	auto = new AutoWifiManager(wifiMngEx);
    }
    /** 
     * Initialize widgets of the main panel.
     */
    private void attrInit()
    {
        submitSpeedBtn = (Button)findViewById(R.id.submitEntrSpeed);
        submitSpeedBtn.setOnClickListener(new SubSpeedButtonListener());
        submitIntervalBtn = (Button)findViewById(R.id.submitExitSpeed);
        submitIntervalBtn.setOnClickListener(new SubIntervalButtonListener());
        editSwitchSpeed = (EditText)findViewById(R.id.editEntrSpeed);
        editInterval = (EditText)findViewById(R.id.editExitSpeed);
        //Set default values, for users.
        //editSwitchSpeed.setText("20");
        //editInterval.setText("30");
        //Set default values, for testing.
        editSwitchSpeed.setText("20");
        editInterval.setText("1");
        auto.setSwitchSpeed(Integer.valueOf(editSwitchSpeed.getText().toString()).intValue());
        auto.setInterval(Integer.valueOf(editInterval.getText().toString()).intValue());
        cbSwi = (CheckBox)findViewById(R.id.cbSwitch);
        cbSwi.setOnCheckedChangeListener(new SwiCBListener());
        cbRib = (CheckBox)findViewById(R.id.cbRib);
        cbRib.setOnCheckedChangeListener(new RibCBListener());
        tvSSTitle = (TextView)findViewById(R.id.tvSSTile);
        tvITitle = (TextView)findViewById(R.id.tvITile);
        tvExit = (TextView)findViewById(R.id.tvExit);  
        tvSSTitle.setOnClickListener(new TextViewListener());
        tvITitle.setOnClickListener(new TextViewListener());
        tvExit.setOnClickListener(new TextViewListener());
    }
    /** 
     * Initialize dialogs
     */
    private void dialogInit()
	{
		dialog_not_cfg = new AlertDialog.Builder(NetMngActivity.this);
	    dialog_cfg = new AlertDialog.Builder(NetMngActivity.this);
	    dialog_i = new AlertDialog.Builder(this);
    	dialog_ss = new AlertDialog.Builder(this);
    	dialog_ss.setTitle(R.string.enter_ss_title_lite);
    	dialog_ss.setMessage(
    			((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.enter_switchSpeed))
    			+((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.enter_switchSpeed_ex))
    			);
	    dialog_i.setTitle(R.string.enter_i_title_lite);
	    dialog_i.setMessage(
    			((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.enter_interval))
    			+((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.enter_interval_ex))
    			);
	    dialog_not_cfg.setTitle(R.string.dialog_ncfg_title);
	    dialog_cfg.setTitle(R.string.dialog_cfg_title);
	    dialog_not_cfg.setPositiveButton(R.string.to_wifi_setttings, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				NetMngActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));	
			}
		});
	    dialog_not_cfg.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				cbSwi.setChecked(true);
			}
		});
	    dialog_not_cfg.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
			}
		});
	    dialog_cfg.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				cbSwi.setChecked(true);
			}
		});
	    dialog_cfg.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
			}
		});
	    dialog_ss.setNeutralButton(R.string.back_lite, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
			}
		});
	    dialog_i.setNeutralButton(R.string.back_lite, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
			}
		});
	}
    /** 
     * Initialize the broadcast receiver.
     */
    private void bcInit()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("dialog_show");
        broadcastReceiver = new DialogBroadcastReceiver();
        this.registerReceiver(broadcastReceiver, intentFilter);
    }
	/** 
     * Set responding operations of submitSpeedBtn
     */
	private class SubSpeedButtonListener implements OnClickListener
	{
    	@Override
    	public void onClick(View v) 
    	{
    		auto.setSwitchSpeed(Integer.valueOf(editSwitchSpeed.getText().toString()).intValue());
    	}   	
    }
	/** 
     * Set responding operations of submitIntervalBtn
     */
	private class SubIntervalButtonListener implements OnClickListener
	{
    	@Override
    	public void onClick(View v) 
    	{
    		auto.setInterval(Integer.valueOf(editInterval.getText().toString()).intValue());
    	}   	
    }
	private class SwiCBListener implements OnCheckedChangeListener
	{
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
		{

			cbSwi.setChecked(isChecked);
			String signal = ((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.already));
			if(isChecked)
			{
				auto.onAuto();
				signal+=((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.onauto));
			}
			else
			{
				auto.offAuto();
				signal+=((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.offauto));
			}
			(Toast.makeText(getApplicationContext(),
					(String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.signal)
					+signal, Toast.LENGTH_SHORT)).show();
		}
		
	}
	private class RibCBListener implements OnCheckedChangeListener
	{
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
		{
			String signal = "";
			if(isChecked)
			{
				NetMngActivity.this.wifiMngEx.openWifi();
				signal+=((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.openwifi));
			}
			else
			{
				NetMngActivity.this.wifiMngEx.closeWifi();
				signal+=((String)NetMngActivity.this.getBaseContext().getResources().getString(R.string.closewifi));
			}
			(Toast.makeText(getApplicationContext(),signal, Toast.LENGTH_SHORT)).show();
		}
		
	}
	private class TextViewListener implements OnClickListener
	{
    	@Override
    	public void onClick(View v) 
    	{
    		switch(v.getId())
    		{
    		case R.id.tvSSTile:
    			dialog_ss.show();break;
    		case R.id.tvITile:
    			dialog_i.show();break;
    		case R.id.tvExit:
    			NetMngActivity.this.finish();break;
    			default:
    				break;
    		}
    	}   	
    }
	/** 
     * A BroadcastReceiver to prompt dialogs
     */
	private class DialogBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("dialog_show"))
			{
				Resources r = context.getResources();
				switch(intent.getExtras().getInt("dialog"))
				{
				case DIALOG_NULL :
					break;
				case DIALOG_NOT_CONFIG :
					auto.offAuto();
					cbSwi.setChecked(false);
					dialog_not_cfg.setMessage((String)r.getString(R.string.dialog_ncfg_message)
							+intent.getExtras().getString("message"));
					dialog_not_cfg.show();
					(Toast.makeText(getApplicationContext(),
							(String)getApplicationContext().getResources().getString(R.string.dialog_ncfg_toast)
							, Toast.LENGTH_LONG)).show();
					break;
				case DIALOG_CONFIG :
					auto.offAuto();
					cbSwi.setChecked(false);
					dialog_cfg.setMessage((String)r.getString(R.string.dialog_cfg_message)
							+intent.getExtras().getString("message"));
					(Toast.makeText(getApplicationContext(),
							(String)getApplicationContext().getResources().getString(R.string.dialog_cfg_toast)
							, Toast.LENGTH_LONG)).show();
					dialog_cfg.show();
					break;
				default:
					break;
				}
			}
		}	
	}
}