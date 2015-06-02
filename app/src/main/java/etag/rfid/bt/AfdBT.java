package etag.rfid.bt;

import java.util.ArrayList;
import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.bt]    
 *@ 类名称:    [AfdBT]  
 *@ 类描述:    [用于在多个Activity间共享数据]
 *@ 创建人:    []   
 *@ 创建时间:  []   
 *@ 修改人:    [廖]   
 *@ 修改时间:  [2015.4.2]   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 * 
 */
public class AfdBT{
	private static BluetoothDevice _device;
	private static ArrayList<HashMap<String, String>> _deviceList;
	private static BluetoothAdapter _adapt;
	private static String address = null;
	
	public static String getAddress() {
		return address;
	}
	public static void setAddress(String address) {
		AfdBT.address = address;
	}
	public static BluetoothDevice getBTDevice()
	{
		return _device;
	}
	public static void setBTDevice(BluetoothDevice iDevice)
	{
		_device=iDevice;
	}
	
	public static ArrayList<HashMap<String, String>> GetDeviceList()
	{
		return _deviceList;
	}
	public static void SetDeviceList(ArrayList<HashMap<String, String>> iDeviceList)
	{
		_deviceList=iDeviceList;
	}
	
	public static BluetoothAdapter GetAdapter()
	{
		return _adapt;
	}
	public static void SetAdapter(BluetoothAdapter iAdapter)
	{
		_adapt=iAdapter;
	}
}
