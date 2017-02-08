package cn.leepon.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.leepon.mapper.HospitalMapper;
import cn.leepon.po.THospital;
import cn.leepon.service.HospitalService;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2017年1月23日 下午5:12:26   
 */
@Service
public class HospitalServiceImpl implements HospitalService {
	
	@Autowired
	HospitalMapper hospitalMapper;

	@Override
	public Map<String, String> getHospitalInfo2Map() {
		List<THospital> list = hospitalMapper.getHospitalList();
		Map<String,String> map = new HashMap<>();
		for (THospital hospital : list) {
			map.put(hospital.getHospitalId()+"", hospital.getHospitalName());
		}
		return map;
	}

}
