package cn.leepon.po;

import lombok.Data;

/**
 * 
 * @ClassName: HospitalPlusToken 
 * @Description: TODO
 * @author leepon
 * @date 2016年12月19日 下午5:44:28 
 *
 */
@Data
public class HospitalPlusToken {
	
	private boolean success;
	
	private String resultCode;
	
	private String time;
	
	private String message;
	
	private TokenData data = new TokenData();
	
	@Data
	public class TokenData{
		
		private String token;
		
		private Integer validTime;
	}

}
