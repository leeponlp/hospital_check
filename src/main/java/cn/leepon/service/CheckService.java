package cn.leepon.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leepon.po.CheckParam;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年12月19日 下午4:48:20   
 */
public interface CheckService {
	
	public void getCheckedReport(CheckParam checkParam, HttpServletRequest request, HttpServletResponse response);   

}

