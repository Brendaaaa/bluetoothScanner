package com.scan.out;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.scan.bleexample.R;
import com.scan.out.BluetoothHandler.OnScanListener;

public class MainActivity extends Activity {

	private Button scanButton;
	private EditText deviceNameEditText;
	private ListView deviceListView;
	private DeviceListAdapter listViewAdapter;
	private BluetoothHandler bluetoothHandler;
	private int regionNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		deviceNameEditText = (EditText)findViewById(R.id.deviceName);
		scanButton = (Button) findViewById(R.id.scanButton);
		deviceListView = (ListView) findViewById(R.id.bleDeviceListView);


		Intent intent = getIntent();
		if (intent != null) {
			regionNumber = intent.getIntExtra("regionNumber", -1);
		}
		listViewAdapter = new DeviceListAdapter(this, regionNumber);
		bluetoothHandler = new BluetoothHandler(this, listViewAdapter);

		System.out.println("Numero da regiao: " + regionNumber);

		deviceListView.setAdapter(bluetoothHandler.getDeviceListAdapter());

		BluetoothApplication app = (BluetoothApplication)getApplication();
		app.setBluetoothHandler(bluetoothHandler);

		startService(new Intent(this, BluetoothService.class));

	}

	public void scanOnClick(final View v){
		deviceListView.setAdapter(bluetoothHandler.getDeviceListAdapter());
		bluetoothHandler.setOnScanListener(new OnScanListener() {
			@Override
			public void onScanFinished() {
				// TODO Auto-generated method stub
				((Button) v).setText("scan");
				((Button) v).setEnabled(true);
			}

			@Override
			public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			}
		});
		((Button)v).setText("scanning");
		System.out.println("out = " + listViewAdapter.getCount());
				((Button) v).setEnabled(false);
		bluetoothHandler.scanLeDevice(true);
	}
	
	private void showMessage(String str){
		Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
	}

	// write text to file
	public void WriteBtn(View v) {
		if (deviceNameEditText.getText().toString().length() > 17) {
			String deviceAddress = deviceNameEditText.getText().toString().toUpperCase().substring(0, 17);

			if (deviceAddress.matches("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})")) {
				String name= deviceNameEditText.getText().toString().substring(17);
				listViewAdapter.addDevice(deviceAddress, name);
			}
		}

	}

	@Override
	public void onPause() {
		if (bluetoothHandler.getBluetoothAdapter() != null) {
			if (bluetoothHandler.getBluetoothAdapter().isDiscovering()) {
				bluetoothHandler.getBluetoothAdapter().cancelDiscovery();
			}
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(bluetoothHandler.getBroadcastReceiver());

		super.onDestroy();
	}

}
