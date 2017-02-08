package cn.leepon.po;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月20日 下午2:18:19   
 */
@Data
public class HospitalizationDailyData {
	
	private Integer hospitalId;
	
	private String hospitalizationNo;
	
	private String admissionTimes;
	
	private String admissionTime;
	
	private String dischargeTime;
	
	private String deptName;
	
	private String treatDoctor;
	
	private String settlementTime;
	
	private String prepayments;
	
	private String bedNo;
	
	private List<FeeItemInfo> feeItemInfo = new ArrayList<>();
	
	private List<DiagnosisInfo> diagnosisInfo = new ArrayList<>();
	
	@Data
	class FeeItemInfo {
		
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
		
		private Integer amount;
		
		private String units;
		
	}
	
	@Data
	class DiagnosisInfo{
		
		private Integer hospitalId;
		
		private String hospitalizationTimes;
		
		private String hospitalizationNo;
		
		private String diagnosiType;
		
		private String diagnosisNo;
		
		private String diagnosisCode;
		
		private String diagnosisName;
		
		private String insurDiagnosisCode;
		
		private String insurDiagnosisName;
		
		private String diagnosisLevel;
		
		private String treatResult;
		
		
	}

}
