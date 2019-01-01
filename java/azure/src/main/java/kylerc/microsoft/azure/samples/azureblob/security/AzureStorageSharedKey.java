package kylerc.microsoft.azure.samples.azureblob.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
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


public class AzureStorageSharedKey {
    public static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final Locale LOCALE_US = Locale.US;
    public static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

	//GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\nx-ms-version:2015-02-21\n/myaccount/mycontainer\ncomp:metadata\nrestype:container\ntimeout:20
	
	// KYLER
	private static String url = "https://storacctsuswest.blob.core.windows.net/images/01.jpg";
    private static String accountname = "storacctsuswest";
    private static byte[]  keyBytes = Base64.getDecoder().decode("<your storage account key>");

    //    private static String Date = "2018-11-13T16:28:09Z"+ "\n" ;
//    private static String VERB = "get";
//    private static String signedresourcetype = "sco"+ "\n" ;
//    private static String signedstart = "2018-11-13T16:28:09Z"+ "\n" ;
//    private static String signedexpiry = "2018-11-14T17:28:09Z"+ "\n" ;
//    private static String signedIP = "\n" ;
//    private static String signedProtocol = "https"+ "\n" ;
//    private static String signedProtocol = "https,http"+ "\n" ;
//    private static String signedversion = "2017-11-09"+"\n" ;
    
    private static String verbr  =  "GET" + "\n";  
    private static String Content_Encoding  =  "\n";  
    private static String Content_Language = "\n";  
    private static String Content_Length =  "\n";  
    private static String Content_MD5 = "\n" ;  
    private static String Content_Type =  "\n";  
    private static String Date = "\n";
    private static String If_Modified_Since  = "\n";  
    private static String If_Match =  "\n";  
    private static String If_None_Match = "\n";  
    private static String If_Unmodified_Since = "\n";  
    private static String Range = "\n";
    //x-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\nx-ms-version:2015-02-21\n    /*CanonicalizedHeaders*/  
    private static String x_ms_date = "";
    private static String x_ms_version = "";
    private static String CanonicalizedHeaders = "";
    private static String CanonicalizedResource = "";
    
    public static void main(String... args) throws Exception {
    	String gmtDate = getGMTDateTime();
    	System.out.println("gmtDate===>" + gmtDate);
        x_ms_date = "x-ms-date:"+gmtDate;
        x_ms_version = "x-ms-version:2015-02-21";
        CanonicalizedHeaders = x_ms_date + "\n" + x_ms_version + "\n";
        CanonicalizedResource = "/" + accountname + "/images/01.jpg";

        String StringToSign = verbr + Content_Encoding + Content_Language + Content_Length + Content_MD5 + Content_Type 
        		+ Date + If_Modified_Since + If_Match + If_None_Match + If_Unmodified_Since + Range 
        		+ CanonicalizedHeaders + CanonicalizedResource;
        //StringToSign = "GET\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\nx-ms-date: Wed, 19 Dec 2018 08:57:10 GMT\\nx-ms-version:2015-02-21\\n/cntvusweaspblobstorage02/asp/hls/450/0303000a/3/default/1c4d6177baae4412fde8888cf0d01402/450.m3u8";
        
        String toPrint = StringToSign;
        toPrint = toPrint.replaceAll("\n", "/n");
        //System.out.println("signstr==>" + toPrint);
        //System.out.println("key==>" + String.valueOf(keyBytes));
        
        // GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\nx-ms-version:2015-02-21\n
        // /myaccount/mycontainer\ncomp:metadata\nrestype:container\ntimeout:20
        //String token = URLEncoder.encode(SignUp(keyBytes,StringToSign));
        String token = SignUp(keyBytes,StringToSign);

        //String token = "";
    	getUrl(accountname,token, gmtDate, x_ms_version.replace("x-ms-version:", ""));
    }

    public static void getUrl(String account_name, String token, String date, String version)  throws Exception{
     	CloseableHttpClient httpClient = getCloseableHttpClient();
    	
     	System.out.println("url==>" + url);
    	System.out.println("x-ms-date:" + date);
    	System.out.println("x-ms-version:" + version);
    	//System.out.println("Authorization:" + token);
    	//Authorization: SharedKey myaccount:ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=
    	StringBuilder auth = new StringBuilder();
    	auth.append("SharedKey ").append(account_name).append(":").append(token);
    	System.out.println("Authorization:"+ auth.toString());


    	HttpGet get = new HttpGet(url);
    	get.addHeader("x-ms-date", date);
    	get.addHeader("x-ms-version", version);
    	get.addHeader("Authorization", auth.toString());
    	//Header[] headers = get.getAllHeaders();
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
			fout.close();
			get.releaseConnection();
		}
    }

    private static CloseableHttpClient getCloseableHttpClient() {
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

    	return httpClient;
    }
    
    public static String SignUp(byte[] keyBytes, String plain) throws UnsupportedEncodingException {

        String plainEncode = URLEncoder.encode(plain,"UTF8");
        byte[] plainBytes = plainEncode.getBytes();
    	//byte[] plainBytes = plain.getBytes();

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

    protected static String getGMTDateTime() {
        final DateFormat formatter = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
        formatter.setTimeZone(GMT_ZONE);
        Date date = new Date();
        String gmtTime = formatter.format(date);
        return gmtTime;
    }
}