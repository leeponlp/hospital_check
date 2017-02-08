package cn.leepon.po;

import lombok.Data;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月23日 下午3:42:49   
 */
@Data
public class HospitailzationDailyFeeItemInfoData {
	
	private Integer hospitalId;
	
	private String admissionTimes;
	
	private String hospitalizationNo;
	
	private String itemNo;
	
	private String itemName;
	
	private String itemClass;
	
	private String itemPrice;
	
	private String totalExpenses;
	
	private String shouldPaymentAmount;
	
	private String actualPaymentAmount;
	
	private String prepayments;
	
	private String billingTime;
	
	private String itemSpec;
	
	private String amount;
	
	private String units;

}
