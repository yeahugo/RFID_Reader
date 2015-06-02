package etag.rfid.bt;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.bt]    
 *@ 类名称:    [CCardInfo]  
 *@ 类描述:    [卡的信息类]
 *@ 创建人:    []   
 *@ 创建时间:  []   
 *@ 修改人:    [廖]   
 *@ 修改时间:  [2015.4.2]   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 * 
 */
public class CCardInfo {
	private String mRfid;
	private int mRssi;
	private int mBattery;
	private Date mReadTime;
	private String time;
	private int t;
	private int mKey;
	private int mWRssi;
	private int mWakeNum;
	byte[] bRfid =new byte[4];
	private int cnt=0;
	private int listCount = -1;
	private int XYZ = -1;
	
	public int getXYZ() {
		//0 x:4 y:2 z:1 xy:6 xz5 yz:3 xyz:7
		setXYZ(mBattery);
		return XYZ;
	}
	public void setXYZ(int mBattery) {
		if(t == 130 || t == 132){
			switch(XYZ){
			
			case 6:
				if(mBattery==3)
					XYZ = XYZ + 1;
				break;
			case 5:
				if(mBattery==2)
					XYZ = XYZ + 2;
				break;
			case 3:
				if(mBattery==1)
					XYZ = XYZ + 4;
				break;
			case 7:
				break;
			case 4:
				
				XYZ = mBattery+XYZ;
				break;
			case 2:
		
				XYZ = mBattery+XYZ;
				break;
			case 1:
				XYZ = mBattery+XYZ;
				break;
			case 0:
				XYZ = mBattery;
				break;
			case -1:
				XYZ = mBattery;
				break;
			default:
				
				break;
			}
			Log.i("+++++++++++++++-", ""+XYZ);
		}else
		XYZ = -1;
	}
	public int getListCount(){
		return listCount;
	}
	public int getT(){
		return t;
	}
	public void setListCount(int listCount){
		if(listCount!=-1)
		this.listCount = listCount;
	}
	
	public void setRssi(int rssi){
		mRssi = rssi;
	}
	public void setRead(String time){
		this.time = time;
	}
	public CCardInfo()
	{
		time = "";
	}
	public CCardInfo(String rfid,int rssi,int battery,int key,int Wrssi,int WN,Date readTime)
	{
		mRfid=rfid;
		mRssi=rssi;
		mBattery=battery;
		mReadTime=readTime;
		mKey = key;
		mWRssi = Wrssi;
		mWakeNum = WN;
		time = "";
	}
	public void InitialCard(byte[] cardBytes)
	{
		int rfid;
		t = (cardBytes[0]&0xFF);
		rfid = t<<24;
		
		rfid += (cardBytes[1]&0xFF)<<16;
		rfid += (cardBytes[2]&0xFF)<<8;
		rfid += cardBytes[3]&0xFF;
		mRfid=Integer.toHexString(rfid);
		mRssi=(int)cardBytes[5];
		mBattery=(int)(cardBytes[4]&0x03);
		
		mKey = (int)(cardBytes[4]&0x04);
		if((cardBytes[0]&0xff)==0x82)
		{
			mWRssi = (int)cardBytes[7];
			mWakeNum = (int)(((cardBytes[8]&0xff)<<8) + (cardBytes[9]&0xff));
		}
		else
		{
			mWRssi = 0;
			mWakeNum = 0;
		}
		mReadTime=new Date();
	}
	public void setRfid(String mRfid)
	{
		this.mRfid = mRfid;
	}
	public String getRfid()
	{
		return mRfid;
	}
	public int getRssi(boolean t){
		return mRssi;
	}
	public int getRssi()
	{
		
		if(mRssi < 128)
		{
			mRssi= (74-mRssi/2);
		}
		else
		{
			mRssi= (74+((255-mRssi)+1)/2);
		}
		
		return mRssi;
	}
	public int getBattery()
	{
		
		if(t == 130 || t == 132){
			if(mBattery < 1)
				return 0;
			else
				return 3;
		}
		
		return mBattery;
	}
	public int getBattery1()
	{
		return mBattery;
	}
	public void setBattery(int pwd)
	{
		mBattery = pwd;
	}
	@SuppressLint("SimpleDateFormat")
	public String GetReadTime()
	{
		if(time!="") return time;
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		return fmt.format(mReadTime);
	}
	public void setCnt(){
		cnt = cnt+1;
	}
	public int getCnt(){
		
		return cnt;
	}
	public int getWRssi()
	{
		return mWRssi;
	}
	public void setWRssi(int mWRssi) {
		this.mWRssi = mWRssi;
	}
	public void setWN(int mWakeNum) {
		this.mWakeNum = mWakeNum;
	}
	public int getWN()
	{
		return mWakeNum;
	}
	@Override
	protected CCardInfo clone() throws CloneNotSupportedException {
		super.clone();
		CCardInfo card =new CCardInfo(this.mRfid,this.mRssi,this.mBattery,this.mKey,this.mWRssi,this.mWakeNum,this.mReadTime);
		return card;
	}
}
