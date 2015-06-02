package etag.rfid.ui;

import etag.rfid.bt.CCardInfo;

/*
 * 读卡接口
 * 接收读卡的对象继承此接口
 */
public interface IReadCard {
	/*
	 * 接受读卡数据，返回读卡结果
	 */
	public void ReadCard(CCardInfo cardInfo);
}

