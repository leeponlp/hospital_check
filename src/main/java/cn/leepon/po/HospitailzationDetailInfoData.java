package cn.leepon.po;

import java.math.BigDecimal;

import lombok.Data;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月21日 上午10:25:26   
 */

@Data
public class HospitailzationDetailInfoData {
	
	private Integer hospitalId;
	
	private String receiptNo;
	
	private String hospitalizationTimes;
	
	private String hospitalizationNo;
	
	private String itemNo;
	
	private String itemClass;
	
	private String insurClass;
	
	private String myselfScale;
	
	private String itemCode;
	
	private String itemName;
	
	private String insurItemNo;
	
	private String insurItemName;
	
	private String itemSpec;
	
	private String units;
	
	private BigDecimal amount;
	
	private BigDecimal itemPrice;
	
	private BigDecimal costs;
	
	private String doctorName;
	
	private String performDept;

}
