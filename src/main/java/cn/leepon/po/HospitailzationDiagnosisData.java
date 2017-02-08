package cn.leepon.po;

import lombok.Data;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月21日 上午10:30:04   
 */

@Data
public class HospitailzationDiagnosisData {
	
	private Integer  hospitalId;
	
	private String hospitalizationTimes;
	
	private String hospitalizationNo;
	
	private String diagnosiType;
	
	private String diagnosisNo;
	
	private String diagnosisCode;
	
	private String diagnosisName;
	
	private String insurDiagnosisCode;
	
	private String insurDiagnosisName;
	
	private String insurLevel;
	
	private String treatResult;

}
