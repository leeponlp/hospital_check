package cn.leepon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * HTTP工具类
 * 
 * @author leepon
 *
 */
@SuppressWarnings("all")
public class HttpClientUtil {

	static Logger logger = Logger.getLogger(HttpClientUtil.class);

	private static HttpClient httpClient = null;

	/**
	 * 创建线程安全的客户端对象
	 * 
	 * @return httpClient
	 */
	public synchronized static HttpClient getHttpClient() {
		if (httpClient == null) {
			try {
				KeyStore keyStore = KeyStore.getInstance("PKCS12");
				keyStore.load(new FileInputStream(new File("credential" + File.separator + "person-client.p12")),
						"qybaoxian.10053.x98g@20160811".toCharArray());
				SSLContext sslcontext = SSLContexts.custom()
						.loadTrustMaterial(new File("credential" + File.separator + "quyi-open-truststore.jks"),
								"quyi-trust-1956vgy@20160706".toCharArray(), new TrustSelfSignedStrategy())
						.loadKeyMaterial(keyStore, "qybaoxian.10053.x98g@20160811".toCharArray()).build();
				SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext,
						new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" }, null,SSLConnectionSocketFactory.getDefaultHostnameVerifier());
				httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
						.build();
			} catch (Exception e) {
				logger.info("get httpClient happened exception like :"+e.getMessage());
			} 
		}
		return httpClient;
	}

	/**
	 * POST方法提交JSON串
	 * 
	 * @param url
	 * @param json
	 * @return
	 */
	public static String executeByPOST(String url, JSONObject json) {

		String result = "";
		// 获取客户端对象
		HttpClient client = getHttpClient();

		HttpPost post = new HttpPost(url);
		try {
			StringEntity s = new StringEntity(json.toString());
			s.setContentEncoding("UTF-8");
			s.setContentType("application/json");
			post.setEntity(s);
			HttpResponse res = client.execute(post);
			result = EntityUtils.toString(res.getEntity());
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

		return result;
	}

	/**
	 * POST方式调用
	 *
	 * @param url
	 * @param params
	 *            参数为NameValuePair键值对对象
	 * @return 响应字符串
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static String executeByPOST(String url, List<NameValuePair> params) {

		// 创建客户端对象
		HttpClient httpclient = getHttpClient();

		HttpPost post = new HttpPost(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseJson = null;
		try {
			if (params != null) {
				// 创建请求实体
				post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			// 执行请求实体获取返回对象
			responseJson = httpclient.execute(post, responseHandler);
			logger.info("HttpClient POST请求结果：" + responseJson);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("HttpClient POST请求异常：" + e.getMessage());
		} 
		return responseJson;
	}

	/**
	 * Get方式请求
	 *
	 * @param url
	 *            带参数占位符的URL，例：http:///xxx/user/center.aspx?_action=
	 *            GetSimpleUserInfo&codes={0}&email={1}
	 * @param params
	 *            参数值数组，需要与url中占位符顺序对应
	 * @return 响应字符串
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static String executeByGET(String url, Object[] params) {

		// 创建客户端对象
		HttpClient httpclient = getHttpClient();
		// 格式化url
		String messages = MessageFormat.format(url, params);

		HttpGet get = new HttpGet(messages);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseJson = null;
		try {
			// 执行请求实体获取返回对象
			responseJson = httpclient.execute(get, responseHandler);
			logger.info("HttpClient GET请求结果：" + responseJson);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("HttpClient GET请求异常：" + e.getMessage());
		}
		return responseJson;
	}

	/**
	 * @param url
	 * @return
	 */
	public static String executeByGET(String url) {
		HttpClient httpclient = getHttpClient();

		HttpGet get = new HttpGet(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseJson = null;
		try {
			responseJson = httpclient.execute(get, responseHandler);
			logger.info("HttpClient GET请求结果：" + responseJson);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("HttpClient GET请求异常：" + e.getMessage());
		} 
		return responseJson;
	}

	/**
	 * 字符串转Json
	 * 
	 * @param str
	 * @return
	 */
	public static JSONObject toJson(String str) {
		if (!StringUtils.isEmpty(str)) {
			return JSONObject.parseObject(str);
		}
		return null;
	}

	

	public static void main(String[] args) {
		String executeByGET = executeByGET("https://open.quyiyuan.com:8443/v1.0/access/token/get?userName=SHBaoXianYanShiTest&password=E74F5B849961E5FE");
		System.err.println(executeByGET);
	}
}
