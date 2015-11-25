package com.scan.out;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mEnabled = false;
	private boolean mScanning = false;
	private static final long SCAN_PERIOD = 2000;
	private BLEDeviceListAdapter mDevListAdapter;
	private ProgressDialog mProgressDlg;

	private OnScanListener onScanListener;
	
    private Context context;

    public interface OnScanListener{
    	public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord);
    	public void onScanFinished();
    };
    
    public void setOnScanListener(OnScanListener l){
    	onScanListener = l;
    }
    
	public BluetoothHandler(Context context, BLEDeviceListAdapter devListAdapter) {
		// TODO Auto-generated constructor stub
		this.context = context;
		mDevListAdapter = devListAdapter;
		mBluetoothAdapter = null;
		mProgressDlg = new ProgressDialog(this.context);

		mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				mBluetoothAdapter.cancelDiscovery();
			}
		});

		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.EXTRA_RSSI);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


		((MainActivity) context).registerReceiver(mReceiver, filter);


		if(!isSupportBle()){
			showUnsupported();
			//Toast.makeText(context, "your device not support BLE!", Toast.LENGTH_SHORT).show();
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
	
	public BLEDeviceListAdapter getDeviceListAdapter(){
		return mDevListAdapter;
	} 

	public boolean isSupportBle(){
		// is support 4.0 ?
		final BluetoothManager bluetoothManager = (BluetoothManager)
				context.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();	
		if (mBluetoothAdapter == null) 
			return false;
		else
			return true;			
	}
	
	public BluetoothAdapter getBluetoothAdapter(){
		return mBluetoothAdapter;
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
				mDevListAdapter.addDevice((BluetoothScanInfo) msg.obj);
				mDevListAdapter.notifyDataSetChanged();
			}
		}
    };
    
    // scan device
 	public void scanLeDevice(boolean enable) {
 		if (enable) {
 			mDevListAdapter.clearDevice();
 			mDevListAdapter.notifyDataSetChanged();

			// Stops scanning after a pre-defined scan period
			mHandler.postDelayed(new Runnable() {
 			@Override
 				public void run() {
// 					mScanning = false;
 					mBluetoothAdapter.stopLeScan(mLeScanCallback);
 					if(onScanListener != null){
 		        		onScanListener.onScanFinished();
 		        	}
					mBluetoothAdapter.startDiscovery();
 				}
 			}, SCAN_PERIOD);

 			mScanning = true;
 			mBluetoothAdapter.startLeScan(mLeScanCallback);
 		} else {
// 			mScanning = false;
 			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mBluetoothAdapter.startDiscovery();
		}
 	}
 	
 	public boolean isScanning(){
 		return mScanning;
 	}
 	
 	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                final byte[] scanRecord) {
        	
        	if(onScanListener != null){
        		onScanListener.onScan(device, rssi, scanRecord);
        	}
        	
            ((MainActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	Message msg = new Message();
                	BluetoothScanInfo info = new BluetoothScanInfo();
                	info.device = device;
                	info.rssi = rssi;
                	info.scanRecord = scanRecord;
                	msg.obj = info;
                	mHandler.sendMessage(msg);
                }
            });      
        }
    };
    
    public class BluetoothScanInfo{
    	public BluetoothDevice device;
    	public int rssi;
    	public byte[] scanRecord;
    };
    
    public static double calculateAccuracy(int txPower, double rssi) {
    	if (rssi == 0) {
    		return -1.0; // if we cannot determine accuracy, return -1.
    	}

    	double ratio = rssi*1.0/txPower;
    	if (ratio < 1.0) {
    		return Math.pow(ratio,10);
    	}
    	else {
    		double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;    
    		return accuracy;
    	}
    }  

	private void showMessage(String str){
		Toast.makeText(((MainActivity) context), str, Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_ON) {
					showMessage("Enabled");
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//				mDeviceList = new ArrayList<BluetoothDevice>();

				mProgressDlg.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mProgressDlg.dismiss();
				mScanning = false;


//				Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

//				newIntent.putParcelableArrayListExtra("device.list", mDeviceList);

//				startActivity(newIntent);
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				final BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				final int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

				((MainActivity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Message msg = new Message();
						BluetoothScanInfo info = new BluetoothScanInfo();
						info.device = device;
						info.rssi = rssi;
						info.scanRecord = null;
						msg.obj = info;
						mHandler.sendMessage(msg);
					}
				});

				showMessage("Found device " + device.getName());
			}
		}
	};


	private void showUnsupported() {
		showMessage("Bluetooth is unsupported by this device");
	}

}