package cn.leepon.po;

import lombok.Data;

/**
 * This class is used for ...
 * 
 * @author leepon1990
 * @version 1.0, 2016年12月20日 上午10:43:09
 */

@Data
public class ClinicData {

	private Integer hospitalId;

	private String visitDate;

	private String receiptNo;

	private String yardName;

	private String medicineTakeFlag;
	
	private String orderAmount;
	
	private String msType;
	
	private String invoiceNo;
	
	private String invoiceType;
	
	private String insurType;
	
	private String insurPay;
	
	private String accountPay;
	
	private String selfPay;
	
	private String insurSelfPay;
	
	private String classSelfPay;
	
	private String otherPay;
	
	private String insurRcptNo;
	
	private String payDate;
	
	private String visitNo;
	
	private String payChannel;

}
