package maximsblog.blogspot.com.tsl1097explorer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.AutoConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;

public class Service1097Bluetooth extends Service implements
		BluetoothConnectionListener, OnDataReceivedListener,
		Callback {

	private BluetoothSPP mBluetoothSPP;
	private String mCurrentAddressDevice;
	private boolean mSleepMode;
	private boolean mIsBarcode;
	private boolean mOutDoorCommandFlag;
	private boolean mIsInitDeviceSuccessful;
	private Handler mHandler = new Handler(this);
	ArrayList<String> mCurrentReadData;
	SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("(dd.MM.yy HH:mm) ");
	
	private Runnable mStatusRun = new Runnable() {

		@Override
		public void run() {
			mBluetoothSPP.send("s", true);
		}
	};

	public enum IntentDo {
		status, connect, disconnect, enableBluetooth, connectFailed, dataDataReceived, requestConnectDevice, buttonHold
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (intent == null)
			return START_STICKY;
		IntentFilter screenStateFilter = new IntentFilter();
		screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);

		mOutDoorCommandFlag = false;

		SharedPreferences pref2 = getSharedPreferences("connection",
				Context.MODE_PRIVATE);
		boolean sleepNow = pref2.getBoolean("sleep", false);
		mIsBarcode = pref2.getBoolean("barcode", false);
		if (sleepNow) {
			mSleepMode = true;
			registerReceiver(mScreenStateReceiver, screenStateFilter);
		} else {
			if (mSleepMode) {
				mSleepMode = false;
				unregisterReceiver(mScreenStateReceiver);
			}
		}

		if (mBluetoothSPP == null) {
			mBluetoothSPP = new BluetoothSPP(this);
			mBluetoothSPP.setBluetoothConnectionListener(this);
			mBluetoothSPP.setOnDataReceivedListener(this);
		}
		if (intent.getSerializableExtra("IntentDo") == IntentDo.status) {
			Intent outIntent = new Intent(IntentDo.status.toString());
			outIntent.putExtra("status", mBluetoothSPP.getServiceState());
			this.sendBroadcast(outIntent);
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.connect) {
			mCurrentAddressDevice = intent.getStringExtra("address");
			if (!mBluetoothSPP.isBluetoothEnabled()) {
				// not enable bluetooth
				Intent outIntent = new Intent(
						IntentDo.enableBluetooth.toString());
				this.sendBroadcast(outIntent);
			} else {
				if (mCurrentAddressDevice == null) {
					Intent outIntent = new Intent(
							IntentDo.requestConnectDevice.toString());
					this.sendBroadcast(outIntent);
				} else {
					if (!mBluetoothSPP.isServiceAvailable()) {
						mBluetoothSPP.setupService();
						mBluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
					}
					mBluetoothSPP.connect(mCurrentAddressDevice);
				}
			}
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.disconnect) {
			if (mBluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
				mBluetoothSPP.send("e", true);
				mBluetoothSPP.disconnect();
			}
			return START_STICKY;
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.dataDataReceived) {
			byte[] data = intent.getByteArrayExtra("data");
			String message = intent.getStringExtra("msg");
			boolean CRLF = intent.getBooleanExtra("CRLF", true);
			mHandler.removeCallbacks(mStatusRun);
			mOutDoorCommandFlag = true;
			if (message != null)
				mBluetoothSPP.send(message, CRLF);
			else
				mBluetoothSPP.send(data, CRLF);
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onDestroy() {
		super.onDestroy();
		mBluetoothSPP.setBluetoothConnectionListener(null);
		mBluetoothSPP.setOnDataReceivedListener(null);
		if (mBluetoothSPP.isAutoConnecting())
			mBluetoothSPP.stopAutoConnect();
		mBluetoothSPP.stopService();
		if (mSleepMode) {
			mSleepMode = false;
			unregisterReceiver(mScreenStateReceiver);
		}
	}

	@Override
	public void onDeviceConnected(String name, String address) {
		Intent intent = new Intent(IntentDo.connect.toString());
		this.sendBroadcast(intent);
		mBluetoothSPP.send("d", true);
		mIsInitDeviceSuccessful = false;
	}

	@Override
	public void onDeviceDisconnected() {
		Intent intent;
		if (mIsInitDeviceSuccessful)
			intent = new Intent(IntentDo.disconnect.toString());
		else
			intent = new Intent(IntentDo.buttonHold.toString());
		this.sendBroadcast(intent);
	}

	@Override
	public void onDeviceConnectionFailed() {
		Intent intent = new Intent(IntentDo.connectFailed.toString());
		this.sendBroadcast(intent);
	}

	

	@Override
	public void onDataReceived(byte[] data, String message) {
		message = message.trim();
		if (message.equals("A")) {
			mHandler.postDelayed(mStatusRun, 200);
		} else if (message.equals("D")) {
			if (!mIsInitDeviceSuccessful) {
				mIsInitDeviceSuccessful = true;
				mBluetoothSPP.send("s", true);
			}
		} else if (message.equals("On")) {
			if (mCurrentReadData == null)
				mCurrentReadData = new ArrayList<String>();
			if (mIsBarcode)
				mBluetoothSPP.send("c09", true);
			else
				mBluetoothSPP.send("i", true);
		} else if (message.equals("Off")) {
			mHandler.postDelayed(mStatusRun, 200);
			if (mCurrentReadData != null) {
				Intent intent = new Intent(IntentDo.dataDataReceived.toString());
				if (mCurrentReadData.size() == 0) {
					mCurrentReadData.add(getTimeStamp() + getNotFoundTagMsg());
				}
				intent.putStringArrayListExtra("message", mCurrentReadData);
				this.sendBroadcast(intent);
				SharedPreferences pref2 = getSharedPreferences("connection",
						Context.MODE_PRIVATE);
				int size = pref2.getInt("tag_array_size", 0);
				Editor editor = pref2.edit();
				editor.remove("tag_array_size");
				for (int i = 0; i < size; i++)
					editor.remove("tag_array_" + i);
				editor.commit();
				editor.putInt("tag_array" +"_size", mCurrentReadData.size());  
			    for(int i=0;i<mCurrentReadData.size();i++)  
			        editor.putString("tag_array" + "_" + i, mCurrentReadData.get(i));
			    editor.commit();
				mCurrentReadData = null;
			}
		} else {
			if (!mIsInitDeviceSuccessful) {
				mBluetoothSPP.disconnect();
			} else {
				if (mOutDoorCommandFlag) {
					Intent intent = new Intent(
							IntentDo.dataDataReceived.toString());
					mCurrentReadData = new ArrayList<String>();
					mCurrentReadData.add(getTimeStamp() + message);
					intent.putStringArrayListExtra("message", mCurrentReadData);
					this.sendBroadcast(intent);
					SharedPreferences pref2 = getSharedPreferences("connection",
							Context.MODE_PRIVATE);
					int size = pref2.getInt("tag_array_size", 0);
					Editor editor = pref2.edit();
					editor.remove("tag_array_size");
					for (int i = 0; i < size; i++)
						editor.remove("tag_array_" + i);
					editor.commit();
					editor.putInt("tag_array" +"_size", mCurrentReadData.size());  
				    for(int i=0;i<mCurrentReadData.size();i++)  
				        editor.putString("tag_array" + "_" + i, mCurrentReadData.get(i));
				    editor.commit();
					mCurrentReadData = null;
					mOutDoorCommandFlag = false;
					mHandler.postDelayed(mStatusRun, 200);
				} else {
					mHandler.removeCallbacks(mStatusRun);
					if (mCurrentReadData != null && !currentReadDatacontainsMsg(message)) {
						if (!message.equals(getNotFoundTagMsg())) {
							mCurrentReadData.add(getTimeStamp() + message);
							mBluetoothSPP.send("a", true);
						} else
							mHandler.postDelayed(mStatusRun, 200);
					} else
						mHandler.postDelayed(mStatusRun, 200);
				}
			}
		}
	}
	private boolean currentReadDatacontainsMsg(String msg)
	{
		for( String item : mCurrentReadData) {
			if(item.contains(msg))
				return true;
		}
		return false;
	}

	private String getTimeStamp() {
		return mSimpleDateFormat.format(new Date());
	}

	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (Service1097Bluetooth.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				if (mCurrentAddressDevice != null)
					mBluetoothSPP.connect(mCurrentAddressDevice);
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				if (mBluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
					mBluetoothSPP.disconnect();
				}
			}

		}

	};

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	private String getNotFoundTagMsg() {
		return mIsBarcode ? "No Barcode" : "No Transponder";
	}
}
