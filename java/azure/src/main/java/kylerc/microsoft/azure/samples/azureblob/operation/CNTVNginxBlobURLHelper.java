package kylerc.microsoft.azure.samples.azureblob.operation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class CNTVNginxBlobURLHelper {
	protected static char[] notDecodeBytes = {' ', '+'};
	protected static String[] notDecodeBytesHexCode = {"%20", "%2B"};
	protected static char[] notEncodeBytes = {' ', '+', '~', '!', '*', '(', ')', '/', '\''};
	protected static String[] controlCharsCode = {"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08",
							"%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F", "%10", "%11", "%12", "%13", "%14",
							"%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%7F"};

	/**
	 * 根据浏览器传递过来的URL，进行必要的转换，映射到正确的blob访问URL
	 * 此方法中的映射算法，和向blob上传时使用的blob名字转码规则相匹配
	 * 此方法/算法，可用于Nginx的相关插件，对页面中嵌入的URL点击访问进行重定向到正确的blob URL
	 * @param receivedURL：浏览器传递过来的URL。浏览器已经会对控制字符，%等URL不接受的字符进行了转码。此外，URL不应该出现字符'#'
	 * @param codecType：指定的编码方法
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getValidBlobURL(String receivedURL, String codecType) throws UnsupportedEncodingException {
		String validBlobURL = "";
		String protocolSeparator = "://";
		int slashPos = receivedURL.indexOf(protocolSeparator);
		String urlProtocol = "";
		String urlPath = "";
		
		// 取出协议部分（http://, https://等）之后的部分，放入变量urlPath中，进行后续转码
		if ( slashPos != -1 ) {
			urlProtocol = receivedURL.substring(0, slashPos);
			urlPath = receivedURL.substring(slashPos+protocolSeparator.length(), receivedURL.length());
		} else {
			urlProtocol = "";
			urlPath = receivedURL;
		}
		
		/**
		 *    策略：
		 *    5.1 对收到的URL先进行 decode，还原成统一的原始字符，但以下情况例外：
		 *    5.1.1 如果URL中有空格符' '和加号符‘+’，保留原样而不进行decode，因为' '和 ‘+'在decode之后，都变成' '，无法区分
		 */
		// 先用空格' '和加号'+'，对收到的URL进行分割，形成多个子串。
		// 例如，如传入的URL为： http://xxx/con1/path1/123+456+789.jpg，分割后得到以下子串
		// http://xxx/con1/path1/123   +   456   +   789.jpg
		ArrayList<String> splitURLs = splitURL(urlPath, notDecodeBytes);
		
		StringBuffer decodedURL = new StringBuffer("");
		// 对每个子串进行处理，得到decode后的URL。此步骤decode会还原成原始的字符串，主要用途在于消除不同浏览器对
		// 页面中嵌入URL转码的不同行为带来的影响
		for ( String suburl : splitURLs ) {
			if ( (suburl.length() == 1) && isCharInSet(suburl.charAt(0), notDecodeBytes) ) {
				// 如果子串是空格' '或者 '+'，那么不进行decode，原样保留。参考以上5.1.1注释
				decodedURL.append(suburl);
			} else {
				// 对非空格' '和非'+'的子串，按照指定编码方式decode
				decodedURL.append(URLDecoder.decode(suburl, codecType));
			}
		}
		
		System.out.println("decoded = " + decodedURL.toString());
		/**
		 *    5.2 再对5.1的结果进行 encode，但有例外情况：
		 *    5.2.1 对5.1结果中的空格符‘ ’和加号符‘+’，分别用"%20"和“%2B”进行替换
		 *    5.2.2 对5.1结果中这些字符：“~ ! * ( ) / ' ”， 保持原样不做encode，因为blob访问的URL这几个字符没有做%编码
		 */
		// 用字符' ', '+'，'~', '!', '*', '(', ')'等字符对5.1中decode得到的URL进行分割，形成多个子串
		splitURLs = splitURL(decodedURL.toString(), notEncodeBytes);
		
		StringBuffer encodedURL = new StringBuffer("");
		HashMap hexCodeMap = getNotDecodeBytesHexCodeMap(notDecodeBytes, notDecodeBytesHexCode);
		// 对每个子串进行处理，得到encode后的URL
		for ( String suburl : splitURLs ) {
			if ( (suburl.length() == 1) && isCharInSet(suburl.charAt(0), notEncodeBytes) ) {
				// 子串内容是这些字符之一：' ', '+'，'~', '!', '*', '(', ')', '/', '\''等字符
				
				if ( isCharInSet(suburl.charAt(0), notDecodeBytes) ) {
					// 如果子串是空格' '或者 '+'，那么分别用对应的%编码进行替换。参考以上5.2.1注释
					String hexCode = (String)hexCodeMap.get(suburl.charAt(0));
					encodedURL.append(hexCode);
				} else {
					// 如果子串是' ', '+', '~', '!', '*', '(', ')', '/', '\''等字符，保持原样放入最终URL。参考以上5.2.2注释
					encodedURL.append(String.valueOf(suburl.charAt(0)));
				}
			} else {
				// 对子串内容不包括' ', '+', '~', '!', '*', '(', ')', '/', '\''等字符的情况，按照指定编码方式encode
				encodedURL.append(URLEncoder.encode(suburl, codecType));
			}
		}
		
		/**
		 * 5.3： 在5.2的结果中，查找是否有 %00~%1F, %7F的字符串，如果有，需要对这样的子字符串再进行一次encode。
		 * 
		 * 举例说明：如果包含有%08，则说明浏览器传过来的URL中，浏览器对回退符进行了转码得到%08。这意味着之前我们有一个
		 * 文件名中包含回退符的本地文件上传到blob，按照我们上传blob的名字转义，真正上传到blob的名字不是回退符而是%08。
		 * 而要访问名字是%08的blob，需要向blob发送%2508，因此我们需要对%08再进行一次encode
		 */
		validBlobURL = handleControlChars(encodedURL.toString(), codecType);
		
		if ( !urlProtocol.isEmpty() ) {
			validBlobURL = urlProtocol + protocolSeparator + validBlobURL;
		}
		return validBlobURL;
	}
	
	protected static String handleControlChars(String inputStr, String codecType) throws UnsupportedEncodingException {
		String returnStr = inputStr;
		inputStr = inputStr.toUpperCase();
		for (String ctrlCode : controlCharsCode ) {
			if ( inputStr.contains(ctrlCode) ) {
				String encodedCtrlChar = URLEncoder.encode(ctrlCode, codecType);
				returnStr = returnStr.replaceAll(ctrlCode, encodedCtrlChar);
			}
		}
		
		return returnStr;
	}
	
	protected static boolean isCharInSet(char ch, char[] charSet) {
		boolean is = false;
		for ( char c : charSet ) {
			if ( c == ch ) {
				is = true;
				break;
			}
		}
		return is;
	}

	protected static ArrayList<String> splitURL(String receivedURL, char[] splitChars) {
		ArrayList<String> subURLs = new ArrayList<String>();
		
		//byte[] receivedURLBytes = receivedURL.getBytes();
		
		char[] receivedURLChars = new char[receivedURL.length()];
		receivedURL.getChars(0, receivedURL.length(), receivedURLChars, 0);

		StringBuffer suburl = new StringBuffer("");
		//for ( byte b : receivedURLBytes ) {
		for ( char ch : receivedURLChars ) {
			if ( isCharInSet(ch, splitChars) ) {
				if ( suburl.length() != 0 ) {
					subURLs.add(suburl.toString());
					suburl.delete(0, suburl.length());
				}
				subURLs.add(String.valueOf(ch));
				
			} else {
				suburl.append(ch);
			}
		}
		if ( suburl.length() != 0 ) {
			subURLs.add(suburl.toString());
		}
		
		return subURLs;
	}
	
	protected static HashMap getNotDecodeBytesHexCodeMap(char[] chars, String[] codes) {
		HashMap map = new HashMap();

		if ( chars.length == codes.length ) {
			for ( int i =0; i<chars.length; i++) {
				map.put(chars[i], codes[i]);
			}
		}
		
		return map;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/**
		 * 开始模拟现有web页面中嵌入的URL包含特殊和控制字符文件，页面中的URL链接被点击的情形 
		 */
		// urlEmbedded模拟我们在页面中嵌入的URL，指向有特殊和控制字符的blob
			String urlEmbeded = "";//args[1];
//			urlEmbeded = "https://cntvuswewebblobstorage02.blob.core.windows.net/export/home0/docs/life/ttys/p/~`!@#$%^&(),.[]{}+_- 2345678.jpg";
			urlEmbeded = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/101103____________________¤____.jpg";
			urlEmbeded = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/100566__¨___ì_______¨_.jpg";
			urlEmbeded = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/~`!@#$%^&(),.[]{}+_- 2345678.jpg";
//			urlEmbeded = "¨ì¨";
//			urlEmbeded = "¨";
			//urlEmbeded = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/¨.jpg";
//			urlEmbeded = "/export/home0/docs/advertisement/wahaha/sanji/p/cola^?.jpg";
//			urlEmbeded = "/export/home0/docs/advertisement/special/C16579/20061010/images/100918_???????? ?";
///			urlEmbeded = "/export/home0/docs/sports/special/C11680/20040813/images/101584_^?^?m.jpg";
//			urlEmbeded = "/export/home0/docs/zhuanti/chaoxian/images/tupian/3zailushang/?";
		
		// 当页面中嵌入的URL被点击后，浏览器会先对其中的%进行转码，然后发送
		String receivedURL = urlEmbeded.replaceAll("%", "%25");
		/**
		 * 点击之后，浏览器的请求会向后先传递给nginx
		 */
		
		// 以下部分模拟nginx对收到的URL进行转换，获取对应blob的正确访问URL
		String validURL;
		try {
			validURL = CNTVNginxBlobURLHelper.getValidBlobURL(receivedURL, "UTF-8");
			System.out.println(validURL);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
