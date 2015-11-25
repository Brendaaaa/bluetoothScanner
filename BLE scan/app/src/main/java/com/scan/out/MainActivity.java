package com.scan.out;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.scan.bleexample.R;
import com.scan.out.BluetoothHandler.OnScanListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {


	private EditText addDeviceName;
	static final int BUFFER_SIZE = 100;
	private Button scanButton;
	private ListView bleDeviceListView;
	private BLEDeviceListAdapter listViewAdapter;

	private BLEDeviceListAdapter mDevListAdapter;

	private BluetoothHandler bluetoothHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addDeviceName = (EditText)findViewById(R.id.deviceName);

		scanButton = (Button) findViewById(R.id.scanButton);
		bleDeviceListView = (ListView) findViewById(R.id.bleDeviceListView);
		listViewAdapter = new BLEDeviceListAdapter(this);


		mDevListAdapter = new BLEDeviceListAdapter(this);

		initDevListAdapter();

		bluetoothHandler = new BluetoothHandler(this, mDevListAdapter);

	}

	public void scanOnClick(final View v){
		bleDeviceListView.setAdapter(bluetoothHandler.getDeviceListAdapter());
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
		System.out.println("out = " + mDevListAdapter.getCount());
				((Button) v).setEnabled(false);
		bluetoothHandler.scanLeDevice(true);
	}
	
	private void showMessage(String str){
		Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
	}

	// write text to file
	public void WriteBtn(View v) {
		if (addDeviceName.getText().toString().length() > 17) {
			String deviceAddress = addDeviceName.getText().toString().toUpperCase().substring(0, 17);

			if (deviceAddress.matches("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})")) {
				String name= addDeviceName.getText().toString().substring(17);
				mDevListAdapter.addDevice(deviceAddress, name);
				writeToFile(deviceAddress + " " + name + "\n");
			}
		}

	}


	public void writeToFile(String text){

		// add-write text into file
		try {
			FileOutputStream fileout=openFileOutput("devices.txt", MODE_APPEND);
			OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
			outputWriter.write(text);
			outputWriter.close();

			//display file saved message
			Toast.makeText(getBaseContext(), "File saved successfully!",
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			e.printStackTrace();
		}
		initDevListAdapter();
	}

	// Read text from file
	public void initDevListAdapter() {
		String text = "";

			try {
				InputStream inputStream = openFileInput("devices.txt");

				if ( inputStream != null ) {
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String receiveString = "";
					StringBuilder stringBuilder = new StringBuilder();

					while ( (receiveString = bufferedReader.readLine()) != null ) {
						stringBuilder.append(receiveString);
					}

					inputStream.close();
					text = stringBuilder.toString();

					String[] devicesInfo = text.split("\n");
					for (String device: devicesInfo){
						mDevListAdapter.addDevice(device.substring(0, 17), device.substring(17));
					}
				}
			}
			catch (FileNotFoundException e) {
				Log.e("login activity", "File not found: " + e.toString());
			} catch (IOException e) {
				Log.e("login activity", "Can not read file: " + e.toString());
			}

			Toast.makeText(getBaseContext(), "File read",Toast.LENGTH_SHORT).show();



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
//		unregisterReceiver(mReceiver);

		super.onDestroy();
	}

}
