package kylerc.microsoft.azure.samples.azureblob.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;

import org.mozilla.universalchardet.UniversalDetector;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class CNTVBlobOperator {
	protected String connectionString = "";
	protected CloudStorageAccount storageAccount = null;
	protected CloudBlobClient blobClient = null;

	public CNTVBlobOperator(String connectionString) {
		super();
		this.connectionString = connectionString;
		initBlobOperator(this.connectionString);
	}

	/**
	 * 上传本地文件到blob，使用文件本身的名字作为blob名字
	 * @param container：blob容器名
	 * @param localFile：本地文件完整路径
	 * @param blobPath：blob在容器下的完整路径
	 */
	public void uploadLocalFileToBlob(String container, String localFile, String blobPath) 
			throws URISyntaxException, StorageException, FileNotFoundException, IOException {
		CloudBlobContainer cbc = getBlobContainer(container);
		
        File sourceFile = new File(localFile);

        // the blob name is not specified, so we use the local file name for the blob naming
        String filename = sourceFile.getName();
        String validBlobName = getValidBlobName(filename);
		if ( !blobPath.endsWith("/") ) {
			blobPath = blobPath + "/";
		}
		
		CloudBlockBlob cbb = cbc.getBlockBlobReference(blobPath + validBlobName);
		cbb.upload(new FileInputStream(sourceFile), sourceFile.length());
	}

	/**
	 * 上传本地文件到blob，并指定blob的名字
	 * @param container：blob容器名
	 * @param localFile：本地文件完整路径
	 * @param blobPath：blob在容器下的完整路径
	 * @param blobName：blob的名字
	 */
	public void uploadLocalFileToBlob(String container, String localFile, String blobPath, String blobName) 
			throws URISyntaxException, StorageException, FileNotFoundException, IOException {
		CloudBlobContainer cbc = getBlobContainer(container);
		
        File sourceFile = new File(localFile);
        String validBlobName = getValidBlobName(blobName);
		if ( !blobPath.endsWith("/") ) {
			blobPath = blobPath + "/";
		}
		
		CloudBlockBlob cbb = cbc.getBlockBlobReference(blobPath + validBlobName);
		cbb.upload(new FileInputStream(sourceFile), sourceFile.length());
	}

	/**
	 * 根据输入的名字，进行必要的转码，获得可被接受的上传blob名字
	 * 策略：
	 * 1. 所有不含控制字符的文件名，不用转码，即使其中有特殊字符（~`!@#$%^&*():;"'<>,.?[]{}+_-| ）， 都可以正常上传。上传成功后，blob显示原文件名
	 * 2. 含有控制字符的文件名，将文件名中的控制字符进行%转码，用转码后的名字上传。上传成功后，blob显示对控制字符进行转码后的文件名
	 * @param inputName：希望采用的blob名字
	 * @return：转码后可被接受上传的blob名字
	 */
	protected String getValidBlobName(String inputName) throws UnsupportedEncodingException {
		StringBuffer validName = new StringBuffer("");
//		byte[]  nameBytes = inputName.getBytes();
//		
//		String encodingType = guessEncoding(nameBytes);
//		//System.out.println("encoding type = " + encodingType);
//		String rawName = URLDecoder.decode(inputName, encodingType);
//		byte[] rawBytes = rawName.getBytes();
		byte[] rawBytes = inputName.getBytes();
		
		/** Policy to get valid blob name:
		 *  控制字符定义：Unicode值: 0x00-0x1F, 0x7F
		 */
		for (byte b : rawBytes ) {
			if ( ((0<=b) && (b<=31)) || (b==127) ) {
				// this is a control character. Control characters: 0x00-0x1F, 0x7F
				String subStr = URLEncoder.encode(String.valueOf((char)b), "UTF-8");
				validName.append(subStr);
			} else {
				validName.append((char)b);
			}
		}
		
		return validName.toString();
	}

	protected static String guessEncoding(byte[] bytes) {
        String DEFAULT_ENCODING = "UTF-8";
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
	}	
	
	private void initBlobOperator(String connectionStr) {
		connectionString = connectionStr;
		try {
			storageAccount = CloudStorageAccount.parse(connectionString);
			blobClient = storageAccount.createCloudBlobClient();
		} catch (InvalidKeyException e) {
			blobClient = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			blobClient = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected CloudBlobContainer getBlobContainer(String containerName) throws URISyntaxException, StorageException {
		CloudBlobContainer container = null;
		if ( blobClient != null ) {
	        container = blobClient.getContainerReference(containerName);
		}
		if ( container != null ) {
	        container.createIfNotExists();
		}
        return container;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String connectionStr = "<Your Azure storage account connection string>";

		CNTVBlobOperator blobOp = new CNTVBlobOperator(connectionStr);
		try {
			// Test 1: upload a local file with normal file name
			blobOp.uploadLocalFileToBlob("export", "c:\\temp\\aa.jpg", "home0/docs/life/ttys/p/");
			
			// Test 2: upload a local file without specifying expected blob name (contains special chars but NO control chars)
			blobOp.uploadLocalFileToBlob("export", "c:\\temp\\~`!@#$%^&(),.[]{}+_- 2345678.jpg", "home0/docs/life/ttys/p/");
			
			// Test 3: upload a local file with specifying expected blob name containing control chars
			blobOp.uploadLocalFileToBlob("export", "c:\\temp\\~`!@#$%^&(),.[]{}+_- 2345678.jpg", "home0/docs/life/ttys/p/", "~`!@#$%^&(),.[]{}+_- 2345678.jpg");

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
