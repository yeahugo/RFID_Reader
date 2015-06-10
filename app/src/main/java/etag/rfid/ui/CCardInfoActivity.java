package etag.rfid.ui;

import com.goatkeeper.R;

import etag.rfid.bt.AfdBT;
import etag.rfid.bt.BluetoothCommService;
import etag.rfid.bt.CCardInfo;
import etag.rfid.bt.CReadCardManage;
import etag.rfid.ui.stub.SpringProgressView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**   
 * 
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.ui]    
 *@ 类名称:    [CCardInfoActivity]  
 *@ 类描述:    [卡详细信息显示活动]
 *@ 创建人:    [廖]   
 *@ 创建时间:  [2015.4.2]   
 *@ 修改人:    []   
 *@ 修改时间:  []   
 *@ 修改备注:  []  
 *@ 版本:      [v1.0]   
 * 
 */
public class CCardInfoActivity extends Activity implements IReadCard{

	private Handler mhandler;
	private String sCpid;
	private  TextView cpid,rssi,readTime,cnt,pwd,wn,wrssi,pow;
	private int iRssi;
	private int XYZ = -1;
	private int XY = -1;
	private ImageView imgPower;
	private ImageView imgCur0;
	private FrameLayout imgCur;
	
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_info_view);
		Intent intent = this.getIntent();
		sCpid = intent.getStringExtra("cpid");
		iRssi = intent.getIntExtra("rssi",50);
		
		progressView = (SpringProgressView) findViewById(R.id.spring_progress_view);
		progressView.setMaxCount(150.0f);
		mhandler=new Handler() {
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) 
			{ 
				switch (msg.what) 
				{ 
				case 1:
					Bundle bundle=msg.getData();
					XYZ = bundle.getInt("XYZ");
    				updateListView(bundle.getString("rfid"),bundle.getInt("rssi"),bundle.getString("time"),bundle.getInt("pwd"),bundle.getInt("wn"),bundle.getInt("wrssi"));
			//		refresUi();
					break; 
				default: 
					break; 
				}
				super.handleMessage(msg);
		//		ccm.readCard();
			} 
		};
		
		imgCur = (FrameLayout) findViewById(R.id.card_info_img_cur);
		imgCur0 = (ImageView) findViewById(R.id.card_info_img_cur0);
		
		pow = ((TextView)findViewById(R.id.item_pow));
		
		((TextView)findViewById(R.id.item_id)).setText(" ");
		cpid = (TextView) findViewById(R.id.item_cpid);
		this.rssi = (TextView)findViewById(R.id.item_rssi);
		readTime = (TextView) findViewById(R.id.item_time);
		cnt = (TextView)findViewById(R.id.item_cnt);
		pwd = (TextView)findViewById(R.id.item_pwd);
		imgPower = (ImageView) findViewById(R.id.power_img);
		wrssi = (TextView)findViewById(R.id.item_Key);
		wn = (TextView)findViewById(R.id.item_WakeNum);
		((TextView) findViewById(R.id.top_tv)).setText("卡信息");
        Button btnTop = ((Button) findViewById(R.id.top_btn));
        btnTop.setText("返回");
        btnTop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				BluetoothCommService.RemoveCardReceiver(CCardInfoActivity.this);
				CCardInfoActivity.this.finish();
			}
		});
		
       
		BluetoothCommService.AddCardReceiver(this);
		updateListView(sCpid,iRssi,intent.getStringExtra("time"),intent.getIntExtra("pwd",3),0,0);
		setPower(intent.getIntExtra("pwd",3));
	
	}
	
	private void setPower(int i){
		switch(i){
		case 0:
			imgPower.setImageResource(R.drawable.power_00);
			break;
		case 1:
			imgPower.setImageResource(R.drawable.power_50);
			break;
		case 2:
			imgPower.setImageResource(R.drawable.power_100);
			break;
		case 3:
			imgPower.setImageResource(R.drawable.power_100);
			break;
		}
	}
	
	int count = 0;
	private void updateListView(String rfid,int rssi,String time,int pw,int wn,int wrssi){
		count++;
		cpid.setText(rfid);
		this.rssi.setText(String.valueOf(rssi));
		readTime.setText(time);
		cnt.setText(String.valueOf(count));
		this.wn.setText(String.valueOf(wn));
		this.wrssi.setText(String.valueOf(wrssi));
		
		
		if(XYZ == -1){
			if(pw <= 1)
				pwd.setTextColor(0xffff0000);
			else
				pwd.setTextColor(0xffffffff);
			pwd.setText(String.valueOf(pw));
		}else{
			pow.setText("在用轴:");
			Log.i("------------->>>", XYZ+"-----"+XY);
			switch(XYZ){
			case 6:
				showXYZ("XY");
				break;
			case 5:
				showXYZ("XZ");
				break;
			case 3:
				showXYZ("YZ");
				break;
			case 7:
				showXYZ("XYZ");
				break;
			case 4:
				pwd.setText("X");
				break;
			case 2:
				pwd.setText("Y");
				break;
			case 1:
				pwd.setText("Z");
				break;
			case 0:
				pwd.setText("NO");
				break;
			}
			
		}
		setTarget(rssi);
		setPower(pw);
	}
	private void showXYZ(String ss){
		Log.i("------------->>>", ss+"-----"+XY);
		int s = XY;
		if(s != 0){
			if(ss.length() < 3)
			{
				if(s==2 && ss.length() < 2)
					s = 1;
				if(s==3){
					if(ss.length() < 2)
						s = 1;
					else if(ss.length() < 3)
						s = 2;
				}
			}
			SpannableStringBuilder str=new SpannableStringBuilder(ss);
			try{
				str.setSpan(new ForegroundColorSpan(Color.RED), s-1, s, Spanned.SPAN_INCLUSIVE_INCLUSIVE); 
			}catch(Exception e){
				pwd.setText(ss);
				return;
			}
			
			pwd.setText(str);
			return;
		}
		pwd.setText(ss);
	}
	@SuppressLint("SimpleDateFormat")
	@Override
	public void ReadCard(CCardInfo cardInfo) {
		// TODO Auto-generated method stub
		if(!cardInfo.getRfid().equals(sCpid)) return;
		Message msg = mhandler.obtainMessage(1);
		 Bundle bundle = new Bundle();
	     bundle.putString("rfid", cardInfo.getRfid());  
	     bundle.putInt("rssi", cardInfo.getRssi(true)); 
	     bundle.putString("time", cardInfo.GetReadTime());
	     bundle.putInt("pwd", cardInfo.getBattery());
	     bundle.putInt("wn", cardInfo.getWN());
	     bundle.putInt("wrssi", cardInfo.getWRssi());
	     bundle.putInt("XYZ", cardInfo.getXYZ());
	     Log.i("----------", "--"+cardInfo.getXYZ());
	     if(XYZ != -1)
	    	 XY = cardInfo.getBattery1();
	     msg.setData(bundle);
	     msg.sendToTarget();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			BluetoothCommService.RemoveCardReceiver(this);
            return super.onKeyDown(keyCode, event);
        }
        return false;
	}

	float t = 0;
	int imgLeft = 0;
	int ii = 0;
	private SpringProgressView progressView;
	
	@SuppressLint("NewApi")
	private void setTarget(int rssi){
		
		if(imgLeft==0){
			imgLeft = progressView.getLeft();
			
			if(imgLeft == 0)
				return;
			
			t = imgCur.getLeft()-imgCur.getWidth()- imgLeft;
			t = t / 130;
		}
		int a = (int)(t * rssi);
		imgCur.setLeft(imgLeft+a);
		
		progressView.setCurrentCount(rssi);

	}
}
