package com.scan.out;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class BluetoothHandler {
	// scan bluetooth device
	private BluetoothAdapter bluetoothAdapter;
	private boolean mEnabled = false;
	private boolean mScanning = false;
	private static final long SCAN_PERIOD = 12000;
	private DeviceListAdapter deviceListAdapter;
	private ProgressDialog progressDialog;

	private OnScanListener onScanListener;
	
    private Context context;

    public interface OnScanListener{
    	public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord);
    	public void onScanFinished();
    };
    
    public void setOnScanListener(OnScanListener l){
    	onScanListener = l;
    }
    
	public BluetoothHandler(Context context, DeviceListAdapter devListAdapter) {
		// TODO Auto-generated constructor stub
		this.context = context;
		deviceListAdapter = devListAdapter;
		bluetoothAdapter = null;
		progressDialog = new ProgressDialog(this.context);

		progressDialog.setMessage("Scanning...");
		progressDialog.setCancelable(false);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				bluetoothAdapter.cancelDiscovery();
			}
		});

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.EXTRA_RSSI);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		((MainActivity) context).registerReceiver(broadcastReceiver, filter);


		if(!supportsBle()){
			showUnsupported();
			showMessage("your device not support BLE!");
			((MainActivity)context).finish();
			return ;
		}

		// open bluetooth
        if (!getBluetoothAdapter().isEnabled()) { 
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
            ((MainActivity)context).startActivityForResult(mIntent, 1);   
        }else{
        	setEnabled(true);
        }
	}
	
	public DeviceListAdapter getDeviceListAdapter(){
		return deviceListAdapter;
	} 

	public boolean supportsBle(){
		// is support 4.0 ?
//		final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null)
			return false;
		else
			return true;			
	}
	
	public BluetoothAdapter getBluetoothAdapter(){
		return bluetoothAdapter;
	}
	
	public void setEnabled(boolean enabled){
		mEnabled = enabled;
	}
	
	public boolean isEnabled(){
		return mEnabled;
	}
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.obj != null){
				deviceListAdapter.addDevice((BluetoothScanInfo) msg.obj);
				deviceListAdapter.notifyDataSetChanged();
			}
		}
    };
    
    // scan device
 	public void scanLeDevice(boolean enable) {
 		if (enable) {
 			deviceListAdapter.clearList();
 			deviceListAdapter.notifyDataSetChanged();

			// Stops scanning after a pre-defined scan period
			mHandler.postDelayed(new Runnable() {
 			@Override
 				public void run() {
// 					mScanning = false;
// 					bluetoothAdapter.stopLeScan(mLeScanCallback);
 					if(onScanListener != null){
 		        		onScanListener.onScanFinished();
 		        	}
					bluetoothAdapter.startDiscovery();
 				}
 			}, SCAN_PERIOD);

 			mScanning = true;
// 			bluetoothAdapter.startLeScan(mLeScanCallback);
 		} else {
// 			mScanning = false;
// 			bluetoothAdapter.stopLeScan(mLeScanCallback);
			bluetoothAdapter.startDiscovery();
		}
 	}

// 	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(final BluetoothDevice device, final int rssi,
//                final byte[] scanRecord) {
//
//        	if(onScanListener != null){
//        		onScanListener.onScan(device, rssi, scanRecord);
//        	}
//
//            ((MainActivity)context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                	Message msg = new Message();
//                	BluetoothScanInfo info = new BluetoothScanInfo();
//                	info.device = device;
//                	info.rssi = rssi;
//                	msg.obj = info;
//                	mHandler.sendMessage(msg);
//                }
//            });
//        }
//    };
    
    public class BluetoothScanInfo{
    	public BluetoothDevice device;
    	public int rssi;
    };

	private void showMessage(String str){
		Toast.makeText(((MainActivity) context), str, Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_ON) {
					showMessage("Enabled");
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				progressDialog.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				progressDialog.dismiss();
				mScanning = false;
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				final BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				final int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

				((MainActivity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Message msg = new Message();
						BluetoothScanInfo info = new BluetoothScanInfo();
						System.out.println("name  " + device.getName());
						info.device = device;
						info.rssi = rssi;
						msg.obj = info;
						mHandler.sendMessage(msg);
					}
				});

				if (device.getName() == null){
					showMessage("Found other device");
				} else {
					showMessage("Found device: " + device.getName());
				}
			}
		}
	};

	public BroadcastReceiver getBroadcastReceiver(){
		return broadcastReceiver;
	}

	private void showUnsupported() {
		showMessage("Bluetooth is unsupported by this device");
	}

}