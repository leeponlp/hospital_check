package cn.leepon.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.leepon.po.CheckParam;
import cn.leepon.service.CheckService;
import cn.leepon.service.HospitalService;
import cn.leepon.util.FastJsonUtil;
import cn.leepon.util.HttpClientUtil;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月19日 下午5:53:28   
 */
@Controller
public class CheckAction {
	
	private boolean flag = false;
	
	@Autowired
	CheckService checkService;
	
	@Autowired
	HospitalService hospitalService;
	
	@RequestMapping("/hospital/check")
	public String query(){
		
		return "hospitalcheck";
	}
	
	@RequestMapping("/hospital/checked")
	public void getCheckedReport(CheckParam checkParam,HttpServletRequest request,HttpServletResponse response){
		
		try {
			flag = false;
			checkService.getCheckedReport(checkParam,request,response);
			flag = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@RequestMapping("/monitor/progress")
	@ResponseBody
	public boolean monitorProgress(){
		return flag;
	}
	
	
	@RequestMapping("/hospital/map")
	@ResponseBody
	public Map<String, String> getHospitalInfo2Map(){
		HashMap<String, String> hospital = new HashMap<>();
		hospital.put("210004", "上海长海医院");
		hospital.put("210007", "海市闵行区中心医院");
		hospital.put("210016", "中国人民解放军第四一一医院");
		hospital.put("210023", "上海市光华中西医结合医院");
		return hospital;
		//return hospitalService.getHospitalInfo2Map();
	}
	
	
	
	public static void main(String[] args) {
		
        String hospitalization_url = "https://open.quyiyuan.com:8443/v1.0/insurance/hospitalization/settlement";
		
		String token ="5F3679781EA144AC9CC103C5807927C0";
		List<NameValuePair> post = new ArrayList<>();
		post.add(new BasicNameValuePair("token",token));
		//医院号
		post.add(new BasicNameValuePair("hospitalId","3790041")); 
		//病人姓名
		post.add(new BasicNameValuePair("patientName","杨逢春"));
		//身份证
		post.add(new BasicNameValuePair("idCardNo",""));
		//住院号
		post.add(new BasicNameValuePair("hospitalizationNo","42423"));
		//是否成人
		post.add(new BasicNameValuePair("childFlag","0"));
		//页数
		post.add(new BasicNameValuePair("page","1"));
		post.add(new BasicNameValuePair("amounts","50"));
		
		String hospitalizationStr = HttpClientUtil.executeByPOST(hospitalization_url, post); 
		JSONObject hospitalizationJsonData = FastJsonUtil.toJSONObject(hospitalizationStr);
		JSONObject jsonData = hospitalizationJsonData.getJSONObject("data");
		JSONArray arr = jsonData.getJSONArray("rows"); 
		for (Object object : arr) {
			JSONObject so = (JSONObject) object;
			Integer hospitalId = so.getInteger("hospitalId"); 
			System.err.println(hospitalId);
		}
		
		
	}

}
