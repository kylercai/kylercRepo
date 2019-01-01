package kylerc.microsoft.azure.samples.azureblob.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
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

public class AzureStorageUtil {

	//GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\nx-ms-version:2015-02-21\n/myaccount/mycontainer\ncomp:metadata\nrestype:container\ntimeout:20  
    public static String accountname = "storacctsuswest" ;
//    private static String Date = "2018-11-13T16:28:09Z"+ "\n" ;
//    private static String VERB = "get";
//    private static String signedresourcetype = "sco"+ "\n" ;
//    private static String signedstart = "2018-11-13T16:28:09Z"+ "\n" ;
//    private static String signedexpiry = "2018-11-14T17:28:09Z"+ "\n" ;
//    private static String signedIP = "\n" ;
//    private static String signedProtocol = "https"+ "\n" ;
//    private static String signedProtocol = "https,http"+ "\n" ;
//    private static String signedversion = "2017-11-09"+"\n" ;
    
    public static String verbr  =  "GET" + "\n";  
    public static String Content_Encoding  =  "\n";  
    public static String Content_Language = "\n";  
    public static String Content_Length =  "\n";  
    public static String Content_MD5 = "\n" ;  
    public static String Content_Type =  "\n";  
    public static String Date = "\n";
//    private static String date  =  "Wed, 14 Nov 2018 17:49:12 GMT\n";  
//    private static String date  =  "2018-11-14T15:17:01.9166177Z\n";  
    public static String If_Modified_Since  = "\n";  
    public static String If_Match =  "\n";  
    public static String If_None_Match = "\n";  
    public static String If_Unmodified_Since = "\n";  
    public static String Range = "\n";
    //x-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\nx-ms-version:2015-02-21\n    /*CanonicalizedHeaders*/  
//    private static String x_ms_date = "Fri, 26 Jun 2015 23:39:12 GMT\n";
//    private static String x_ms_date = "Wednesday, 14 Nov 2018 10:07:10 GMT\n";
//    private static String x_ms_date = "2018-11-14T15:00:03.9498403Z\n";
//    private static String x_ms_date = "x-ms-date:"+Date;
//    private static String x_ms_version = "x-ms-version:2015-02-21\n";
//    private static String CanonicalizedHeaders = x_ms_date+x_ms_version;
//    //private static String CanonicalizedResource = "/" + accountname + "/images\n" + "restype:container\ntimeout:20";
//    private static String CanonicalizedResource = "/" + accountname + "/images/01.jpg";
///myaccount /mycontainer\ncomp:metadata\nrestype:container\ntimeout:20    /*CanonicalizedResource*/  

    public static String x_ms_date = "";
    public static String x_ms_version = "";
    public static String CanonicalizedHeaders = "";
    public static String CanonicalizedResource = "";

    public static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
    public static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final int ExpectedBlobQueueCanonicalizedStringLength = 300;
    public static final String PREFIX_FOR_STORAGE_HEADER = "x-ms-";

    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LANGUAGE = "Content-Language";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DATE = "Date";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String IF_MATCH = "If-Match";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String RANGE = "Range";
    
    public static final String EMPTY_STRING = "";
    public static final Locale LOCALE_US = Locale.US;
    public static final Pattern CRLF  = Pattern.compile("\r\n", Pattern.LITERAL);
    
    public static byte[]  keyBytes = Base64.getDecoder().decode("<Your Azure storage account connection access key>");

    protected static String getGMTDateTime() {
        final DateFormat formatter = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
        formatter.setTimeZone(GMT_ZONE);
        Date date = new Date();
        String gmtTime = formatter.format(date);
        return gmtTime;
    }
    
    public static void main(String... args) throws Exception {
    	accessblob(accountname);
    }

    public static String SignUp(byte[] keyBytes, String plain) throws UnsupportedEncodingException {

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
    
    protected static String getStandardHeaderValue(final HttpURLConnection conn, final String headerName) {
    	//Map headers = conn.getHeaderFields();
        String headerValue = "";
//    	headerValue = headers.toString();
    	headerValue = conn.getHeaderField(headerName);
//    	headerValue = conn.getRequestProperty(headerName);
        
        // Coalesce null value
        return headerValue == null ? EMPTY_STRING : headerValue;
    }
    
    public static void addCanonicalizedHeaders(final HttpURLConnection conn, final StringBuilder canonicalizedString) {
        // Look for header names that start with
        // HeaderNames.PrefixForStorageHeader
        // Then sort them in case-insensitive manner.

        //final Map<String, List<String>> headers = conn.getRequestProperties();
        Map<String, List<String>> headers = conn.getHeaderFields();
        ArrayList<String> httpStorageHeaderNameArray = new ArrayList<String>();

        for (String key : headers.keySet()) {
            if (key != null && key.toLowerCase(LOCALE_US).startsWith(PREFIX_FOR_STORAGE_HEADER)) {
                httpStorageHeaderNameArray.add(key.toLowerCase(LOCALE_US));
            }
        }
        
        Collections.sort(httpStorageHeaderNameArray);

        // Now go through each header's values in the sorted order and append
        // them to the canonicalized string.
        for (String key : httpStorageHeaderNameArray) {
            StringBuilder canonicalizedElement = new StringBuilder(key);
            String delimiter = ":";
            ArrayList<String> values = getHeaderValues(headers, key);

            boolean appendCanonicalizedElement = false;
            // Go through values, unfold them, and then append them to the
            // canonicalized element string.
            for (String value : values) {
                if (value != null) {
                    appendCanonicalizedElement = true;
                }

                // Unfolding is simply removal of CRLF.
                String unfoldedValue = CRLF.matcher(value)
                    .replaceAll(Matcher.quoteReplacement(EMPTY_STRING));

                // Append it to the canonicalized element string.
                canonicalizedElement.append(delimiter);
                canonicalizedElement.append(unfoldedValue);
                delimiter = ",";
            }

            // Now, add this canonicalized element to the canonicalized header
            // string.
            if (appendCanonicalizedElement) {
                appendCanonicalizedElement(canonicalizedString, canonicalizedElement.toString());
            }
        }
    }

    public static ArrayList<String> getHeaderValues(final Map<String, List<String>> headers, final String headerName) {

        final ArrayList<String> arrayOfValues = new ArrayList<String>();
        List<String> values = null;

        for (final Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().toLowerCase(LOCALE_US).equals(headerName)) {
                values = entry.getValue();
                break;
            }
        }
        if (values != null) {
            for (final String value : values) {
                // canonicalization formula requires the string to be left
                // trimmed.
                arrayOfValues.add(trimStart(value));
            }
        }
        return arrayOfValues;
    }

    public static String trimStart(final String value) {
        int spaceDex = 0;
        while (spaceDex < value.length() && value.charAt(spaceDex) == ' ') {
            spaceDex++;
        }

        return value.substring(spaceDex);
    }

    protected static void appendCanonicalizedElement(final StringBuilder builder, final String element) {
        builder.append("\n");
        builder.append(element);
    }
    
    protected static String constructSignString(HttpURLConnection conn) {
        // The first element should be the Method of the request.
        // I.e. GET, POST, PUT, or HEAD.
        final StringBuilder canonicalizedString = new StringBuilder(ExpectedBlobQueueCanonicalizedStringLength);
        canonicalizedString.append(conn.getRequestMethod());

        // The next elements are
        // If any element is missing it may be empty.
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, CONTENT_ENCODING));
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, CONTENT_LANGUAGE));
        String strLength = getStandardHeaderValue(conn, CONTENT_LENGTH);
        if ( !strLength.equals(EMPTY_STRING)) {
            long contentLength = new Long(strLength).longValue();
        	strLength = String.valueOf(contentLength);
        }
        appendCanonicalizedElement(canonicalizedString, strLength);
        
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, CONTENT_MD5));
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, CONTENT_TYPE));

        //String dateString = getStandardHeaderValue(conn, DATE);
        String dateString = EMPTY_STRING;
        // If x-ms-date header exists, Date should be empty string
        appendCanonicalizedElement(canonicalizedString, dateString);

        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, IF_MODIFIED_SINCE));
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, IF_MATCH));
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, IF_NONE_MATCH));
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, IF_UNMODIFIED_SINCE));
        appendCanonicalizedElement(canonicalizedString, getStandardHeaderValue(conn, RANGE));

        addCanonicalizedHeaders(conn, canonicalizedString);
        appendCanonicalizedElement(canonicalizedString, getCanonicalizedResource(accountname));

        return canonicalizedString.toString();    
    }

    protected static String getCanonicalizedResource(String account) {
    	return "/" + account + "/images/01.jpg";
    }
    
    public static void accessblob(String account_name) {
    	String url = "https://storacctsuswest.blob.core.windows.net/images/01.jpg";  	
    	String gmtDate = getGMTDateTime();
    	
    	HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)(new URL(url)).openConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String strToSign = verbr + Content_Encoding + Content_Language + Content_Length
                + Content_MD5 + Content_Type + Date + If_Modified_Since + If_Match
                + If_None_Match + If_Unmodified_Since + Range + CanonicalizedHeaders + CanonicalizedResource;
		strToSign = constructSignString(conn);
		
//        x_ms_date = "x-ms-date:"+gmtDate;
//        x_ms_version = "x-ms-version:2015-02-21\n";
//        CanonicalizedHeaders = x_ms_date + "\n" + x_ms_version;
//        CanonicalizedResource = "/" + accountname + "/images/01.jpg";

        
        // GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Fri, 26 Jun 2015 23:39:12 GMT\nx-ms-version:2015-02-21\n
        // /myaccount/mycontainer\ncomp:metadata\nrestype:container\ntimeout:20
//        System.out.println(Arrays.toString(keyBytes));
//        System.out.println(URLEncoder.encode(SignUp(keyBytes,StringToSign)));
        String token = "";
		try {
			token = URLEncoder.encode(SignUp(keyBytes,strToSign));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
    	System.out.println("url==>" + url);
    	System.out.println("token==>" + token);
    	String auth = String.format("%s %s:%s", "SharedKey", account_name, token);
    	System.out.println("auth==>" + auth);
    	
//        final Canonicalizer canonicalizer = CanonicalizerFactory.getBlobQueueFileCanonicalizer(request);
//
//        final String stringToSign = canonicalizer.canonicalize(request, creds.getAccountName(), contentLength);
//
//        final String computedBase64Signature = StorageCredentialsHelper.computeHmac256(creds, stringToSign);

        conn.setRequestProperty("Authorization", auth);
        conn.setRequestProperty("x-ms-date", gmtDate);
        conn.setRequestProperty("Date", gmtDate);
        try {
			conn.connect();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
//    	HttpGet get = new HttpGet(new_url);
//    	//Authorization: SharedKey myaccount:ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=
//    	StringBuilder auth = new StringBuilder();
//    	System.out.println("token==>" + token);
//    	auth.append("SharedKey ").append(account_name).append(":").append(token);
//    	System.out.println("auth==>"+ auth.toString());
////    	System.out.println("CanonicalizedHeaders==>" +CanonicalizedHeaders);
////    	System.out.println("CanonicalizedResource==>" +CanonicalizedResource);
//    	get.addHeader("Date", date.replaceAll("\n", ""));
//    	get.addHeader("Authorization", auth.toString());
////    	get.addHeader("CanonicalizedHeaders", CanonicalizedHeaders);
////    	get.addHeader("CanonicalizedResource", CanonicalizedResource);
//    	CloseableHttpResponse response = httpClient.execute(get);
//
//		System.out.println(response.getStatusLine());
    }
    
}