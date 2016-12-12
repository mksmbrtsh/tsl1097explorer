package maximsblog.blogspot.com.tsl1097explorer;

import maximsblog.blogspot.com.tsl1097explorer.ServiceBluetooth.IntentDo;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements OnClickListener,
		OnCheckedChangeListener {

	private ToggleButton tg;
	private String address;
	private ListView mLogList;
	private CheckBox mSleepCheckBox;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		tg = (ToggleButton) rootView.findViewById(R.id.toggleButton1);
		tg.setOnClickListener(this);
		mLogList = (ListView) rootView.findViewById(R.id.log_listView);
		mSleepCheckBox = (CheckBox) rootView.findViewById(R.id.sleep_checkBox);

		if (savedInstanceState != null)
			address = savedInstanceState.getString("address");
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("address", address);
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tg.setChecked(false);
		tg.setEnabled(false);

		SharedPreferences pref2 = getActivity().getSharedPreferences(
				"connection", Context.MODE_PRIVATE);
		boolean sleep = pref2.getBoolean("sleep", true);
		mSleepCheckBox.setChecked(sleep);
		mSleepCheckBox.setOnCheckedChangeListener(this);
		Intent service;
		if (!ServiceBluetooth.isMyServiceRunning(getActivity())) {
			service = new Intent(getActivity(), ServiceBluetooth.class);
			service.putExtra("IntentDo", IntentDo.start);
			service.putExtra("sleep", sleep);
			tg.setEnabled(true);
		} else {
			service = new Intent(getActivity(), ServiceBluetooth.class);
			service.putExtra("IntentDo", IntentDo.status);
			service.putExtra("sleep", sleep);
		}
		getActivity().startService(service);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK) {
				address = data
						.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS);
				Intent service = new Intent(getActivity(),
						ServiceBluetooth.class);
				service.putExtra("IntentDo", IntentDo.connect);
				service.putExtra("address", address);
				getActivity().startService(service);
			}
		} else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				if (address == null) {
					Intent intent = new Intent(getActivity()
							.getApplicationContext(), DeviceList.class);
					startActivityForResult(intent,
							BluetoothState.REQUEST_CONNECT_DEVICE);
				} else {
					Intent service = new Intent(getActivity(),
							ServiceBluetooth.class);
					service.putExtra("IntentDo", IntentDo.connect);
					service.putExtra("address", address);
					getActivity().startService(service);
				}
			} else {
				// Do something if user doesn't choose any device (Pressed back)
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		Intent service;
		service = new Intent(getActivity(), ServiceBluetooth.class);
		tg.setEnabled(false);
		if (!tg.isChecked()) {
			service.putExtra("IntentDo", IntentDo.disconnect);
		} else {
			service.putExtra("address", address);
			service.putExtra("IntentDo", IntentDo.connect);
		}
		getActivity().startService(service);
	}

	public void setIntent(Intent intent) {
		if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.connect.toString())) {
			tg.setEnabled(true);
			tg.setChecked(true);
		} else if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.status.toString())) {
			tg.setEnabled(true);
			int status = intent.getIntExtra("status", -1);
			if (status == BluetoothState.STATE_CONNECTED)
				tg.setChecked(true);
			else
				tg.setChecked(false);
		} else if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.connectFailed.toString())) {
			tg.setChecked(false);
			tg.setEnabled(true);
		} else if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.dataDataReceived.toString())) {
			Toast.makeText(getActivity(), intent.getStringExtra("message"),
					Toast.LENGTH_LONG).show();
		} else if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.disconnect.toString())) {
			tg.setChecked(false);
			tg.setEnabled(true);
		} else if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.enableBluetooth.toString())) {
			Intent activity = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(activity, BluetoothState.REQUEST_ENABLE_BT);
		} else if (intent.getAction().equals(
				ServiceBluetooth.IntentDo.requestConnectDevice.toString())) {
			Intent activity = new Intent(getActivity().getApplicationContext(),
					DeviceList.class);
			startActivityForResult(activity,
					BluetoothState.REQUEST_CONNECT_DEVICE);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Intent service = new Intent(getActivity(), ServiceBluetooth.class);
		service.putExtra("IntentDo", IntentDo.status);
		service.putExtra("sleep", mSleepCheckBox.isChecked());
		getActivity().startService(service);
	}

}
