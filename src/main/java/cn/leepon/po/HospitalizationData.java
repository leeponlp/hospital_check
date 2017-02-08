package cn.leepon.po;

import java.math.BigDecimal;
import cn.leepon.util.FastJsonUtil;
import lombok.Data;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月20日 下午1:48:58   
 */
@Data
public class HospitalizationData {
	
	private Integer hospitalId;
	
	private String receiptNo;
	
	private Integer hospitalizationTimes;
	
	private String hospitalizationNo;
	
	private String costCategory;
	
	private String insurRcptNo;
	
	private String invoiceNo;
	
	private String invoiceType;
	
	private BigDecimal totaPay;
	
	private BigDecimal insurPay;
	
	private BigDecimal accountPay;
	
	private BigDecimal selfPay;
	
	private BigDecimal insurSelfPay;
	
	private BigDecimal classSelfPay;
	
	private BigDecimal otherPay;
	
	private String admissionTime;
	
	private String dischargeTime;
	
	private String deptName;
	
	private String settleTime;
	
	private String doctorName;
	
	//private JSONArray detailInfoList = new JSONArray(new ArrayList<>());
	
	//private JSONArray diagnosisData = new JSONArray(new ArrayList<>());
	
	//private List<DetailInfoData> detailInfoList = new ArrayList<>();
	
	//private List<DiagnosisData> diagnosisData = new ArrayList<>();
	
//	@Data
//	class DetailInfoData{
//		
//		private Integer hospitalId;
//		
//		private String receiptNo;
//		
//		private String hospitalizationTimes;
//		
//		private String hospitalizationNo;
//		
//		private String itemNo;
//		
//		private String itemClass;
//		
//		private String insurClass;
//		
//		private String myselfScale;
//		
//		private String itemCode;
//		
//		private String insurItemNo;
//		
//		private String insurItemName;
//		
//		private String itemSpec;
//		
//		private String units;
//		
//		private BigDecimal amount;
//		
//		private BigDecimal itemPrice;
//		
//		private BigDecimal costs;
//		
//		private String doctorName;
//		
//		private String performDept;
//		
//	}
	
//	@Data
//	class DiagnosisData{
//		
//		private Integer  hospitalId;
//		
//		private String hospitalizationTimes;
//		
//		private String hospitalizationNo;
//		
//		private String diagnosiType;
//		
//		private String diagnosisNo;
//		
//		private String diagnosisCode;
//		
//		private String diagnosisName;
//		
//		private String insurDiagnosisCode;
//		
//		private String insurDiagnosisName;
//		
//		private String insurLevel;
//		
//		private String treatResult;
//		
//	}
	
	public static void main(String[] args) {
		
		//String str = "{'hospitalId':290138,'receiptNo':'011051644501','hospitalizationTimes':100,'hospitalizationNo':'12000034','costCategory':'全自费','insurRcptNo':'1','invoiceNo':'011051644501','invoiceType':'住院发票','totaPay':'112.00','insurPay':'100.00','selfPay':'1.00','insurSelfPay':'1.00','classSelfPay':'0.00','otherPay':'0.00','admissionTime':'2016-06-22 10:56:11','dischargeTime':'2016-06-30 10:56:16','deptName':'泌尿心胸肛肠外科 ','settleTime':'2016-02-20 16:59:19','doctorName':'吴文弼 ','detailInfoList':null,'diagnosisData':null}";
		String str1 ="{'hospitalId':290138,'receiptNo':'011051644501','hospitalizationTimes':100,'hospitalizationNo':'12000034','costCategory':'全自费','insurRcptNo':'1','invoiceNo':'011051644501','invoiceType':'住院发票','totaPay':'112.00','insurPay':'100.00','selfPay':'1.00','insurSelfPay':'1.00','classSelfPay':'0.00','otherPay':'0.00','admissionTime':'2016-06-22 10:56:11','dischargeTime':'2016-06-30 10:56:16','deptName':'泌尿心胸肛肠外科 ','settleTime':'2016-02-20 16:59:19','doctorName':'吴文弼 ','detailInfoList':[{'hospitalId':290138,'receiptNo':'011051644501','hospitalizationTimes':'1','hospitalizationNo':'0','itemNo':'12000041','itemClass':'全血细胞计数+五分类','insurClass':'1','myselfScale':'1','itemCode':'12000041','itemName':'全血细胞计数+五分类','medicalInsuranceDiagnosisCode':null,'medicalInsuranceDiagnosisName':null,'itemSpec':'门诊','units':'包', 'amount':'112.00','itemPrice':'100.00','costs':'111.00','doctorName':'王主任','performDept':'妇科门诊'},{'hospitalId':290138,'receiptNo':'011051644501','hospitalizationTimes':'1','hospitalizationNo':'0','itemNo':'12000042','itemClass':'HEHE','insurClass':'1','myselfScale':'1','itemCode':'1234','itemName':'KANG','medicalInsuranceDiagnosisCode':null,'medicalInsuranceDiagnosisName':null,'itemSpec':'住院','units':'个','amount':'113.00','itemPrice':'101.00','costs':'1113.00', 'doctorName':'小明','performDept':'曾'}],'diagnosisData':['hospitalId':290138,'patientId':null,'hospitalizationTimes':'2','hospitalizationNo':'2','diagnosisType':'1','diagnosisNo':'123','diagnosisCode':'诊断编码','diagnosisName':'诊断名称','insurDiagnosisCode':'医保诊断编码','insurDiagnosisName':'医保诊断编码','insurLevel':'初步诊断','treatResult':'诊断结果']}";
		HospitalizationData bean = FastJsonUtil.toBean(str1, HospitalizationData.class);
		System.err.println(bean);
		}
	

}
