package com.scan.out;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scan.bleexample.R;
import com.scan.out.BluetoothHandler.BluetoothScanInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceListAdapter extends BaseAdapter {

	private TextView devNameTextView;
	private TextView devAddressTextView;

	private ArrayList<String> addressList;
	private BluetoothScanInfo resultScan;
	private Context context;

	public Map devices;
	public Map devicesNames;
	private int deviceNumber;

	private int region;

	public DeviceListAdapter(Context context, int regionNumber) {
		this.context = context;
		addressList = new ArrayList<String>();

		//Map MAC -> rssi
		devices = new LinkedHashMap<String, String>();

		//Map MAC -> name
		devicesNames = new LinkedHashMap<String, String>();

		deviceNumber = 0;

		region = regionNumber;

		//Read from file devices names
		Utils.initInfoFromFile(this.context, devicesNames);

		System.out.println("lendo devicesMeasures\n\n" + Utils.readFile(Utils.DEVICES_MEASURES, this.context));

	}

	public void addDevice(BluetoothScanInfo scanInfo) {
		String macAddress = scanInfo.device.getAddress();
		String deviceName = scanInfo.device.getName();
		String rssi = new Integer(scanInfo.rssi).toString();
		String date = new SimpleDateFormat("HH:mm:ss-dd.MM.yyyy").format(Calendar.getInstance().getTime());

		//Add name to device
		if (!devicesNames.containsKey(macAddress)) {
			if (deviceName != null && deviceName.length() > 0) {
				devicesNames.put(macAddress, deviceName);
			} else {
				deviceName = "Device_" + region + "_" + deviceNumber + new SimpleDateFormat("mmss").format(Calendar.getInstance().getTime());
				devicesNames.put(macAddress, deviceName);
				deviceNumber++;
			}
			//Update names file
			Utils.writeToFile(macAddress + "," + deviceName + ";", Utils.DEVICES_NAMES, this.context);
		}

		//Add rssi
		if (!devices.containsKey(macAddress)) {
			devices.put(macAddress, rssi);
		}

		//Update rssi file
		Utils.writeToFile("R" + region + "," + macAddress + "," + rssi + "," + date + ";", Utils.DEVICES_MEASURES, this.context);

		if (!addressList.contains(macAddress)) {
			addressList.add(macAddress);
		}
	}

	public void addDevice(String macAddress, String deviceName) {
		//Add name to device
		devicesNames.put(macAddress, deviceName);
		//Update names file
		Utils.writeToFile(macAddress + "," + deviceName + ";", Utils.DEVICES_NAMES, this.context);

		if (!addressList.contains(macAddress)) {
			addressList.add(macAddress);
		}
	}

	public void clearList() {
		addressList.clear();
	}

	public void removeDevice(BluetoothScanInfo dev) {
		addressList.remove(dev);
	}

	@Override
	public int getCount() {
		return addressList.size();
	}

	@Override
	public String getItem(int position) {
		return addressList.get(position);
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
			convertView = layout;
		}

		System.out.println("size = " + addressList.size());
		String macAddress = addressList.get(position);
		System.out.println("mac address = " + macAddress);
		String deviceName = (String) devicesNames.get(macAddress);
		System.out.println("devicess NAMES = " + deviceName);
		String rssi = (String) devices.get(macAddress);
		if (rssi == null) {
			rssi = "--";
		}

		devNameTextView.setText(deviceName + "   RSSI: " + rssi);
		devAddressTextView.setText(macAddress);

		return convertView;
	}

}
