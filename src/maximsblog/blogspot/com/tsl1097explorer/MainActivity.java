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
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.connect.toString());
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.status.toString());
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.connectFailed.toString());
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.dataDataReceived.toString());
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.disconnect.toString());
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.enableBluetooth.toString());
		mIntentFilter.addAction(ServiceBluetooth.IntentDo.requestConnectDevice.toString());
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
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
}
