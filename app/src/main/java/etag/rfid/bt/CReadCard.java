package etag.rfid.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import etag.rfid.ui.IReadCard;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.bt]    
 *@ 类名称:    [CReadCard]  
 *@ 类描述:    [读卡线程类]
 *@ 创建人:    []   
 *@ 创建时间:  []   
 *@ 修改人:    [廖]   
 *@ 修改时间:  [2015.4.2]   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 * 
 */
public class CReadCard extends Thread{
	 private BluetoothSocket mmSocket; 
     private InputStream mmInStream; 
     private OutputStream mmOutStream;
     private ArrayList<IReadCard> cardReceivers=new ArrayList<IReadCard>();
     private boolean isClose = false;
     public void  setClose(boolean isClose){
    	 this.isClose = isClose;
     }
     public void setSocket(BluetoothSocket socket){
    	 mmSocket = socket; 
         InputStream tmpIn = null; 
         OutputStream tmpOut = null; 
        
         try { 
             tmpIn = socket.getInputStream(); 
             tmpOut = socket.getOutputStream(); 
         } catch (IOException e) { } 
         mmInStream = tmpIn; 
         mmOutStream = tmpOut; 
     }
     public CReadCard() { 
     } 
     //每个数据包的大小为256
     
   
		@SuppressLint("UseValueOf")
		public void run() { 
         int bytes; // bytes returned from read() 
         // Keep listening to the InputStream until an exception occurs 
     //    while (_status==BTReaderStatus.Open&&mmSocket!=null) { 
         try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         
        while (mmSocket!=null && isClose) {
             try {
             	byte[] buffer = new byte[1024];
         
                 bytes = mmInStream.read(buffer);
              
                 if(bytes>0)
                 {
                 	//处理收到的包数据
                 	ResolveReceiveBuf(buffer,bytes);
                 }
                 
             } catch (IOException e) { 
         //    	ResolveMessage(e.getMessage());
            	 System.exit(0);
                 break; 
             } 
             try {
            	 	
            	 	Thread.sleep(new Long(500));//500
            	 	
				} catch (InterruptedException ex) {
			//		ResolveMessage(ex.getMessage());
					ex.printStackTrace();
				}
             
         } 
     } 
     /* Call this from the main Activity to send data to the remote device */ 
     public void write(byte[] bytes) { 
         try { 
             mmOutStream.write(bytes); 
         } catch (IOException e) { } 
     } 
     /* Call this from the main Activity to shutdown the connection */ 
     public void cancel() { 
         try { 
         	if(mmSocket!=null)
         		mmSocket.close(); 
         } catch (IOException e) { } 
   
     } 
     
     private Unpack mUnpack=new Unpack();
 	/*
 	 * 读卡线程读到数据之后解析数据
 	 * 并通知每个观察者
 	 */
 	CCardInfo card=new CCardInfo();
	private void ResolveReceiveBuf(byte[] bytes,int pLength)
 	{
 		try
 		{
 			
 			List<byte[]> bs=mUnpack.getRfids(bytes,pLength);
 			for(byte[] tmp:bs)
 			{
 				
 				card.InitialCard(tmp);
 	
	 			for(IReadCard receiver : cardReceivers)
 				{
 					receiver.ReadCard(card);
 				}
 			}
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
	public void AddCardReceiver(IReadCard iReceiver)
	{
		cardReceivers.add(iReceiver);
	}
	/*
	 * 删除读卡观察者
	 */
	public void RemoveCardReceiver(IReadCard iReceiver)
	{
		cardReceivers.remove(iReceiver);
	}
	
	
}

