package etag.rfid.bt;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
/*
 * 将每次读取的数据包解析成包含的第一个卡
 */
public class Unpack {
	private List<byte[]> mrfids=new ArrayList<byte[]>();
	private int rx_index = 0;
	private boolean bIsStart= false;
	private byte[] curRfidBytes=new byte[10];
	private boolean bLongPkt = false;
	public Unpack()
	{
		
	}
	/*
	 * 传入一个包的数据，得到包含的第个卡的信息。
	 */
	public List<byte[]> getRfids(byte[] sourcePack,int packLength)
	{
		mrfids.clear();
		for(int i=0;i<packLength&&i<sourcePack.length;i++)
		{
			if((sourcePack[i]&0xFF)==0xaa&&!bIsStart)
			{
				rx_index = 0;
				bIsStart = true;
				bLongPkt = false;
			}
			if(bIsStart)
			{
				rx_index++;
				switch(rx_index)
				{
					case 1://0xaa
						break;
					case 2:
						curRfidBytes[0]=sourcePack[i];
						if((curRfidBytes[0]&0xff)==0x82)
							bLongPkt = true;
						break;
					case 3:
						curRfidBytes[1]=sourcePack[i];
						break;
					case 4:
						curRfidBytes[2]=sourcePack[i];
						break;
					case 5:
						curRfidBytes[3]=sourcePack[i];
						break;
					case 6://状态字节
						if(bLongPkt)
						{
							curRfidBytes[6]=sourcePack[i];
						}
						else
						{
							curRfidBytes[4]=sourcePack[i];
						}
						break;
					case 7://rssi
						if(bLongPkt)
						{
							curRfidBytes[7]=sourcePack[i];
						}
						else
						{
							curRfidBytes[5]=sourcePack[i];
							mrfids.add(curRfidBytes);
							curRfidBytes=new byte[10];
							bIsStart = false;//重新开始新的解包
						}
						break;
					case 8://状态字节
						curRfidBytes[8]=sourcePack[i];
						break;
					case 9://状态字节
						curRfidBytes[9]=sourcePack[i];
						break;
					case 10://状态字节
						curRfidBytes[4]=sourcePack[i];
						break;
					case 11://rssi
						curRfidBytes[5]=sourcePack[i];
						mrfids.add(curRfidBytes);
						curRfidBytes=new byte[10];
						bIsStart = false;//重新开始新的解包
						break;
					default:
						bIsStart = false;//未知情况
						break;
				}
			}
		}
	//	Log.i("CCC",String.valueOf(rx_index));
		return mrfids;
	}	
}
