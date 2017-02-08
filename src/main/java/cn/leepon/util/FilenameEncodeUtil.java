package cn.leepon.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;


public class FilenameEncodeUtil {
	
	 	
	/**
	 * 
	 * @Title: encodeFilename 
	 * @Description: TODO <针对不同浏览器，进行附件名的编码>
	 * @param @param filename
	 * @param @param agent
	 * @param @return 
	 * @return String  
	 * @throws
	 */
	public static String encodeFilename(String filename, String agent) {
		String urlFName = null;
		filename = filename.replaceAll("\r\n", "");
		filename = filename.replace("+", " ");
		if ( StringUtils.containsIgnoreCase(agent, "Firefox") ) { //火狐浏览器
			try {
				urlFName = Base64.encodeBase64String(filename.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				urlFName = filename;
				e.printStackTrace();
			}
			urlFName = ("=?UTF-8?B?"+ urlFName + "?=");
		} else {//IE及其他浏览器
			try {
				urlFName = URLEncoder.encode(filename, "utf-8");
			} catch (UnsupportedEncodingException e) {
				urlFName = filename;
				e.printStackTrace();
			}
		}
		return urlFName;
	}
	
	
	
}
