package com.dyna.sdk;


import com.Routon.iDRHIDLib.iDRHIDDev;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class IDCertificationActivity extends Activity {

	private TextView tv_result;
	private Button btn_read;
	private Button btn_clear;

	private TextView tv_name;
	private TextView tv_gender;
	private TextView tv_nation;
	private TextView tv_birth;
	private TextView tv_certAddress;
	private TextView tv_certNumber;
	private TextView tv_certOrg;
	private TextView tv_date;
	private ImageView image_identityPic;

	private static final String TAG = "IDCertificationActivity";
	private UsbManager mUsbManager;// USB管理
	private static final String ACTION_USB_PERMISSION = "com.Routon.HIDTest.USB_PERMISSION";// 广播信号
	private UsbDevice mDevice;// usb设备
	private final iDRHIDDev mHIDDev = new iDRHIDDev();// 读卡器设备
	private iDRHIDDev.SecondIDInfo sIDInfo;// 身份证信息

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idcertfication);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		findViewByID();
		initView();
	}

	@Override
	protected void onDestroy() {
		
		if (mDevice != null) {
			mHIDDev.closeDevice();
			mDevice = null;
		}
		unregisterReceiver(mUsbReceiver);// 注销广播
		super.onDestroy();
	}

	private void findViewByID() {
		// TODO Auto-generated method stub
		btn_read = (Button) this.findViewById(R.id.btn_read);
		btn_clear = (Button) this.findViewById(R.id.btn_clear);
		tv_result = (TextView) this.findViewById(R.id.tv_result);
		this.tv_birth = (TextView) this.findViewById(R.id.tv_birthday);
		this.tv_certAddress = (TextView) this.findViewById(R.id.tv_address);
		this.tv_certNumber = (TextView) this.findViewById(R.id.tv_number);

		this.tv_nation = (TextView) this.findViewById(R.id.tv_ehtnic);
		this.tv_name = (TextView) this.findViewById(R.id.tv_name);
		this.tv_gender = (TextView) this.findViewById(R.id.tv_sex);
		this.image_identityPic = (ImageView) this.findViewById(R.id.iv_photo);
		this.tv_certOrg = (TextView) this.findViewById(R.id.tv_org);
		this.tv_date = (TextView) this.findViewById(R.id.tv_date);
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (ACTION_USB_PERMISSION.equals(action)) {
				
				synchronized (this) {
					
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (mDevice != null) {
						mHIDDev.closeDevice();
						mDevice = null;
					}

					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

						if (device != null) {
							final int ret = mHIDDev.openDevice(mUsbManager, device);
							Log.i(TAG, "open device:" + ret);
							if (ret == 0) {
								mDevice = device;
								Log.e(TAG, "usb device: 已授权");
							} else {
								mDevice = null;
								Log.e(TAG, "usb device: 授权失败");
							}
						}
						
					} else {
						Log.d(TAG, "permission denied for device " + device);
						Log.d(TAG, "usb device: 未授权");
						finish();
					}
				}
			}
		}
	};

	private void RegistBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		registerReceiver(mUsbReceiver, filter);
	}

	private void initView() {
		super.onStart();

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);// 获取USB管理服务
		RegistBroadcast();// 注册广播
		// 检查USB设备是否插入
		for (UsbDevice device : mUsbManager.getDeviceList().values()) {
	
			//Log.e(TAG, "vid: " + device.getVendorId() + " pid:" + device.getProductId()+" DeviceId:"+device.getDeviceId());
			if (device.getVendorId() == 1061 && device.getProductId() == 33113) {
				Log.e(TAG, "vid: " + device.getVendorId() + " pid:" + device.getProductId()+" DeviceId:"+device.getDeviceId());
				Intent intent = new Intent(ACTION_USB_PERMISSION);
				PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
				mUsbManager.requestPermission(device, mPermissionIntent);
				Log.d(TAG, "usb device: checked" + "发现读卡设备");
			}
		}

		btn_read.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tv_result.setText("");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ReadCard();
					}
				});

			}

		});

	}

	private void ReadCard() {

		tv_result.setText("读二代证");
		if (mDevice == null) {
			Log.d(TAG, "请插入读卡器！");
			return;
		}

		int ret;

		// 读安全模块的状态
		ret = mHIDDev.GetSamStaus();
		if (ret < 0) {
			Log.d(TAG, "读卡器未准备好！");
			return;
		}
		iDRHIDDev.SamIDInfo samIDInfo = mHIDDev.new SamIDInfo();
		// 读安全模块号
		ret = mHIDDev.GetSamId(samIDInfo);
		Log.d(TAG, "samid: " + samIDInfo.samID);
		// 找卡
		ret = mHIDDev.Authenticate();
		if (ret >= 0) // 找到卡
		{
			// 读卡
			sIDInfo = mHIDDev.new SecondIDInfo();
			byte[] fingerPrint = new byte[1024];

			ret = mHIDDev.ReadBaseFPMsg(sIDInfo, fingerPrint);

			if (ret < 0) {
				Log.d(TAG, "读卡失败：");
				return;
			}
			Log.d(TAG, sIDInfo.fingerPrint == 1024 ? "有指纹" : "无指纹");
			updateResultToUI();
			// 设置蜂鸣器和LED灯
			ret = mHIDDev.BeepLed(true, true, 500);
		} 
		else // 未找到卡
		{
			iDRHIDDev.MoreAddrInfo mAddr = mHIDDev.new MoreAddrInfo();
			// 通过读追加地址来判断卡是否在机具上。
			ret = mHIDDev.GetNewAppMsg(mAddr);

			if (ret < 0) // 机具上没有放卡
				Log.d(TAG, "请放卡：");
			else // 机具上的卡已读过一次
				Log.d(TAG, "请重新放卡：");
		}
		// 读卡号， 注意不要放在读身份证信息前面，否则会读身份证信息失败
		byte data[] = new byte[32];
		int www = mHIDDev.getIDCardCID(data);
		if (www < 0) {
			Log.d(TAG, "读卡号失败：" + www);
		} else {
			String cardID = String.format("%s %02x%02x%02x%02x%02x%02x%02x%02x", "卡体号：", data[0], data[1], data[2],
					data[3], data[4], data[5], data[6], data[7]);
			Log.d(TAG, cardID);
		}
	}
	
	private void updateResultToUI() {

		this.tv_birth.setText(sIDInfo.birthday);
		this.tv_certAddress.setText(sIDInfo.address);
		this.tv_certNumber.setText(sIDInfo.id);
		this.tv_gender.setText(sIDInfo.gender);
		this.tv_name.setText( sIDInfo.name);
		
	//	this.tv_nation.setText(sIDInfo.cardType);
		
		this.tv_certOrg.setText(sIDInfo.agency);
		this.tv_date.setText(sIDInfo.expireStart + "-" + sIDInfo.expireEnd);
		if (sIDInfo.photo != null) {
			this.image_identityPic.setImageBitmap(sIDInfo.photo);
		}
		
	}
}
