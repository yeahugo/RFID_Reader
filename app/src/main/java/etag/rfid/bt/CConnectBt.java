package etag.rfid.bt;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.bt]    
 *@ 类名称:    [CConnectBt]  
 *@ 类描述:    [蓝牙配对类]
 *@ 创建人:    []   
 *@ 创建时间:  []   
 *@ 修改人:    [廖]   
 *@ 修改时间:  [2015.4.2]   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 * 
 */
public class CConnectBt extends Thread{
	 private  BluetoothSocket mmSocket; 
	 private  BluetoothDevice mmDevice;
	 private final UUID _conUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	 
	    public CConnectBt(BluetoothDevice device) { 
	        // Use a temporary object that is later assigned to mmSocket, 
	        // because mmSocket is final 
	        BluetoothSocket tmp = null; 
	        mmDevice = device; 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice 
	        try { 
	            // MY_UUID is the app's UUID string, also used by the server code 
	            tmp = mmDevice.createRfcommSocketToServiceRecord(_conUUID); 
	        } catch (IOException e) {
	   //     	ResolveMessage(e.getMessage());
	        } 
	        mmSocket = tmp;
	    } 
	    public BluetoothSocket GetSocket()
	    {
	    	return mmSocket;
	    }
	    public void run() { 
	        // Cancel discovery because it will slow down the connection 
	        //mBluetoothAdapter.cancelDiscovery(); 
	    	
	        try { 
	            // Connect the device through the socket. This will block 
	            // until it succeeds or throws an exception 
	        	if(mmSocket!=null)
	            mmSocket.connect();
	 //           startReading();
	        } catch (Exception connectException) { 
	            // Unable to connect; close the socket and get out 
	//        	ResolveMessage(connectException.getMessage());
	            try { 
	                mmSocket.close();
	            } catch (IOException closeException) {
	  //          	ResolveMessage(connectException.getMessage());
	            } 
	            return; 
	        } 
	        
	    } 
	    /** Will cancel an in-progress connection, and close the socket 
	     * @throws IOException */ 
	    public void cancel() throws IOException { 
	            mmSocket.close(); 
	    } 
}
