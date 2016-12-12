package maximsblog.blogspot.com.tsl1097explorer;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements
		BluetoothConnectionListener, OnClickListener {

	private BluetoothSPP bt;
	private ToggleButton tg;
	private String arr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		tg = (ToggleButton)rootView.findViewById(R.id.toggleButton1);
		tg.setOnClickListener(this);
		if(savedInstanceState != null)
			arr = savedInstanceState.getString("arr");
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("arr", arr);
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		bt = new BluetoothSPP(getActivity());
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
			tg.setChecked(true);
		else 
			tg.setChecked(false);
		bt.setBluetoothConnectionListener(this);
		if (!bt.isBluetoothEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
		} else {
			if (!bt.isServiceAvailable()) {
				bt.setupService();
				bt.startService(BluetoothState.DEVICE_OTHER);
				if(arr != null){
					bt.connect(arr);
					tg.setEnabled(false);
				}
			}
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		bt.setBluetoothConnectionListener(null);
	};

	public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK) {
				bt.connect(data);
				tg.setEnabled(false);
			}
		} else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				bt.setupService();
				bt.startService(BluetoothState.DEVICE_OTHER);
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), DeviceList.class);
				startActivityForResult(intent,
						BluetoothState.REQUEST_CONNECT_DEVICE);
			} else {
				// Do something if user doesn't choose any device (Pressed back)
			}
		}
	}

	private void setup() {
		bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			public void onDataReceived(byte[] data, String message) {
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG)
						.show();
			}
		});
		bt.send("a", true);
	}

	@Override
	public void onDeviceConnected(String name, String address) {
		arr = bt.getConnectedDeviceAddress();
		tg.setChecked(true);
		tg.setEnabled(true);
		setup();
	}

	@Override
	public void onDeviceDisconnected() {
		Toast.makeText(getActivity(), "onDeviceDisconnected", Toast.LENGTH_LONG)
				.show();
		tg.setEnabled(true);
	}

	@Override
	public void onDeviceConnectionFailed() {
		tg.setEnabled(true);
		Toast.makeText(getActivity(), "onDeviceConnectionFailed",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View arg0) {
		((ToggleButton)arg0).setChecked(false);
		if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            Intent intent = new Intent(getActivity().getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
	}

}
