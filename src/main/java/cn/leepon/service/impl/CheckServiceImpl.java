package cn.leepon.service.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.leepon.po.CheckParam;
import cn.leepon.po.ClinicDetailInfoData;
import cn.leepon.po.ClinicDiagnosisInfoData;
import cn.leepon.po.HospitailzationDailyDiagnosisInfoData;
import cn.leepon.po.HospitailzationDailyFeeItemInfoData;
import cn.leepon.po.HospitailzationDetailInfoData;
import cn.leepon.po.HospitailzationDiagnosisData;
import cn.leepon.po.HospitalPlusToken;
import cn.leepon.service.CheckService;
import cn.leepon.util.DateUtil;
import cn.leepon.util.FastJsonUtil;
import cn.leepon.util.FilenameEncodeUtil;
import cn.leepon.util.HttpClientUtil;
import cn.leepon.util.StyleUtil;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月19日 下午4:48:48   
 */
@Service
public class CheckServiceImpl implements CheckService {
	
	private static Logger logger = Logger.getLogger(CheckServiceImpl.class);
	
	private static String token_url = "";
	
	private static String clinic_url = "";
	
	private static String hospitalization_url = "";
	
	private static String hospitalizationdaily_url = "";
	
	private static String token = "";
	
	private static String requestTime = "";
	
	private static int validTime = 28000;
	
	private static final String FORMAT_LONG = "yyyy/MM/dd HH:mm:ss";
	
	static{
		token_url= "https://open.quyiyuan.com:8443/v1.0/access/token/get?userName=BaoXianYunWeiTest&password=CF83FBFDBE2D2E10";
		clinic_url = "https://open.quyiyuan.com:8443/v1.0/insurance/clinic/settlement";
		hospitalization_url = "https://open.quyiyuan.com:8443/v1.0/insurance/hospitalization/settlement";
		hospitalizationdaily_url = "https://open.quyiyuan.com:8443/v1.0/insurance/hospitalization/daily/expenses";
	}

	@Override
	public void getCheckedReport(CheckParam checkParam,HttpServletRequest request, HttpServletResponse response){
		
		// 获取token信息
		if (StringUtils.isEmpty(token)) {
			HospitalPlusToken hospitalPlusToken = getHospitalPlusToken();
			if (hospitalPlusToken.isSuccess() == true) {
				token = hospitalPlusToken.getData().getToken();
				requestTime = hospitalPlusToken.getTime(); 
			} else {
				logger.info("get token happened exception :"+hospitalPlusToken.getMessage());
			}
		}else{
			// 校验token是否过期
			// 过期重新获取新的token
			long requestTimeStamp = DateUtil.getTimestamp(requestTime,FORMAT_LONG)/1000;
			long nowTimeStamp = System.currentTimeMillis()/1000;
			if (nowTimeStamp > (requestTimeStamp+validTime)) {
				HospitalPlusToken hospitalPlusToken = getHospitalPlusToken();
				if (hospitalPlusToken.isSuccess() == true) {
					token = hospitalPlusToken.getData().getToken();
					requestTime = hospitalPlusToken.getTime();
				} else {
					logger.info("get token happened exception :"+hospitalPlusToken.getMessage());
				}
			}
			
		}
		
		//线程池简单应用 利用Future异步获取多线程的返回结果
		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(5));
		Future<JSONArray> futureClinicRows = executor.submit(new Callable<JSONArray>() {
			@Override
			public JSONArray call() throws Exception {
					
				return getRowsJson(token, clinic_url,checkParam);
			}
		});
		
		
		Future<JSONArray> futureHospitalizationRows = executor.submit(new Callable<JSONArray>() {
			@Override
			public JSONArray call() throws Exception {
				return getRowsJson(token, hospitalization_url,checkParam);
			}
		});
		
		
		Future<JSONArray> futureHospitalizationDailyRows = executor.submit(new Callable<JSONArray>() {
			@Override
			public JSONArray call() throws Exception {
				return getRowsJson(token, hospitalizationdaily_url,checkParam);
			}
		});
		
		//梳理返回结果重新封装
		Map<Integer, Object> clinicData = new HashMap<>();
        Map<Integer, Object> hospitalizationData = new HashMap<>();
        Map<Integer, Object> hospitalizationDailyData = new HashMap<>();
		
		try {
			if (null != futureClinicRows) { 
				JSONArray clinicRows = futureClinicRows.get();
				if (CollectionUtils.isNotEmpty(clinicRows)) {
					setMap(clinicData, clinicRows);
				}
			}
			
			if (null != futureHospitalizationRows) {
				JSONArray hospitalizationRows = futureHospitalizationRows.get();
			    if (CollectionUtils.isNotEmpty(hospitalizationRows)) {
			    	setMap(hospitalizationData, hospitalizationRows);
				}

			}
			
			if (null != futureHospitalizationDailyRows) {
				JSONArray hospitalizationDailyRows = futureHospitalizationDailyRows.get();
			    if (CollectionUtils.isNotEmpty(hospitalizationDailyRows)) {
			    	setMap(hospitalizationDailyData, hospitalizationDailyRows);
				}

			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		// 文件下载
		response.setCharacterEncoding("utf-8");
		// 文件名
		String fileName = checkParam.getPatientName()+"的医院验收信息.xlsx";
		response.setContentType("application/msexcel");
		response.setBufferSize(10240);

		// 客户端
		String agent = request.getHeader("User-Agent");
		try {
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
			String encodefilename = FilenameEncodeUtil.encodeFilename(fileName, agent);
			response.setHeader("Content-Disposition", "attachment;filename=" + encodefilename);
			SXSSFWorkbook workbook = getWorkbook(clinicData, hospitalizationData, hospitalizationDailyData);
			workbook.write(out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	
	/**
	 * 
	 * @Title: getHospitalPlusToken 
	 * @Description: TODO <获取装载token信息对应的bean>
	 * @return HospitalPlusToken  
	 * @throws
	 */
	private HospitalPlusToken getHospitalPlusToken() {
		return FastJsonUtil.toBean(HttpClientUtil.executeByGET(token_url),HospitalPlusToken.class);
	}
	
	

	/**
	 * 
	 * @Title: getRowsJson 
	 * @Description: TODO <处理医院+接口数据提取rows节点的数据>
	 * @param token
	 * @param url
	 * @param checkParam
	 * @return JSONArray  
	 * @throws
	 */
	private JSONArray getRowsJson(String token, String url,CheckParam checkParam) {
		JSONArray rowsJson = null;
		JSONObject dataJson = getDataJson(checkParam,token,url);
		if (null != dataJson) {
			String linkMode = dataJson.getString("linkMode");
			if ("0".equals(linkMode)) {
				try {
					Thread.sleep(60000);
					dataJson = getDataJson(checkParam,token,url);
					if (null != dataJson) {
						rowsJson = dataJson.getJSONArray("rows");
					}
				} catch (InterruptedException e) {
					logger.info("Thread was Interrupted and happened Exception:"+e.getMessage());
					e.printStackTrace();
				}
				
			}else{
				rowsJson = dataJson.getJSONArray("rows");
			}
		}
		
		return rowsJson;
	}
	
	
	/**
	 * 
	 * @Title: setMap 
	 * @Description: TODO <遍历返回值JSONArray梳理数据结构>
	 * @param map
	 * @param jsonArray
	 * @return Map<Integer,Object>  
	 * @throws
	 */
	 private static Map<Integer, Object> setMap(Map<Integer,Object> map, JSONArray jsonArray) {
	        int begin = map.size();
            if (null != jsonArray && jsonArray.size() > 0) {
            	int size = jsonArray.size();
                for (int i = 0; i < size; i++) {
                    map.put(i + begin, jsonArray.get(i));
                }
            }
	        return map;
	    }
	
	
	/**
	 * 
	 * @Title: getDataJson 
	 * @Description: TODO <处理医院+接口数据提取data节点的数据>
	 * @param checkParam
	 * @param token
	 * @param url
	 * @return JSONObject  
	 * @throws
	 */
	private JSONObject getDataJson(CheckParam checkParam ,String token,String url){
		List<NameValuePair> post = getParam(checkParam, token);
		String str = HttpClientUtil.executeByPOST(url, post); 
		if (!StringUtils.isEmpty(str)) {
			JSONObject strJson = FastJsonUtil.toJSONObject(str);
			if (null != strJson) {
				String data = strJson.getString("data");
				if (!StringUtils.isEmpty(data)) {
					JSONObject dataJson = FastJsonUtil.toJSONObject(data);
					return dataJson;
				}
			}
			
		}
		return null;
	}


	/**
	 * 
	 * @Title: getParam 
	 * @Description: TODO <封装接口参数>
	 * @param checkParam
	 * @param token
	 * @return List<NameValuePair>  
	 * @throws
	 */
	private List<NameValuePair> getParam(CheckParam checkParam, String token) {
		List<NameValuePair> post = new ArrayList<>();
		post.add(new BasicNameValuePair("token",token));
		//医院号
		post.add(new BasicNameValuePair("hospitalId",checkParam.getHospitalId())); 
		//病人姓名
		post.add(new BasicNameValuePair("patientName",checkParam.getPatientName()));
		//身份证
		post.add(new BasicNameValuePair("idCardNo",checkParam.getIdCardNo()));
		//住院号
		post.add(new BasicNameValuePair("hospitalizationNo",checkParam.getHospitalizationNo()));
		//是否是成人
		post.add(new BasicNameValuePair("childFlag","0"));
		//页数
		post.add(new BasicNameValuePair("page","1"));
		//每页数量
		post.add(new BasicNameValuePair("amounts","50"));
		return post;
	}
	
	
	/**
	 * 
	 * @Title: getWorkbook 
	 * @Description: TODO <构建输出excel所需SXSSFWorkbook对象>
	 * @param clinicData
	 * @param hospitalizationData
	 * @param hospitalizationDailyData
	 * @throws IOException 
	 * @return SXSSFWorkbook  
	 */
	private SXSSFWorkbook getWorkbook(Map<?, ?> clinicData, Map<?, ?> hospitalizationData, Map<?, ?> hospitalizationDailyData) throws IOException {
		
        // 创建workbook
        SXSSFWorkbook wb = new SXSSFWorkbook(1000);

     	// 设置样式
     	CellStyle headstyle = StyleUtil.getStyle(wb);
     	Sheet sheet1 = wb.createSheet("门诊已结算");
     	Sheet sheet2 = wb.createSheet("住院已结算");
     	Sheet sheet3 = wb.createSheet("住院日清单");

        if (null != clinicData && clinicData.size()>0) {
            
            int staRowNum = 0;
            int endRowNum = 0;
            
            for(int i=0,j=clinicData.size();i<j;i++){
            	staRowNum = endRowNum;
            	JSONObject jsonObject = (JSONObject) clinicData.get(i);
            	
            	Integer hospitalId = jsonObject.getInteger("hospitalId");
            	String visitDate = jsonObject.getString("visitDate");
            	String receiptNo = jsonObject.getString("receiptNo");
            	String yardName = jsonObject.getString("yardName");
            	String medicineTakeFlag = jsonObject.getString("medicineTakeFlag");
            	String orderAmount = jsonObject.getString("orderAmount");
            	String msType = jsonObject.getString("msType");
            	String invoiceNo = jsonObject.getString("invoiceNo");
            	String invoiceType = jsonObject.getString("invoiceType");
            	String insurType = jsonObject.getString("insurType");
            	String insurPay = jsonObject.getString("insurPay");
            	String accountPay = jsonObject.getString("accountPay");
            	String selfPay = jsonObject.getString("selfPay");
            	String insurSelfPay = jsonObject.getString("insurSelfPay");
            	String classSelfPay = jsonObject.getString("classSelfPay");
            	String otherPay = jsonObject.getString("otherPay");
            	String insurRcptNo = jsonObject.getString("insurRcptNo");
            	String payDate = jsonObject.getString("payDate");
            	String visitNo = jsonObject.getString("visitNo");
            	String payChannel = jsonObject.getString("payChannel");
            	
            	String[] head1 = new String[]{"医院号","就诊日期","票据号","院区名称","取药状态","订单金额","医保支付标识","发票号",
            			"发票类型","医保类别","统筹金额","账户金额","自费金额","自付金额","分类自付金额",
            			"其他金额","医保交易流水号","支付日期","就诊序号","支付渠道"};
            	
            	Object[] data1 = new Object[]{hospitalId,visitDate,receiptNo,yardName,medicineTakeFlag,orderAmount,msType,invoiceNo,
            			invoiceType,insurType,insurPay,accountPay,selfPay,insurSelfPay,classSelfPay,
            			otherPay,insurRcptNo,payDate,visitNo,payChannel};
            	
            	for(int d=0;d<head1.length;d++){
            		Row headRow1 = sheet1.createRow(staRowNum+d);
            		sheet1.setColumnWidth(d, 5000);
            		Cell cell = headRow1.createCell(0);
            		cell.setCellValue(head1[d]);
            		cell.setCellStyle(headstyle); 
            		headRow1.createCell(1).setCellValue(null==data1[d]?"//":data1[d]+"");
            	}
            	
            	
            	JSONArray detailInfoArray = jsonObject.getJSONArray("detailInfo");
            	List<ClinicDetailInfoData> detailInfoList = null;
            	if (null != detailInfoArray) {
            		detailInfoList = JSONArray.parseArray(detailInfoArray.toString(), ClinicDetailInfoData.class);
				}
            	
            	if (CollectionUtils.isNotEmpty(detailInfoList)) {
					
            		String[] head2 = new String[]{"项目序号","项目名称","医保项目编码","医保项目名称","项目数量","项目单位",
            				"项目单价","项目总金额","项目规格","项目序号","开单科室","开单医生","医保类别","自付比例","项目类别"};
            		Row dataRow2 = sheet1.createRow(staRowNum+head1.length);
            		for(int d=0;d<head2.length;d++){
            			sheet1.setColumnWidth(d, 5000);
            			Cell cell = dataRow2.createCell(d);
            			cell.setCellValue(head2[d]);
            			cell.setCellStyle(headstyle);
            		}
            		
            		for (ClinicDetailInfoData clinicDetailInfoData : detailInfoList) {
            			Row dataRow = sheet1.createRow(sheet1.getLastRowNum()+1);
            			dataRow.createCell(0).setCellValue(null==clinicDetailInfoData.getItemNo()?"//":clinicDetailInfoData.getItemNo());
            			dataRow.createCell(1).setCellValue(null==clinicDetailInfoData.getItemName()?"//":clinicDetailInfoData.getItemName());
            			dataRow.createCell(2).setCellValue(null==clinicDetailInfoData.getInsurItemNo()?"//":clinicDetailInfoData.getInsurItemNo());
            			dataRow.createCell(3).setCellValue(null==clinicDetailInfoData.getInsurItemName()?"//":clinicDetailInfoData.getInsurItemName());
            			dataRow.createCell(4).setCellValue(null==clinicDetailInfoData.getItemAmount()?"//":clinicDetailInfoData.getItemAmount());
            			dataRow.createCell(5).setCellValue(null==clinicDetailInfoData.getItemUnit()?"//":clinicDetailInfoData.getItemUnit());
            			dataRow.createCell(6).setCellValue(null==clinicDetailInfoData.getItemPrice()?"//":clinicDetailInfoData.getItemPrice());
            			dataRow.createCell(7).setCellValue(null==clinicDetailInfoData.getItemCost()?"//":clinicDetailInfoData.getItemCost());
            			dataRow.createCell(8).setCellValue(null==clinicDetailInfoData.getItemSpec()?"//":clinicDetailInfoData.getItemSpec());
            			dataRow.createCell(9).setCellValue(null==clinicDetailInfoData.getItemCode()?"//":clinicDetailInfoData.getItemCode());
            			dataRow.createCell(10).setCellValue(null==clinicDetailInfoData.getExecuteDept()?"//":clinicDetailInfoData.getExecuteDept());
            			dataRow.createCell(11).setCellValue(null==clinicDetailInfoData.getDoctorName()?"//":clinicDetailInfoData.getDoctorName());
            			dataRow.createCell(12).setCellValue(null==clinicDetailInfoData.getInsurClass()?"//":clinicDetailInfoData.getInsurClass());
            			dataRow.createCell(13).setCellValue(null==clinicDetailInfoData.getSelfScale()?"//":clinicDetailInfoData.getSelfScale());
            			dataRow.createCell(14).setCellValue(null==clinicDetailInfoData.getItemClass()?"//":clinicDetailInfoData.getItemClass());
            		}
            		
				}
            	
            	
            	JSONArray diagnosisInfoArray = jsonObject.getJSONArray("diagnosisInfo");
            	List<ClinicDiagnosisInfoData> diagnosisInfoList = null;
            	if (null != diagnosisInfoArray) {
            		diagnosisInfoList = JSONArray.parseArray(diagnosisInfoArray.toString(), ClinicDiagnosisInfoData.class);
				}
            	
            	if (CollectionUtils.isNotEmpty(diagnosisInfoList)) {
            		
            		String[] head3 = new String[]{"诊断类型","诊断序号","诊断代码","诊断名称","医保诊断编码","医保诊断名称","诊断级别","治疗结果"};
            		Row headRow3 = sheet1.createRow(staRowNum+detailInfoList.size()+1+head1.length);
            		for(int d=0;d<head3.length;d++){
            			sheet1.setColumnWidth(d, 5000);
            			Cell cell = headRow3.createCell(d);
            			cell.setCellValue(head3[d]);
            			cell.setCellStyle(headstyle);
            		}
					
            		for (ClinicDiagnosisInfoData clinicDiagnosisInfoData : diagnosisInfoList) {
            			Row dataRow = sheet1.createRow(sheet1.getLastRowNum()+1);
            			dataRow.createCell(0).setCellValue(null==clinicDiagnosisInfoData.getDiagnosiType()?"//":clinicDiagnosisInfoData.getDiagnosiType());
            			dataRow.createCell(1).setCellValue(null==clinicDiagnosisInfoData.getDiagnosisNo()?"//":clinicDiagnosisInfoData.getDiagnosisNo());
            			dataRow.createCell(2).setCellValue(null==clinicDiagnosisInfoData.getDiagnosisCode()?"//":clinicDiagnosisInfoData.getDiagnosisCode());
            			dataRow.createCell(3).setCellValue(null==clinicDiagnosisInfoData.getDiagnosisName()?"//":clinicDiagnosisInfoData.getDiagnosisName());
            			dataRow.createCell(4).setCellValue(null==clinicDiagnosisInfoData.getInsurDiagnosisCode()?"//":clinicDiagnosisInfoData.getInsurDiagnosisCode());
            			dataRow.createCell(5).setCellValue(null==clinicDiagnosisInfoData.getInsurDiagnosisName()?"//":clinicDiagnosisInfoData.getInsurDiagnosisName());
            			dataRow.createCell(6).setCellValue(null==clinicDiagnosisInfoData.getInsurLevel()?"//":clinicDiagnosisInfoData.getInsurLevel());
            			dataRow.createCell(7).setCellValue(null==clinicDiagnosisInfoData.getTreatResult()?"//":clinicDiagnosisInfoData.getTreatResult());
            			
            		}
            		
				}
            	
            	endRowNum = staRowNum+detailInfoList.size()+diagnosisInfoList.size()+2+head1.length;
            	
            }
            
            

        }

        if (null != hospitalizationData && hospitalizationData.size()>0 ) {
            
            int staRowNum = 0;
            int endRowNum = 0;
            
            for(int i=0;i<hospitalizationData.size();i++){
            	
            	staRowNum = endRowNum;
            	JSONObject jsonObject = (JSONObject) hospitalizationData.get(i);
            	
            	Integer hospitalId = jsonObject.getInteger("hospitalId");
            	String receiptNo = jsonObject.getString("receiptNo");
            	Integer hospitalizationTimes = jsonObject.getInteger("hospitalizationTimes");
            	String hospitalizationNo = jsonObject.getString("hospitalizationNo");
            	String costCategory = jsonObject.getString("costCategory");
            	String insurRcptNo = jsonObject.getString("insurRcptNo");
            	String invoiceNo = jsonObject.getString("invoiceNo");
            	String invoiceType = jsonObject.getString("invoiceType");
            	BigDecimal totaPay = jsonObject.getBigDecimal("totaPay");
            	BigDecimal insurPay = jsonObject.getBigDecimal("insurPay");
            	BigDecimal accountPay = jsonObject.getBigDecimal("accountPay");
            	BigDecimal selfPay = jsonObject.getBigDecimal("selfPay");
            	BigDecimal insurSelfPay = jsonObject.getBigDecimal("insurSelfPay");
            	BigDecimal classSelfPay = jsonObject.getBigDecimal("classSelfPay");
            	BigDecimal otherPay = jsonObject.getBigDecimal("otherPay");
            	String admissionTime = jsonObject.getString("admissionTime");
            	String dischargeTime = jsonObject.getString("dischargeTime");
            	String settleTime = jsonObject.getString("settleTime");
            	String deptName = jsonObject.getString("deptName");
            	String doctorName = jsonObject.getString("doctorName");
            	
            	String[] head1 = new String[]{"医院编号","票据号","住院次数","住院号","费用类别","医保交易流水号","费用类别","发票类型",
            			"费用总额","统筹金额","账户金额","自费金额","自付金额","分类自付金额","其他金额","入院时间","出院时间","出院科室","结算时间","经治医生"};
            	
            	Object[] data1 = new Object[]{hospitalId,receiptNo,hospitalizationTimes,hospitalizationNo,costCategory,insurRcptNo,invoiceNo,
            			invoiceType,totaPay,insurPay,accountPay,selfPay,insurSelfPay,classSelfPay,otherPay,admissionTime,dischargeTime,deptName,settleTime,doctorName};
            	
            	for(int d=0;d<head1.length;d++){
            		Row headRow1 = sheet2.createRow(staRowNum+d);
            		sheet2.setColumnWidth(d, 5000);
            		Cell cell = headRow1.createCell(0);
            		cell.setCellValue(head1[d]);
            		cell.setCellStyle(headstyle); 
            		headRow1.createCell(1).setCellValue(null==data1[d]?"//":data1[d]+"");
            	}
            	
            	JSONArray detailInfoArray = jsonObject.getJSONArray("detailInfoList");
            	List<HospitailzationDetailInfoData> detailInfoList =null;
            	if (null != detailInfoArray){
            		
            		detailInfoList = JSONArray.parseArray(detailInfoArray.toString(),HospitailzationDetailInfoData.class);
            	}
				
            	
            	if (CollectionUtils.isNotEmpty(detailInfoList)) {
					
            		String[] head2 = new String[]{"费用序号","项目类别","医保类别","自付比例","项目编码","项目名称",
            				"医保项目编码","医保项目名称","规格","单位","数量","单价","金额","开单医生","执行科室"};
            		
            		Row headRow2 = sheet2.createRow(staRowNum+head1.length);
            		for(int d=0;d<head2.length;d++){
            			Cell cell = headRow2.createCell(d);
            			cell.setCellValue(head2[d]);
            			cell.setCellStyle(headstyle);
            		}
            		
            		for (HospitailzationDetailInfoData hospitailzationDetailInfoData : detailInfoList) {
            			Row dataRow = sheet2.createRow(sheet2.getLastRowNum()+1);
            			dataRow.createCell(0).setCellValue(null==hospitailzationDetailInfoData.getItemNo()?"//":hospitailzationDetailInfoData.getItemNo());
            			dataRow.createCell(1).setCellValue(null==hospitailzationDetailInfoData.getItemClass()?"//":hospitailzationDetailInfoData.getItemClass());
            			dataRow.createCell(2).setCellValue(null==hospitailzationDetailInfoData.getInsurClass()?"//":hospitailzationDetailInfoData.getInsurClass());
            			dataRow.createCell(3).setCellValue(null==hospitailzationDetailInfoData.getMyselfScale()?"//":hospitailzationDetailInfoData.getMyselfScale());
            			dataRow.createCell(4).setCellValue(null==hospitailzationDetailInfoData.getItemCode()?"//":hospitailzationDetailInfoData.getItemCode());
            			dataRow.createCell(5).setCellValue(null==hospitailzationDetailInfoData.getItemName()?"//":hospitailzationDetailInfoData.getItemName());
            			dataRow.createCell(6).setCellValue(null==hospitailzationDetailInfoData.getInsurItemNo()?"//":hospitailzationDetailInfoData.getInsurItemNo());
            			dataRow.createCell(7).setCellValue(null==hospitailzationDetailInfoData.getInsurItemName()?"//":hospitailzationDetailInfoData.getInsurItemName());
            			dataRow.createCell(8).setCellValue(null==hospitailzationDetailInfoData.getItemSpec()?"//":hospitailzationDetailInfoData.getItemSpec());
            			dataRow.createCell(9).setCellValue(null==hospitailzationDetailInfoData.getUnits()?"//":hospitailzationDetailInfoData.getUnits());
            			dataRow.createCell(10).setCellValue(null==hospitailzationDetailInfoData.getAmount()?"//":hospitailzationDetailInfoData.getAmount()+"");
            			dataRow.createCell(11).setCellValue(null==hospitailzationDetailInfoData.getItemPrice()?"//":hospitailzationDetailInfoData.getItemPrice()+"");
            			dataRow.createCell(12).setCellValue(null==hospitailzationDetailInfoData.getCosts()?"//":hospitailzationDetailInfoData.getCosts()+"");
            			dataRow.createCell(13).setCellValue(null==hospitailzationDetailInfoData.getDoctorName()?"//":hospitailzationDetailInfoData.getDoctorName());
            			dataRow.createCell(14).setCellValue(null==hospitailzationDetailInfoData.getPerformDept()?"//":hospitailzationDetailInfoData.getPerformDept());
            		}
				}
            	
            	JSONArray diagnosisDataArray = jsonObject.getJSONArray("diagnosisData");
            	List<HospitailzationDiagnosisData> diagnosisList = null;
            	if (null != diagnosisDataArray) {
            		diagnosisList = JSONArray.parseArray(diagnosisDataArray.toString(), HospitailzationDiagnosisData.class);
				}
            	
            	if (CollectionUtils.isNotEmpty(diagnosisList)) {
					
            		String[] head3 = new String[]{"医院号","住院次数","住院号","诊断类型","诊断序号","诊断代码","诊断名称","医保诊断编码","医保诊断名称","诊断级别","治疗结果"};
            		Row headRow3 = sheet2.createRow(staRowNum+detailInfoList.size()+1+head1.length);
            		for(int d=0;d<head3.length;d++){
            			Cell cell = headRow3.createCell(d);
            			cell.setCellValue(head3[d]+"");
            			cell.setCellStyle(headstyle);
            		}
            		
            		for (HospitailzationDiagnosisData hospitailzationDiagnosisData : diagnosisList) {
            			Row dataRow = sheet2.createRow(sheet2.getLastRowNum()+1);
            			dataRow.createCell(0).setCellValue(null==hospitailzationDiagnosisData.getHospitalId()?"//":hospitailzationDiagnosisData.getHospitalId()+"");
            			dataRow.createCell(1).setCellValue(null==hospitailzationDiagnosisData.getHospitalizationTimes()?"//":hospitailzationDiagnosisData.getHospitalizationTimes());
            			dataRow.createCell(2).setCellValue(null==hospitailzationDiagnosisData.getHospitalizationNo()?"//":hospitailzationDiagnosisData.getHospitalizationNo());
            			dataRow.createCell(3).setCellValue(null==hospitailzationDiagnosisData.getDiagnosiType()?"//":hospitailzationDiagnosisData.getDiagnosiType());
            			dataRow.createCell(4).setCellValue(null==hospitailzationDiagnosisData.getDiagnosisNo()?"//":hospitailzationDiagnosisData.getDiagnosisNo());
            			dataRow.createCell(5).setCellValue(null==hospitailzationDiagnosisData.getDiagnosisCode()?"//":hospitailzationDiagnosisData.getDiagnosisCode());
            			dataRow.createCell(6).setCellValue(null==hospitailzationDiagnosisData.getDiagnosisName()?"//":hospitailzationDiagnosisData.getDiagnosisName());
            			dataRow.createCell(7).setCellValue(null==hospitailzationDiagnosisData.getInsurDiagnosisCode()?"//":hospitailzationDiagnosisData.getInsurDiagnosisCode());
            			dataRow.createCell(8).setCellValue(null==hospitailzationDiagnosisData.getInsurDiagnosisName()?"//":hospitailzationDiagnosisData.getInsurDiagnosisName());
            			dataRow.createCell(9).setCellValue(null==hospitailzationDiagnosisData.getInsurLevel()?"//":hospitailzationDiagnosisData.getInsurLevel());
            			dataRow.createCell(10).setCellValue(null==hospitailzationDiagnosisData.getTreatResult()?"//":hospitailzationDiagnosisData.getTreatResult());
            		}
				}
            	
            	endRowNum = staRowNum+detailInfoList.size()+diagnosisList.size()+2+head1.length;
            }
            
        	
        }

        if (null != hospitalizationDailyData && hospitalizationDailyData.size()>0) {
            
            int staRowNum = 0;
            int endRowNum = 0;
            
            for(int i=0,j=hospitalizationDailyData.size();i<j;i++){
            	staRowNum = endRowNum;
            	JSONObject jsonObject = (JSONObject) hospitalizationDailyData.get(i);
            	
            	Integer hospitalId = jsonObject.getInteger("hospitalId");
            	String hospitalizationNo = jsonObject.getString("hospitalizationNo");
            	String admissionTimes = jsonObject.getString("admissionTimes");
            	String admissionTime = jsonObject.getString("admissionTime");
            	String dischargeTime = jsonObject.getString("dischargeTime");
            	String deptName = jsonObject.getString("deptName");
            	String treatDoctor = jsonObject.getString("treatDoctor");
            	String settlementTime = jsonObject.getString("settlementTime");
            	String prepayments = jsonObject.getString("prepayments");
            	String bedNo = jsonObject.getString("bedNo");
            	
            	String[] head1 = new String[]{"医院号","住院编号","住院次数","入院时间","出院时间","部门名称","主治医生","清算时间","预交金额","床位号"};
            	
            	Object[] data1 = new Object[]{hospitalId,hospitalizationNo,admissionTimes,admissionTime,dischargeTime,deptName,treatDoctor,settlementTime,prepayments,bedNo};
            	for(int d=0;d<head1.length;d++){
            		Row headRow1 = sheet3.createRow(staRowNum+d);
            		sheet3.setColumnWidth(d, 5000);
            		Cell cell = headRow1.createCell(0);
            		cell.setCellValue(head1[d]);
            		cell.setCellStyle(headstyle); 
            		headRow1.createCell(1).setCellValue(null==data1[d]?"//":data1[d]+"");
            	}
            	
            	JSONArray feeItemInfoArray = jsonObject.getJSONArray("feeItemInfo");
            	List<HospitailzationDailyFeeItemInfoData> feeItemInfoList = null;
            	if (null != feeItemInfoArray) {
            		feeItemInfoList = JSONArray.parseArray(feeItemInfoArray.toString(), HospitailzationDailyFeeItemInfoData.class);
				}
            	
            	if (CollectionUtils.isNotEmpty(feeItemInfoList)) {
					
            		String[] head2 = new String[]{"项目序号","项目名称","项目类别","缴费项目价格单价","缴费项目价格总价","应付金额","支付金额","预交金收支差额","结算时间","缴费项目描述","数量","单位"};
            		Row dataRow2 = sheet3.createRow(staRowNum+head1.length);
            		for(int d=0;d<head2.length;d++){
            			sheet3.setColumnWidth(d, 5000);
            			Cell cell = dataRow2.createCell(d);
            			cell.setCellValue(head2[d]);
            			cell.setCellStyle(headstyle);
            		}
            		
            		for (HospitailzationDailyFeeItemInfoData hospitailzationDailyfeeItemInfoData : feeItemInfoList) {
            			Row dataRow = sheet1.createRow(sheet3.getLastRowNum()+1);
            			dataRow.createCell(0).setCellValue(null==hospitailzationDailyfeeItemInfoData.getItemNo()?"//":hospitailzationDailyfeeItemInfoData.getItemNo());
            			dataRow.createCell(1).setCellValue(null==hospitailzationDailyfeeItemInfoData.getItemName()?"//":hospitailzationDailyfeeItemInfoData.getItemName());
            			dataRow.createCell(2).setCellValue(null==hospitailzationDailyfeeItemInfoData.getItemClass()?"//":hospitailzationDailyfeeItemInfoData.getItemClass());
            			dataRow.createCell(3).setCellValue(null==hospitailzationDailyfeeItemInfoData.getItemPrice()?"//":hospitailzationDailyfeeItemInfoData.getItemPrice());
            			dataRow.createCell(4).setCellValue(null==hospitailzationDailyfeeItemInfoData.getTotalExpenses()?"//":hospitailzationDailyfeeItemInfoData.getTotalExpenses());
            			dataRow.createCell(5).setCellValue(null==hospitailzationDailyfeeItemInfoData.getShouldPaymentAmount()?"//":hospitailzationDailyfeeItemInfoData.getShouldPaymentAmount());
            			dataRow.createCell(6).setCellValue(null==hospitailzationDailyfeeItemInfoData.getActualPaymentAmount()?"//":hospitailzationDailyfeeItemInfoData.getActualPaymentAmount());
            			dataRow.createCell(7).setCellValue(null==hospitailzationDailyfeeItemInfoData.getPrepayments()?"//":hospitailzationDailyfeeItemInfoData.getPrepayments());
            			dataRow.createCell(8).setCellValue(null==hospitailzationDailyfeeItemInfoData.getBillingTime()?"//":hospitailzationDailyfeeItemInfoData.getBillingTime());
            			dataRow.createCell(9).setCellValue(null==hospitailzationDailyfeeItemInfoData.getItemSpec()?"//":hospitailzationDailyfeeItemInfoData.getItemSpec());
            			dataRow.createCell(10).setCellValue(null==hospitailzationDailyfeeItemInfoData.getAmount()?"//":hospitailzationDailyfeeItemInfoData.getAmount());
            			dataRow.createCell(11).setCellValue(null==hospitailzationDailyfeeItemInfoData.getUnits()?"//":hospitailzationDailyfeeItemInfoData.getUnits());
            		}
            		
				}
            	
            	
            	JSONArray diagnosisInfoArray = jsonObject.getJSONArray("diagnosisInfo");
            	List<HospitailzationDailyDiagnosisInfoData> diagnosisInfoList = null;
            	if (null != diagnosisInfoArray) {
            		diagnosisInfoList = JSONArray.parseArray(diagnosisInfoArray.toString(), HospitailzationDailyDiagnosisInfoData.class);
				}
            	
            	if (CollectionUtils.isNotEmpty(diagnosisInfoList)) {
            		
            		String[] head3 = new String[]{"诊断类型","诊断序号","诊断代码","诊断名称","医保诊断编码","医保诊断名称","诊断级别","治疗结果"};
            		Row headRow3 = sheet3.createRow(staRowNum+feeItemInfoList.size()+1+head1.length);
            		for(int d=0;d<head3.length;d++){
            			sheet3.setColumnWidth(d, 5000);
            			Cell cell = headRow3.createCell(d);
            			cell.setCellValue(head3[d]);
            			cell.setCellStyle(headstyle);
            		}
					
            		for (HospitailzationDailyDiagnosisInfoData hospitailzationDailyDiagnosisInfoData : diagnosisInfoList) {
            			Row dataRow = sheet1.createRow(sheet3.getLastRowNum()+1);
            			dataRow.createCell(0).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getDiagnosiType()?"//":hospitailzationDailyDiagnosisInfoData.getDiagnosiType());
            			dataRow.createCell(1).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getDiagnosisNo()?"//":hospitailzationDailyDiagnosisInfoData.getDiagnosisNo());
            			dataRow.createCell(2).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getDiagnosisCode()?"//":hospitailzationDailyDiagnosisInfoData.getDiagnosisCode());
            			dataRow.createCell(3).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getDiagnosisName()?"//":hospitailzationDailyDiagnosisInfoData.getDiagnosisName());
            			dataRow.createCell(4).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getInsurDiagnosisCode()?"//":hospitailzationDailyDiagnosisInfoData.getInsurDiagnosisCode());
            			dataRow.createCell(5).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getInsurDiagnosisName()?"//":hospitailzationDailyDiagnosisInfoData.getInsurDiagnosisName());
            			dataRow.createCell(6).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getDiagnosisLevel()?"//":hospitailzationDailyDiagnosisInfoData.getDiagnosisLevel());
            			dataRow.createCell(7).setCellValue(null==hospitailzationDailyDiagnosisInfoData.getTreatResult()?"//":hospitailzationDailyDiagnosisInfoData.getTreatResult());
            			
            		}
            		
				}
            	
            	endRowNum = staRowNum+feeItemInfoList.size()+diagnosisInfoList.size()+2+head1.length;
            	
            }
            
        }

        return wb;

    }
	
	
	
	public static void main(String[] args) {
		System.err.println(HttpClientUtil.executeByGET(token_url)); 
	}

}
