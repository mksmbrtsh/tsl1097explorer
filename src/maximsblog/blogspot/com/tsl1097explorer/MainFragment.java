package maximsblog.blogspot.com.tsl1097explorer;

import java.util.ArrayList;
import maximsblog.blogspot.com.tsl1097explorer.Service1097Bluetooth.IntentDo;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements OnClickListener {

	private Switch mToggleConnectSwitch;
	private String mCurrentAddressDevice;
	private ListView mLogList;
	private MenuItem mSleepMenuItem;
	private MenuItem mBarcodeMenuItem;
	private Button mBatteryStatus;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		mToggleConnectSwitch = (Switch) rootView.findViewById(R.id.toggleButton1);
		mToggleConnectSwitch.setOnClickListener(this);
		mLogList = (ListView) rootView.findViewById(R.id.log_listView);
		mBatteryStatus = (Button) rootView.findViewById(R.id.battery_status);
		mBatteryStatus.setOnClickListener(this);
		if (savedInstanceState != null)
			mCurrentAddressDevice = savedInstanceState.getString("address");
		TextView t = (TextView) rootView.findViewById(R.id.about_text);
		t.setText(getResources().getText(R.string.about_text));
		Linkify.addLinks(t, Linkify.ALL);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(android.view.Menu menu,
			android.view.MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
		SharedPreferences pref2 = getActivity().getSharedPreferences(
				"connection", Context.MODE_PRIVATE);
		boolean sleep = pref2.getBoolean("sleep", false);
		boolean barcode = pref2.getBoolean("barcode", false);
		mSleepMenuItem = menu.findItem(R.id.sleep_with_screen);
		mSleepMenuItem.setChecked(sleep);
		mBarcodeMenuItem = menu.findItem(R.id.tagtype);
		mBarcodeMenuItem.setChecked(barcode);
	};

	@Override
	public void onResume() {
		super.onResume();
		mToggleConnectSwitch.setChecked(false);
		mToggleConnectSwitch.setEnabled(false);
		mBatteryStatus.setEnabled(false);
		Intent service;
		service = new Intent(getActivity(), Service1097Bluetooth.class);
		if (!Service1097Bluetooth.isMyServiceRunning(getActivity())) {
			mToggleConnectSwitch.setEnabled(true);
		} else {
			service.putExtra("IntentDo", IntentDo.status);
		}
		getActivity().startService(service);
		
		SharedPreferences pref2 = getActivity().getSharedPreferences(
				"connection", Context.MODE_PRIVATE);
		int size = pref2.getInt("tag_array_size", 0);
		ArrayList<String> t = new ArrayList<String>(size);
		for (int i = 0; i < size; i++)
			t.add(pref2.getString("tag_array_" + i, null));
		dataReceived(t);
		
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.sleep_with_screen) {
			item.setChecked(!item.isChecked());
			getActivity()
			.getSharedPreferences("connection", Context.MODE_PRIVATE)
			.edit().putBoolean("sleep", item.isChecked()).commit();
			Intent service = new Intent(getActivity(), Service1097Bluetooth.class);
			service.putExtra("IntentDo", IntentDo.status);
			getActivity().startService(service);
			return false;

		} else if (item.getItemId() == R.id.tagtype) {
			item.setChecked(!item.isChecked());
			getActivity()
					.getSharedPreferences("connection", Context.MODE_PRIVATE)
					.edit().putBoolean("barcode", item.isChecked()).commit();
			Intent service = new Intent(getActivity(), Service1097Bluetooth.class);
			service.putExtra("IntentDo", IntentDo.status);
			getActivity().startService(service);
			return false;
		} else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("address", mCurrentAddressDevice);
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mLogList.setAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, android.R.id.text1,
				new ArrayList<String>()));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK) {
				mCurrentAddressDevice = data
						.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS);
				Intent service = new Intent(getActivity(),
						Service1097Bluetooth.class);
				service.putExtra("IntentDo", IntentDo.connect);
				service.putExtra("address", mCurrentAddressDevice);
				getActivity().startService(service);
			} else {
				mToggleConnectSwitch.setChecked(false);
				mToggleConnectSwitch.setEnabled(true);
			}
		} else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				if (mCurrentAddressDevice == null) {
					Intent intent = new Intent(getActivity()
							.getApplicationContext(), DeviceList.class);
					startActivityForResult(intent,
							BluetoothState.REQUEST_CONNECT_DEVICE);
				} else {
					Intent service = new Intent(getActivity(),
							Service1097Bluetooth.class);
					service.putExtra("IntentDo", IntentDo.connect);
					service.putExtra("address", mCurrentAddressDevice);
					getActivity().startService(service);
				}
			} else {
				mToggleConnectSwitch.setChecked(false);
				mToggleConnectSwitch.setEnabled(true);
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.battery_status) {
			Intent service;
			service = new Intent(getActivity(), Service1097Bluetooth.class);
			service.putExtra("IntentDo", IntentDo.dataDataReceived);
			service.putExtra("msg", "b");
			getActivity().startService(service);
		} else {
			Intent service;
			service = new Intent(getActivity(), Service1097Bluetooth.class);
			mToggleConnectSwitch.setEnabled(false);
			if (!mToggleConnectSwitch.isChecked()) {
				service.putExtra("IntentDo", IntentDo.disconnect);
			} else {
				service.putExtra("address", mCurrentAddressDevice);
				service.putExtra("IntentDo", IntentDo.connect);
			}
			getActivity().startService(service);
		}
	}

	public void setIntent(Intent intent) {
		if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.connect.toString())) {
			mToggleConnectSwitch.setEnabled(true);
			mToggleConnectSwitch.setChecked(true);
			mBatteryStatus.setEnabled(true);
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.status.toString())) {
			mToggleConnectSwitch.setEnabled(true);
			int status = intent.getIntExtra("status", -1);
			if (status == BluetoothState.STATE_CONNECTED) {
				mToggleConnectSwitch.setChecked(true);
				mBatteryStatus.setEnabled(true);
			} else {
				mToggleConnectSwitch.setChecked(false);
				mBatteryStatus.setEnabled(false);
			}
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.connectFailed.toString())) {
			mToggleConnectSwitch.setChecked(false);
			mToggleConnectSwitch.setEnabled(true);
			mBatteryStatus.setEnabled(false);
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.dataDataReceived.toString())) {
			dataReceived(intent.getStringArrayListExtra("message"));
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.disconnect.toString())) {
			mToggleConnectSwitch.setChecked(false);
			mToggleConnectSwitch.setEnabled(true);
			mBatteryStatus.setEnabled(false);
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.enableBluetooth.toString())) {
			Intent activity = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(activity, BluetoothState.REQUEST_ENABLE_BT);
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.requestConnectDevice.toString())) {
			Intent activity = new Intent(getActivity().getApplicationContext(),
					DeviceList.class);
			startActivityForResult(activity,
					BluetoothState.REQUEST_CONNECT_DEVICE);
		} else if (intent.getAction().equals(
				Service1097Bluetooth.IntentDo.buttonHold.toString())) {
			mToggleConnectSwitch.setChecked(false);
			mToggleConnectSwitch.setEnabled(true);
			mBatteryStatus.setEnabled(false);
			Toast.makeText(getActivity(), R.string.button_hold,
					Toast.LENGTH_LONG).show();
		}
	}

	private void dataReceived(ArrayList<String> arrayList) {
		if (arrayList.size() != 0) {
			ArrayAdapter<String> adapter = ((ArrayAdapter<String>) mLogList
					.getAdapter());
			adapter.clear();
			adapter.addAll(arrayList);
			adapter.notifyDataSetChanged();
		}
	}

}
