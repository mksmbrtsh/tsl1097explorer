package maximsblog.blogspot.com.tsl1097explorer;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.AutoConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;

public class ServiceBluetooth extends Service implements
		BluetoothConnectionListener, OnDataReceivedListener,
		AutoConnectionListener {

	private BluetoothSPP bt;
	private boolean status;
	private String address;
	private String keywordName;
	private boolean sleep;

	public enum IntentDo {
		start, status, connect, disconnect, enableBluetooth, connectFailed, dataDataReceived, requestConnectDevice
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (intent == null)
			return START_STICKY;
		IntentFilter screenStateFilter = new IntentFilter();
		screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
		sleep = intent.getBooleanExtra("sleep", true);
		if (sleep) {
			registerReceiver(mScreenStateReceiver, screenStateFilter);
		} else {
			unregisterReceiver(mScreenStateReceiver);
		}
		
		if (intent.getSerializableExtra("IntentDo") == IntentDo.start) {
			bt = new BluetoothSPP(this);
			bt.setBluetoothConnectionListener(this);
			bt.setOnDataReceivedListener(this);
			bt.setAutoConnectionListener(this);
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.status) {
			Intent outIntent = new Intent(IntentDo.status.toString());
			outIntent.putExtra("status", bt.getServiceState());
			this.sendBroadcast(outIntent);
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.connect) {
			address = intent.getStringExtra("address");
			keywordName = intent.getStringExtra("keywordName");
			if (!bt.isBluetoothEnabled()) {
				// not enable bluetooth
				Intent outIntent = new Intent(
						IntentDo.enableBluetooth.toString());
				this.sendBroadcast(outIntent);
			} else {
				if (address == null) {
					Intent outIntent = new Intent(
							IntentDo.requestConnectDevice.toString());
					this.sendBroadcast(outIntent);
				} else {
					if (!bt.isServiceAvailable()) {
						bt.setupService();
						bt.startService(BluetoothState.DEVICE_OTHER);
					}
					bt.connect(address);
					if (keywordName != null)
						bt.autoConnect(keywordName);
				}
			}
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.disconnect) {
			if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
				bt.disconnect();
			}
			return START_STICKY;
		} else if (intent.getSerializableExtra("IntentDo") == IntentDo.dataDataReceived) {
			byte[] data = intent.getByteArrayExtra("data");
			boolean CRLF = intent.getBooleanExtra("CRLF", true);
			bt.send(data, CRLF);
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onDestroy() {
		super.onDestroy();
		bt.setBluetoothConnectionListener(null);
		bt.setOnDataReceivedListener(null);
		if (bt.isAutoConnecting())
			bt.stopAutoConnect();
		bt.stopService();
		if (mScreenStateReceiver != null)
			unregisterReceiver(mScreenStateReceiver);
	}

	@Override
	public void onDeviceConnected(String name, String address) {
		Intent intent = new Intent(IntentDo.connect.toString());
		this.sendBroadcast(intent);
	}

	@Override
	public void onDeviceDisconnected() {
		Intent intent = new Intent(IntentDo.disconnect.toString());
		this.sendBroadcast(intent);
	}

	@Override
	public void onDeviceConnectionFailed() {
		Intent intent = new Intent(IntentDo.connectFailed.toString());
		this.sendBroadcast(intent);
	}

	@Override
	public void onDataReceived(byte[] data, String message) {
		Intent intent = new Intent(IntentDo.dataDataReceived.toString());
		intent.putExtra("data", data);
		intent.putExtra("message", message);
		this.sendBroadcast(intent);
	}

	@Override
	public void onAutoConnectionStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewConnection(String name, String address) {
		// TODO Auto-generated method stub

	}

	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (ServiceBluetooth.class.getName().equals(
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
				if (address != null)
					bt.connect(address);
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
					bt.disconnect();
				}
			}

		}

	};

}
