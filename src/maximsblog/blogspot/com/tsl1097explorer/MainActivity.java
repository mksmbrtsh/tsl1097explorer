package maximsblog.blogspot.com.tsl1097explorer;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends Activity {

	private IntentFilter mIntentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.connect.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.status.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.connectFailed.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.dataDataReceived.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.disconnect.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.enableBluetooth.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.requestConnectDevice.toString());
		mIntentFilter.addAction(Service1097Bluetooth.IntentDo.buttonHold.toString());
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment(), "main").commit();
		}
	}
	
	@Override
	public void onResume() {
		super.onStart();
		registerReceiver(mIntentReceiver, mIntentFilter);
	}
	
	@Override
	public void onPause() {
		super.onStop();
		if (mIntentReceiver != null)
			unregisterReceiver(mIntentReceiver);
	};
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				MainFragment mainFragment = (MainFragment)getFragmentManager().findFragmentByTag("main");
				mainFragment.setIntent(intent);
			}
		}
	};
}
