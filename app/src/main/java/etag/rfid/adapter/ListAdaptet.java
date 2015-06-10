package etag.rfid.adapter;

import java.util.List;

import com.goatkeeper.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import etag.rfid.bt.CCardInfo;

/**
 *
 *@ Simple To Introduction
 *@ 项目名称:  [蓝牙读写器]
 *@ 包:        [etag.rfid.adapter]
 *@ 类名称:    [ListAdaptet]
 *@ 类描述:    [卡信息显示，适配器]
 *@ 创建人:    [廖]
 *@ 创建时间:  [2015.4.2]
 *@ 修改人:    []
 *@ 修改时间:  []
 *@ 修改备注:  []
 *@ 版本:      [v1.0]
 *@
 */
public class ListAdaptet extends BaseAdapter{
	private List<CCardInfo> appinfos;
	private Context context;
	private static TextView id,cpid,rssi,readTime,cnt,pwd,wn,key;
	public ListAdaptet(List<CCardInfo> appinfos, Context context) {
		this.appinfos = appinfos;
		this.context = context;
	}

	public int getCount() {
		return appinfos.size();
	}

	public Object getItem(int position) {
		return appinfos.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * View convertView (转化view对象 ,历史view对象的缓存) convertview 就是拖动的时候被回收掉的view对象
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		if (convertView == null) {
			//Log.i(TAG,"通过资源文件 创建view对象");
			view = View.inflate(context, R.layout.listview_item, null);
		}else{
			//Log.i(TAG,"使用历史缓存view对象");
			view = convertView;
		}
		
		CCardInfo info;
		try{
			info = appinfos.get(position);
		}catch(Exception e){
			return view;
		}

		//id,cpid,rssi,readTime,cnt
		id = (TextView) view.findViewById(R.id.item_id);
		id.setText(String.valueOf(""+info.getListCount()));
		cpid = (TextView) view.findViewById(R.id.item_cpid);
		cpid.setText(info.getRfid());
		rssi = (TextView) view.findViewById(R.id.item_rssi);
		rssi.setText(String.valueOf(info.getRssi(true)));
		readTime = (TextView) view.findViewById(R.id.item_time);
		readTime.setText(info.GetReadTime());
		cnt = (TextView) view.findViewById(R.id.item_cnt);
		cnt.setText(String.valueOf(info.getCnt()));
		wn = (TextView) view.findViewById(R.id.item_WakeNum);
		wn.setText(String.valueOf(info.getWN()));
		key = (TextView) view.findViewById(R.id.item_Key);
		key.setText(String.valueOf(info.getWRssi()));
		pwd = (TextView) view.findViewById(R.id.item_pwd);
		int t = info.getBattery();
		if(t <= 1)
			pwd.setTextColor(0xffff0000);
		else
			pwd.setTextColor(0xffffffff);
		pwd.setText(String.valueOf(t));
		
		
		return view;
	}
}
