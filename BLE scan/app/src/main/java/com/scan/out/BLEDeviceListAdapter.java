package com.scan.out;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scan.bleexample.R;
import com.scan.out.BluetoothHandler.BluetoothScanInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class BLEDeviceListAdapter extends BaseAdapter{
	private TextView devNameTextView, devAddressTextView;
	private TextView devUUIDTextView, devMajorTextView, devMinorTextView;
	private TextView devTxPowerTextView, devDistanceTextView;

	private ArrayList<BluetoothScanInfo> bleArrayList;
	private BluetoothScanInfo findResult;
	private Context context;

	private Map devices;
	private int deviceNumber;

	public BLEDeviceListAdapter(Context context) {
		this.context = context;
		bleArrayList = new ArrayList<BluetoothScanInfo>();
		devices = new LinkedHashMap<String, LinkedList<String>>();
		deviceNumber = 0;
	}

	public void addDevice(BluetoothScanInfo device) {
		LinkedList<String> infoDevice;
		if (!devices.containsKey(device.device.getAddress())){
			infoDevice = new LinkedList<String>();
			if (device.device.getName() != null && device.device.getName().length() > 0) {
				infoDevice.add(device.device.getName());
			} else {
				infoDevice.add("Device" + deviceNumber);
				deviceNumber++;
			}
			infoDevice.add(device.device.getAddress());
			devices.put(device.device.getAddress(), infoDevice);
		}
		infoDevice = (LinkedList<String>) devices.get(device.device.getAddress());
		infoDevice.add(new Integer(device.rssi).toString());
		devices.put(device.device.getAddress(), infoDevice);

		if (!contains(device)) {
			bleArrayList.add(device);
		}else{
			if(findResult != null){
				int index = bleArrayList.indexOf(findResult);
				bleArrayList.set(index, device);
			}
		}

	}
	
	public boolean contains(BluetoothScanInfo dstDevice){
		boolean val = false;
		findResult = null;
		for(BluetoothScanInfo d:bleArrayList){
			if(d.device.getAddress().equals(dstDevice.device.getAddress())){
				val = true;
				findResult = d;
				break;
			}
		}
		return val;
	}

	public void addDevice(String deviceAddress, String name){
		System.out.println("outttt" + deviceAddress + "    " + name);
		LinkedList<String> infoDevice;
		if (!devices.containsKey(deviceAddress)) {
			infoDevice = new LinkedList<String>();
			infoDevice.add(name);
		} else {
			infoDevice = (LinkedList<String>) devices.get(deviceAddress);
			infoDevice.addFirst(name);
		}
		devices.put(deviceAddress, infoDevice);
	}
	
	public void clearDevice(){
		bleArrayList.clear();
	}
	
	public void removeDevice(BluetoothScanInfo dev){
		bleArrayList.remove(dev);
	}

	@Override
	public int getCount() {
		return bleArrayList.size();
	}

	@Override
	public BluetoothScanInfo getItem(int position) {
		return bleArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = null;
		
		if (convertView == null) {
			layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.item_list, null);
			devNameTextView = (TextView) layout.findViewById(R.id.textViewDevName);
			devAddressTextView = (TextView) layout.findViewById(R.id.textViewDevAddress);
			devUUIDTextView = new TextView(context);
			devUUIDTextView.setTextSize(12);
			devMajorTextView = new TextView(context);
			devMajorTextView.setTextSize(12);
			devMinorTextView = new TextView(context);
			devMinorTextView.setTextSize(12);
			devTxPowerTextView = new TextView(context);
			devTxPowerTextView.setTextSize(12);
			devDistanceTextView = new TextView(context);
			devDistanceTextView.setTextSize(12);
			convertView = layout;
		}

		// add-Parameters
		BluetoothDevice device = bleArrayList.get(position).device;
		int rssi = bleArrayList.get(position).rssi;
		byte[] scanRecord = bleArrayList.get(position).scanRecord;
		LinkedList<String> deviceHistory = (LinkedList<String>) devices.get(device.getAddress());
		String devName = deviceHistory.getFirst();
		if (devName != null && devName.length() > 0) {
			devNameTextView.setText(devName+"   rssi:"+String.valueOf(rssi));
		} else {
			devNameTextView.setText("unknow-device"+"   rssi:"+String.valueOf(rssi));
		}
		devAddressTextView.setText(deviceHistory.toString());

		//Checks for iBeacon specification, that is byte 7 = 0x02 - iBeacon type, byte 8 = 0x15 => length = 16bytes(uuid) + 2 major + 2 minor + 1 tx porwer = 21bytes
		if(scanRecord!= null && scanRecord[7] == 0x02 && scanRecord[8] == 0x15){
			String uuid = String.format("uuid:%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X", 
					scanRecord[9], scanRecord[10], scanRecord[11], scanRecord[12],
					scanRecord[13], scanRecord[14],
					scanRecord[15], scanRecord[16], 
					scanRecord[17], scanRecord[18], 
					scanRecord[19], scanRecord[20], scanRecord[21], scanRecord[22], scanRecord[23], scanRecord[24]);
			String major = String.format("major:%02X%02X", scanRecord[25], scanRecord[26]);
			String minor = String.format("minor:%02X%02X", scanRecord[27], scanRecord[28]);
			String txPower = String.format("txPower:%02X", scanRecord[29]);
			String distance = String.format("distance:%f m", BluetoothHandler.calculateAccuracy(Integer.parseInt(String.format("%s", scanRecord[29])), rssi));
			devUUIDTextView.setText(uuid);
			devMajorTextView.setText(major);
			devMinorTextView.setText(minor);
			devTxPowerTextView.setText(txPower);
			devDistanceTextView.setText(distance);
//			if(layout != null) {
//				layout.addView(devUUIDTextView);
//				layout.addView(devMajorTextView);
//				layout.addView(devMinorTextView);
//				layout.addView(devTxPowerTextView);
//				layout.addView(devDistanceTextView);
//			}
		}else{
			devUUIDTextView.setText("");
			devMajorTextView.setText("");
			devMinorTextView.setText("");
		}

		
		return convertView;
	}
}
