package etag.rfid.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.etag.R;

import etag.rfid.bt.AfdBT;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ScanDeviceActivity extends Activity{
    // Debugging
    private static final String TAG = "BluetoothComm";
    private static final boolean D = true;
    
	// Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    //没有搜到新设备设为1，搜到新设备为0
    private int noDeviceFlag = 0;

    // Member fields
    private BluetoothAdapter bluetooth;
    private ArrayList<String> newDevices = new ArrayList<String>();
    private ListView newDevicesList;
    private ListView pairedDevicesList;
    private ArrayAdapter<String> pairedDevicesAdapter;
    private ArrayAdapter<String> newDevicesAdapter;
    private Button scanButton;

 //   private ProgressBar pbShow;//进度条
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.serach);
        initBt();
        scanButton = (Button)findViewById(R.id.search_btn_search);
        
        final Handler handler = new Handler(){
        	public void handleMessage(Message msg) 
			{ 
				switch (msg.what) 
				{ 
				case 159:
					scanButton.setBackgroundResource(R.drawable.btn_backg1);
					break; 
				}
				super.handleMessage(msg);
		//		ccm.readCard();
			} 
		};
        
        
        
     
        scanButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				Message msg = handler.obtainMessage(159);
				msg.sendToTarget();

				doDiscovery();
			}
		});
        
        ((Button)findViewById(R.id.top_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				exitDialog();
			}
		});
        
	}
	private void initDate(){
		  // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
  //      pbShow = (ProgressBar) findViewById(R.id.search_progressBar);
       
       
        newDevicesList = (ListView)findViewById(R.id.newDevices);
        newDevicesAdapter = new ArrayAdapter<String>(this, 
                        android.R.layout.simple_list_item_1,
                        newDevices);
        newDevicesList.setAdapter(newDevicesAdapter);
        newDevicesList.setOnItemClickListener(mNewDeviceClickListener);
        
        // Find and set up the ListView for paired devices
        pairedDevicesList = (ListView)findViewById(R.id.pairedDevices);
        pairedDevicesAdapter = new ArrayAdapter<String>(this, 
                android.R.layout.simple_list_item_1);
        pairedDevicesList.setAdapter(pairedDevicesAdapter);
        //当点击时，启动线程connect
        pairedDevicesList.setOnItemClickListener(mDeviceClickListener);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
     // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
         //   findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
               
                String str = device.getName() + "\n" + device.getAddress();
           	 	boolean l = false;
           	 	for(int i = 0; i < pairedDevicesAdapter.getCount(); i++){
	           		if(pairedDevicesAdapter.getItem(i).equals(str)){
	           			l = true;
	           			break;
	           		}
	           	}
	           	if(!l)
	           	 pairedDevicesAdapter.add(str);
            }
        } else {
            String noDevices = getResources().getText(R.string.noPairedDevice).toString();
            pairedDevicesAdapter.add(noDevices);
        }
	}
	private void initBt(){	
		if(bluetooth == null){
			bluetooth = BluetoothAdapter.getDefaultAdapter();
		}
		if(bluetooth==null)
        {
        	Toast.makeText(getApplicationContext(), "此设备不支持蓝牙！",Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }else{
        	if(!bluetooth.isEnabled()){
        		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        		startActivityForResult(intent, 100);
        	}else{
        		initDate();
        	}
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
	//	lvShow.removeAllViews();
		super.onActivityResult(requestCode, resultCode, data);
		if(100 == requestCode){
			initDate();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Make sure we're not doing discovery anymore
        if (bluetooth != null) {
            bluetooth.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
	}


	private void doDiscovery(){
		// Indicate scanning in the title
	
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        if(bluetooth.isDiscovering()){
        	bluetooth.cancelDiscovery();
        }
		bluetooth.startDiscovery();
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			  String action = intent.getAction();

	          //当发现设备
	          if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	             // Get the BluetoothDevice object from the Intent
	             BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	             noDeviceFlag = 0;
	             //如果设备已经配对，则忽略
	             if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	            	 String str = device.getName() + "\n" + device.getAddress();
	            	 boolean l = false;
	            	for(int i = 0; i < newDevicesAdapter.getCount(); i++){
	            		if(newDevicesAdapter.getItem(i).equals(str)){
	            			l = true;
	            			break;
	            		}
	            	}
	            	if(!l)
	            		newDevicesAdapter.add(str);
	             }
	            // When discovery is finished, change the Activity title
	          } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	                setProgressBarIndeterminateVisibility(false);
	                setTitle(R.string.select_device);
	                if (newDevicesAdapter.getCount() == 0) {
	                    String noDevices = getResources().getText(R.string.noNewDevice).toString();
	                    newDevicesAdapter.add(noDevices);
	                    noDeviceFlag = 1;
	                }
	                else {
	                	noDeviceFlag = 0;
	                }
                    if(D) Log.d(TAG, "搜索完成noDeviceFlag="+noDeviceFlag);
	            }
		}
		
	};
	
	/**
	 * the ItemClickListener for all devices in the two ListViews
	 */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            //当选择连接之后，关闭搜索过程，此过程消耗比较大
            bluetooth.cancelDiscovery();

            // 从点击的Item中获得Mac地址，由于Mac地址格式可以其为最后的17位
            String info = ((TextView) v).getText().toString();
            if("no paired device".equals(info)) return;
            String address = info.substring(info.length() - 17);
            AfdBT.setAddress(address);
            AfdBT.SetAdapter(bluetooth);
            // Create the result Intent and include the MAC address
            Intent it = new Intent(ScanDeviceActivity.this, ShowCard.class);
	        startActivity(it); 
         //   finish();
            
        }
    };
    
    private OnItemClickListener mNewDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            //当选择连接之后，关闭搜索过程，此过程消耗比较大
            bluetooth.cancelDiscovery();

            // 从点击的Item中获得Mac地址，由于Mac地址格式可以其为最后的17位
            //添加noDeviceFlag标志，防止在没有搜索到设备时仍然选择连接而造成程序中断的bug
            if(noDeviceFlag==0) {
            	String info = ((TextView) v).getText().toString();
            	if("no new device".equals(info)) return;
            	String address = info.substring(info.length() - 17);
                // Create the result Intent and include the MAC address
            	AfdBT.setAddress(address);
                AfdBT.SetAdapter(bluetooth);
                  // Create the result Intent and include the MAC address
                Intent it = new Intent(ScanDeviceActivity.this, ShowCard.class);
      	        startActivity(it);     
            }
          //  finish();
            
        }
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//处理返加键按下事件
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	//确认退出
        	exitDialog();
            return false;
        }
        return false;
    }
    private void exitDialog()
    {
	    new AlertDialog.Builder(ScanDeviceActivity.this)
	    .setTitle("提示")
	    .setMessage("确定退出吗？")
	    .setPositiveButton("确定",
	      new DialogInterface.OnClickListener() {
	       @Override
	       public void onClick(DialogInterface arg0,
	         int arg1) {
	    	   
	       finish();
	       }
	      })
	    .setNegativeButton("取消",
	      new DialogInterface.OnClickListener() {
	       @Override
	       public void onClick(DialogInterface arg0,
	         int arg1) {
	       }
	      }).show();
    }
}
