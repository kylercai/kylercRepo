package kylerc.microsoft.azure.samples.azureblob.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

public class AzureStorageSASToken {

    private static String accountname = "storacctsuswest" + "\n" ;
    private static String signedpermissions = "r"+ "\n" ;
    private static String signedservice = "b"+ "\n" ;
    private static String signedresourcetype = "sco"+ "\n" ;
    private static String signedstart = "2018-11-21T00:00:00Z"+ "\n" ;
    private static String signedexpiry = "2018-11-23T00:00:00Z"+ "\n" ;
//    private static String signedIP = "192.168.1.1" +"\n" ;
    private static String signedIP = "\n" ;
    private static String signedProtocol = "https"+ "\n" ;
//    private static String signedProtocol = "https,http"+ "\n" ;
    private static String signedversion = "2017-11-09"+"\n" ;


    private static byte[]  keyBytes = Base64.getDecoder().decode("<your storage account key>");

    public static void main(String... args) throws Exception {

        String StringToSign = accountname+signedpermissions+signedservice+signedresourcetype+signedstart
                                +signedexpiry+signedIP+signedProtocol+signedversion;
        
//        StringToSign = "GET\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\nx-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\\nx-ms-version:2015-02-21\\n" +
//        "/" + accountname + "/" + "demo\\n" + "comp:metadata\\nrestype:container\\ntimeout:20";
//        System.out.println(Arrays.toString(keyBytes));
//        System.out.println(URLEncoder.encode(SignUp(keyBytes,StringToSign)));
        String token = URLEncoder.encode(SignUp(keyBytes,StringToSign));
        System.out.println(token);
    	getUrl(token);
    }


    public static String SignUp(byte[] keyBytes, String plain) throws UnsupportedEncodingException {

        //String plainEncode = URLDecoder.decode(plain,"UTF8");
        String plainEncode = URLEncoder.encode(plain,"UTF8");
        byte[] plainBytes = plainEncode.getBytes();

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hashs = sha256_HMAC.doFinal(plainBytes);
            String hash = Base64.getEncoder().encodeToString(hashs);
	       return hash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
  

    public static void getUrl(String token)  throws Exception{
    	String url = "https://storacctsuswest.blob.core.windows.net/images/01.jpg";
    	StringBuilder command = new StringBuilder();
    	command.append(url);
    	command.append("?sv=").append(signedversion).append("&ss=").append(signedservice).append("&srt=").append(signedresourcetype).
    	append("&sp=").append(signedpermissions).append("&");
    	command.append("se=").append(signedexpiry).append("&st=").append(signedstart).append("&spr=").append(signedProtocol)
    	.append("&sig=").append(token);
    	
    	String new_url = command.toString();
    	new_url = new_url.replaceAll("\n", "");
//    	System.out.println("repla==>"+ url);
    	System.out.println("url==>" + new_url);
    	LayeredConnectionSocketFactory sslsf = null;
		try {
			SSLContextBuilder s = new SSLContextBuilder();
			s.loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			});
			SSLContext sslcontext = s.build();
			sslsf = new SSLConnectionSocketFactory(sslcontext, new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		RequestConfig config = RequestConfig.custom().setConnectTimeout(120 * 1000).setSocketTimeout(3600 * 1000).build();
		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainsf).register("https", sslsf).build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
		connManager.setValidateAfterInactivity(0);
		connManager.setMaxTotal(1000);
		connManager.setDefaultMaxPerRoute(1000);
    	CloseableHttpClient httpClient = HttpClients.custom().disableAutomaticRetries().setConnectionManager(connManager).setDefaultRequestConfig(config).build();
    	
    	HttpGet get = new HttpGet(new_url);
    	CloseableHttpResponse response = httpClient.execute(get);

		System.out.println(response.getStatusLine());

		File file = new File("D://test.jpg");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(file);

		InputStream in = null;
		try {
			in = response.getEntity().getContent();
			byte[] bs = new byte[1024];
			int i = -1;
			while ((i = in.read(bs)) > 0) {
				fout.write(bs, 0, i);
			}
		} finally {
			in.close();
			fout.close();
			get.releaseConnection();
		}
    }
}