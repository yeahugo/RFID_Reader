package etag.rfid.ui;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request.Method;

import com.etag.R;

import etag.rfid.adapter.ListAdaptet;
import etag.rfid.bt.AfdBT;
import etag.rfid.bt.BluetoothCommService;
import etag.rfid.bt.CCardInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.ui]    
 *@ 类名称:    [ShowCard]  
 *@ 类描述:    [卡信息显示活动]
 *@ 创建人:    [廖]   
 *@ 创建时间:  [2015.4.2]   
 *@ 修改人:    []   
 *@ 修改时间:  []   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 * 
 */
@SuppressLint("HandlerLeak")
public class ShowCard extends Activity implements IReadCard,OnClickListener{

    private double latitude = 0.0;
    private double longitude = 0.0;

    private  static final int MaxSentListSize = 10;

    // Debugging
    private static final String TAG = "BluetoothComm";
    private static final boolean D = true;
    //请求开启蓝牙的requestCode
	static final int REQUEST_ENABLE_BT = 1;
	//请求连接的requestCode
	static final int REQUEST_CONNECT_DEVICE = 2;
	//bluetoothCommService 传来的消息状态
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
	
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    //蓝牙设备
	private BluetoothDevice device = null;
	//本地蓝牙适配器
	private BluetoothAdapter bluetooth;
	//创建一个蓝牙串口服务对象
	private BluetoothCommService mCommService = null;
	
	
	private Handler mhandler;

    private HashMap<Integer,HashMap<String,CCardInfo>> firstCards = new HashMap<Integer, HashMap<String,CCardInfo>>();
    private HashMap<Integer,HashMap<String,CCardInfo>> secondCards = new HashMap<Integer, HashMap<String,CCardInfo>>();
    private HashMap<Integer,HashMap<String,CCardInfo>> sendCards = new HashMap<Integer, HashMap<String,CCardInfo>>();

    private List<HashMap<Integer,HashMap<String,CCardInfo>>> sendCardLists = new ArrayList<HashMap<Integer,HashMap<String,CCardInfo>>>();

	private ListView lvShow;//显示卡信息列表
	private ListAdaptet lsAdapter = null;//卡信息适配器
	private List<CCardInfo> lsitCardInfo = new ArrayList<CCardInfo>();//卡装载容器
	private List<CCardInfo> TempCardInfo = new ArrayList<CCardInfo>();//搜索时卡容器
	private String filterStr = "";//过滤字符串
	private Button btnMode;//模式按键
	private EditText EdtCard;//卡输入框
	private Button btnT;//搜索卡按键
	private EditText EdtRssi;//RSSI过滤输入框
	private int count=0;//添加卡的数量
	private int FilterFlag = -1;//搜索过滤标记
	private int FilterRssi = 0;//RSSI过滤标记
	private boolean isLookCard = false;//是否加卡模式还是普通模式
    int filterCount= 0;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_card);

		bluetooth = AfdBT.GetAdapter();
		device = bluetooth.getRemoteDevice(AfdBT.getAddress());

		mhandler=new Handler() {
			boolean t = false;
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) 
			{ 
				switch (msg.what) 
				{ 
				case 1:
					if(!t){
						t = true;
						lvShow.setVisibility(View.VISIBLE);
						((ImageView)findViewById(R.id.card_img_info)).setVisibility(View.GONE);
					}
					Bundle bundle=msg.getData();
    				updateListView(bundle.getString("rfid"),bundle.getInt("rssi"),bundle.getString("time"),bundle.getInt("pwd"),bundle.getInt("wn"),bundle.getInt("wrssi"));
			//		refresUi();
					break; 
				case 2:
					finsh();
					break;
				default: 
					break; 
				}
				super.handleMessage(msg);
		//		ccm.readCard();
			} 
		};
		initView();	
	}
	private void finsh()
	{
	/*	Intent intent = new Intent(ShowCard.this,ScanDeviceActivity.class);
		startActivity(intent);*/
		this.finish();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(!bluetooth.isEnabled()){
    		//请求打开蓝牙设备
    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    	} else {
    		if(mCommService==null) {
    			mCommService = new BluetoothCommService(this, mHandler);
    			BluetoothCommService.AddCardReceiver(this);
    		}
    	}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 if (mCommService != null) {
	            // Only if the state is STATE_NONE, do we know that we haven't started already
	            if (mCommService.getState() == BluetoothCommService.STATE_NONE) {
	              // Start the Bluetooth services，开启监听线程
	            	mCommService.start();
	            	 mCommService.connect(device);
	            }
	        }
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		  if (mCommService != null) mCommService.stop();
	        if(D) Log.e(TAG, "--- ON DESTROY ---");
	}

    private boolean isCardsEqual(HashMap<Integer,HashMap<String,CCardInfo>> firstCards,HashMap<Integer,HashMap<String,CCardInfo>> secondCards){
        boolean result = true;

        if (firstCards == null && secondCards == null){
            return true;
        }

        HashMap<String,CCardInfo> firstCardMap = firstCards.get(firstCards.keySet().toArray()[0]);
        HashMap<String,CCardInfo> secondCardMap = secondCards.get(secondCards.keySet().toArray()[0]);
        if (firstCardMap.size() != secondCardMap.size()){
            result = false;
            return result;
        } else {
            for (String s:firstCardMap.keySet()){
                if (!secondCards.containsKey(s)){
                    result = false;
                    return result;
                }
            }
        }
        return result;
    }

    private void postCardsData(List<HashMap<Integer,HashMap<String,CCardInfo>>> sendCardList)
    {
        String url = "";
        mRequestQueue = Volley.newRequestQueue(this);
        mStringRequest = new StringRequest(Request.Method.POST,url,new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.v("response",s);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.v("error is ",volleyError.toString());
            }
        });
    }

    private void updateCardsList(String rfid,int rssi,String time,int pwd,int wn,int wrssi)
    {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Integer timeInteger = 0;
        try {
            Date date = formatter.parse(time);
            timeInteger = new Integer(String.valueOf(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CCardInfo card = CardInfo(rfid, rssi, time, pwd, wn, wrssi);
        if (firstCards.containsKey(timeInteger)){
            HashMap<String,CCardInfo> cardMap = firstCards.get(timeInteger);
            cardMap.put(rfid,card);
        } else if(secondCards.containsKey(timeInteger)){
            HashMap<String,CCardInfo> cardMap = secondCards.get(timeInteger);
            cardMap.put(rfid,card);
        } else {
            HashMap<String,CCardInfo> cardMap = new HashMap<String,CCardInfo>();
            cardMap.put(rfid,card);

            if (firstCards.keySet().size() == 0){
                firstCards.put(timeInteger,cardMap);
            } else if (secondCards.keySet().size() == 0) {
                secondCards.put(timeInteger,cardMap);
            } else {
                Integer firstKey = (Integer) firstCards.keySet().toArray()[0];
                Integer secondKey = (Integer) secondCards.keySet().toArray()[0];

                if (firstKey <= secondKey){
                    if (!isCardsEqual(firstCards,secondCards)){
                        sendCards = secondCards;
                    }
                    firstCards.clear();
                    firstCards.put(timeInteger,cardMap);
                } else {
                    if (!isCardsEqual(firstCards,secondCards)){
                        sendCards = firstCards;
                    }
                    secondCards.clear();
                    firstCards.put(timeInteger,cardMap);
                }
                sendCardLists.add(sendCards);
            }
        }

        if (sendCardLists.size() > MaxSentListSize){
            List<HashMap<Integer,HashMap<String,CCardInfo>>> tempSendList = new ArrayList(sendCardLists);
            postCardsData(tempSendList);
            sendCardLists.clear();
        }
    }

	@SuppressLint("ShowToast")
	private void updateListView(String rfid,int rssi,String time,int pwd,int wn,int wrssi){
		boolean b=false;

        updateCardsList(rfid,rssi,time,pwd,wn,wrssi);

        Log.i("sendCards is ",sendCards.toString());

//		Log.i("kkk", ">>>>>>>>"+rfid+"------"+""+rssi+"------"+time);
		//Log.i("ReadCardsActivity", targetId);
		if(filterStr.length() > 7){
			if(filterStr.equals(rfid)){
				if(FilterFlag != 1){
				}else{
					CCardInfo h = TempCardInfo.get(0);
					h.setCnt();
					h.setRead(time);
					h.setRssi(rssi);
					h.setBattery(pwd);
					h.setWN(wn);
					h.setWRssi(wrssi);
				}
				lsAdapter = new ListAdaptet(TempCardInfo, this);
		        lvShow.setAdapter(lsAdapter);
			}
			return;
		}
		for(int i = 0; i < lsitCardInfo.size(); i++){
			if(rfid.equals(lsitCardInfo.get(i).getRfid())){
				lsitCardInfo.get(i).setCnt();
				lsitCardInfo.get(i).setRead(time);
				lsitCardInfo.get(i).setRssi(rssi);
				lsitCardInfo.get(i).setBattery(pwd);
				lsitCardInfo.get(i).setWN(wn);
				lsitCardInfo.get(i).setWRssi(wrssi);
				b=true;
				if(FilterRssi != 0)
				break;
			}
			if(FilterRssi > 0){
				if(lsitCardInfo.get(i).getRssi(true) < FilterRssi)
				{
					lsitCardInfo.remove(i);
				}
			}
		}
		if(filterStr == ""){
			if(!b){
				if(FilterRssi <= 0)
					count++;
				if(FilterRssi > 0){
					if(rssi > FilterRssi)
					{
						lsitCardInfo.add(CardInfo(rfid, rssi, time, pwd, wn, wrssi));
					}
				}else{

					lsitCardInfo.add(CardInfo(rfid, rssi, time, pwd, wn, wrssi));
				}
                Log.v("find new card ",rfid);
                Log.v("lsitCardInfo is ",lsitCardInfo.toString());
			}
		}
		if(FilterFlag == 1){
			TempCardInfo.clear();
			lsAdapter = new ListAdaptet(lsitCardInfo, this);
	        lvShow.setAdapter(lsAdapter);
	        FilterFlag = -1;
	        return;
		}
		
		lsAdapter.notifyDataSetChanged();
		if(isLookCard)
		lvShow.setSelection(lsAdapter.getCount());
	}

	private CCardInfo CardInfo(String rfid, int rssi, String time, int pwd, int wn, int wrssi){
		CCardInfo h = new CCardInfo();
		h.setRfid(rfid);
		h.setCnt();
		h.setRead(time);
		h.setRssi(rssi);
		h.setListCount(count);
		h.setBattery(pwd);
		h.setWN(wn);
		h.setWRssi(wrssi);
		return h;
	}
	
	@SuppressLint("NewApi")
	private void initView(){
		lvShow = (ListView) findViewById(R.id.card_listview);
		if(lsAdapter == null){
			lsAdapter = new ListAdaptet(lsitCardInfo, this);
		}
		//添加并且显示  
        lvShow.setAdapter(lsAdapter);

        ((Button) findViewById(R.id.card_info_btn_clear)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				count=0;
				
				lvShow.setSelection(0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FilterRssi = 0;
				lsitCardInfo.clear();

			}
		});
        
        ((TextView) findViewById(R.id.top_tv)).setText("读卡列表");
        
        Button btnTop = ((Button) findViewById(R.id.top_btn));
        btnTop.setText("返回");
        btnTop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(mCommService!=null){
					mCommService.stop();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = mhandler.obtainMessage(2);
				msg.sendToTarget();
				//System.exit(0);
			}
		});
        //RSSi过滤
        EdtRssi = (EditText) findViewById(R.id.card_search_rssi);
        ((Button) findViewById(R.id.card_rssi_btn)).setOnClickListener(this);
        //卡号
        EdtCard = (EditText) findViewById(R.id.card_search_card);
        btnT = ((Button) findViewById(R.id.card_card_btn));
        btnT.setOnClickListener(this);
        btnMode = ((Button) findViewById(R.id.show_card_mode));
        btnMode.setOnClickListener(this);
        lvShow.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				CCardInfo v = (CCardInfo)lvShow.getItemAtPosition(position);
				Intent intent = new Intent(ShowCard.this,CCardInfoActivity.class);
				intent.putExtra("cpid", v.getRfid());
				intent.putExtra("rssi", v.getRssi(true));
				intent.putExtra("time", v.GetReadTime());
				intent.putExtra("pwd", v.getBattery());
				startActivity(intent);
		
			}
		});
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void ReadCard(CCardInfo cardInfo) {
		Message msg = mhandler.obtainMessage(1);
		 Bundle bundle = new Bundle();
	     bundle.putString("rfid", cardInfo.getRfid());  
	     bundle.putInt("rssi", cardInfo.getRssi()); 
	     bundle.putString("time", cardInfo.GetReadTime());
	     bundle.putInt("pwd", cardInfo.getBattery());
	     bundle.putInt("wn", cardInfo.getWN());
	     bundle.putInt("wrssi", cardInfo.getWRssi());
	     msg.setData(bundle);
         msg.sendToTarget();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(mCommService!=null){
				mCommService.stop();
			}
		
			this.finsh();
            return super.onKeyDown(keyCode, event);
        }
        return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.show_card_mode://模式
			if("滚动模式".equals(btnMode.getText().toString())){
				isLookCard = true;
				btnMode.setText("非滚模式");
			}else{
				isLookCard = false;
				btnMode.setText("滚动模式");
			}
			break;
		case R.id.card_card_btn: //搜索
			
			if("搜  索".equals(btnT.getText().toString())){
				filterStr = EdtCard.getText().toString();
				if("".equals(filterStr) || filterStr.length() < 8)
				{
					Toast.makeText(this, "请输入8位卡号！", 0).show();
					filterStr = "";
					break;
				}
					
				btnT.setText("清  除");
				
				for(CCardInfo h : lsitCardInfo){
					if(filterStr.equals(h.getRfid())){
						TempCardInfo.add(h);
						FilterFlag = 1;
						break;
					}
				}
				if(FilterFlag!=1){
					filterStr = "";
					btnT.setText("搜  索");
					Toast.makeText(this, "没有找到卡！", 0).show();
					return;
				}
				lsAdapter = new ListAdaptet(TempCardInfo, this);
		        lvShow.setAdapter(lsAdapter); 
			
			}else{
				filterStr = "";
				btnT.setText("搜  索");
			}
				
			break;
		case R.id.card_rssi_btn://rssi过滤
			String s = EdtRssi.getText().toString().trim();
			try{
				count=0;
				filterStr = "";
				lsitCardInfo.clear();	
				if(s != null){
					FilterRssi=Integer.valueOf(s).intValue();
					}
				else{
					FilterRssi = 0;
				}
				}catch(Exception e){
					FilterRssi = 0;
				}
			break;
		}
	}
	 private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MESSAGE_STATE_CHANGE:
	                break;
	            case MESSAGE_WRITE:
	           
	                break;
	            case MESSAGE_READ:
	              
	                break;
	            case MESSAGE_DEVICE_NAME:
	          //      Toast.makeText(getApplicationContext(), "Connected to ", Toast.LENGTH_SHORT).show();
	                break;
	            case MESSAGE_TOAST:
	            	
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                               Toast.LENGTH_SHORT).show();
	               finsh();
	                break;
	            }
	        }
	    };

}
