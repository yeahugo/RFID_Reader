package etag.rfid.bt;

import etag.rfid.ui.IReadCard;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.bt]    
 *@ 类名称:    [CReadCardManage]  
 *@ 类描述:    [蓝牙配对可读卡管理类]
 *@ 创建人:    [廖]   
 *@ 创建时间:  [2015.4.2]   
 *@ 修改人:    []   
 *@ 修改时间:  []   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 *@ 
 */
public class CReadCardManage {

	private BluetoothDevice dv;
	private CConnectBt connectThread;
	
	private static CReadCard readCardThread;
	private BluetoothSocket socket=null;
	public CReadCardManage() {
		// TODO Auto-generated constructor stub
		
	}
	//开启.........
	
	//1.配对
	/*
	 * 连接线程
	 * 
	 */
	public CReadCardManage(BluetoothDevice dv,Context context) {
		// TODO Auto-generated constructor stub
		this.dv = dv;
		if(readCardThread == null)
			readCardThread = new CReadCard();
		
	}
	
	public boolean connectStart(){
		connectThread = new CConnectBt(dv);
		connectThread.start();
		socket = connectThread.GetSocket();
		if(socket == null){
			
			return false;
		}
		readCardThread.setClose(true);
		readCardThread.setSocket(socket);
		readCardThread.start();
		return true;
	}
	//2.获取数据

	public boolean readClose(){
		readCardThread.setClose(false);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readCardThread.cancel();
		readCardThread=null;
	//	System.exit(0);
		return false;
	}
	public boolean readCard(){
		readCardThread.setSocket(socket);
		readCardThread.start();
		return true;
	}
	//3.解析数据
	
	//4.数据适配
	//5.是否有筛选数据
	//6.更新ui显示数据
	//7.连接是否断开
	//8.重复2步
	//关闭...........
	//关闭连接
	//清理数据
	//关闭线程，释放资源

	
	/*
	 * 添加读卡观察者
	 */
	public void AddCardReceiver(IReadCard iReceiver)
	{
		readCardThread.AddCardReceiver(iReceiver);
	}
	/*
	 * 删除读卡观察者
	 */
	public void RemoveCardReceiver(IReadCard iReceiver)
	{
		readCardThread.RemoveCardReceiver(iReceiver);
	}
}
