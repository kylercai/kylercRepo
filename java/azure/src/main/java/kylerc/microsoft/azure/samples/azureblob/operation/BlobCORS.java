// MIT License
// Copyright (c) Microsoft Corporation. All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE

package kylerc.microsoft.azure.samples.azureblob.operation;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.CorsHttpMethods;
import com.microsoft.azure.storage.CorsRule;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

/* *************************************************************************************************************************
* Summary: This application demonstrates how to use the Blob Storage service.
* It does so by creating a container, creating a file, then uploading that file, listing all files in a container, 
* and downloading the file. Then it deletes all the resources it created
* 
* Documentation References:
* Associated Article - https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java
* What is a Storage Account - http://azure.microsoft.com/en-us/documentation/articles/storage-whatis-account/
* Getting Started with Blobs - http://azure.microsoft.com/en-us/documentation/articles/storage-dotnet-how-to-use-blobs/
* Blob Service Concepts - http://msdn.microsoft.com/en-us/library/dd179376.aspx 
* Blob Service REST API - http://msdn.microsoft.com/en-us/library/dd135733.aspx
* *************************************************************************************************************************
*/
public class BlobCORS 
{
	/* *************************************************************************************************************************
	* Instructions: Update the storageConnectionString variable with your AccountName and Key and then run the sample.
	* *************************************************************************************************************************
	*/
//	public static final String storageConnectionString =
//	"DefaultEndpointsProtocol=https;" +
//	"AccountName=<account-name>;" +
//	"AccountKey=<account-key>";

	public static final String storageConnectionString =
	"DefaultEndpointsProtocol=https;" +
	"AccountName=ckdev03;" +
	"AccountKey=<your storage account key>";
	
    public static String hashMapToJson(HashMap<String, String> map) {
        String string = "{";
        for (Iterator<Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {
            Entry<String, String> e = (Entry<String, String>) it.next();
            string += "\"" + e.getKey() + "\":";
            string += "\"" + e.getValue() + "\",";
        }
        string = string.substring(0, string.lastIndexOf(","));
        string += "}";
        return string;
    }	

	public static void main( String[] args )
	{
		File sourceFile = null, downloadedFile = null;
		System.out.println("Azure Blob storage quick start sample");

		CloudStorageAccount storageAccount;
		CloudBlobClient blobClient = null;
		CloudBlobContainer container=null;

		try {    
			// Parse the connection string and create a blob client to interact with Blob storage
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
			blobClient = storageAccount.createCloudBlobClient();
			container = blobClient.getContainerReference("quickstartcontainer");
			

            CorsRule corsRule = new CorsRule();
            corsRule.getAllowedOrigins().add("*");
            
            Collection<CorsHttpMethods> methods = new ArrayList<CorsHttpMethods>();
            methods.add(CorsHttpMethods.GET);
            methods.add(CorsHttpMethods.PUT);
            //corsRule.getAllowedMethods().add(CorsHttpMethods.GET);
            corsRule.getAllowedMethods().addAll(methods);

            corsRule.getAllowedHeaders().add("*");
            corsRule.getExposedHeaders().add("*");
            
            ServiceProperties props = blobClient.downloadServiceProperties();
            props.getCors().getCorsRules().add(corsRule);
            blobClient.uploadServiceProperties(props);

			// Create the container if it does not exist with public access.
			System.out.println("Creating container: " + container.getName());
			container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());		    

			//Creating a sample file
//			sourceFile = File.createTempFile("sampleFile", ".txt");
//			System.out.println("Creating a sample file at: " + sourceFile.toString());
//			Writer output = new BufferedWriter(new FileWriter(sourceFile));
//			output.write("Hello Azure!");
//			output.close();
			sourceFile = new File("e:\\Maps\\Maps-China\\eb17ba24d0e85b68505820d26460ec4d.jpg");

			//Getting a blob reference
			CloudBlockBlob blob = container.getBlockBlobReference(sourceFile.getName());
			
//			HashMap<String, String> metadata = new HashMap<String, String>();
//			metadata.put("Access-Control-Allow-Origin", "*");
//			metadata.put("Access-Control-Allow-Credentials", "*");
//			metadata.put("Access-Control-Allow-Methods", "*");
//			
//			String blobMetadata = hashMapToJson(metadata);
//			HashMap<String, String> putToBlob = new HashMap<String, String>();
//			putToBlob.put("cntvblobmetadata", blobMetadata);
//			blob.setMetadata(putToBlob);

			BlobRequestOptions option = new BlobRequestOptions();
			option.setConcurrentRequestCount(5);

			//Creating blob and uploading file to it
			System.out.println("Uploading the sample file ");
			//blob.uploadFromFile(sourceFile.getAbsolutePath());
			blob.uploadFromFile(sourceFile.getAbsolutePath(), null, option, null);
			
			//Listing contents of container
			for (ListBlobItem blobItem : container.listBlobs()) {
			System.out.println("URI of blob is: " + blobItem.getUri());
		}

		// Download blob. In most cases, you would have to retrieve the reference
		// to cloudBlockBlob here. However, we created that reference earlier, and 
		// haven't changed the blob we're interested in, so we can reuse it. 
		// Here we are creating a new file to download to. Alternatively you can also pass in the path as a string into downloadToFile method: blob.downloadToFile("/path/to/new/file").
		//downloadedFile = new File(sourceFile.getParentFile(), "downloadedFile.txt");
//		downloadedFile = new File(sourceFile.getParentFile(), "downloadedFile.jpg");
//		blob.downloadToFile(downloadedFile.getAbsolutePath());
		} 
		catch (StorageException ex)
		{
			System.out.println(String.format("Error returned from the service. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
		}
		catch (Exception ex) 
		{
			System.out.println(ex.getMessage());
		}
		finally 
		{
//			System.out.println("The program has completed successfully.");
//			System.out.println("Press the 'Enter' key while in the console to delete the sample files, example container, and exit the application.");
//
//			//Pausing for input
//			Scanner sc = new Scanner(System.in);
//			sc.nextLine();
//
//			System.out.println("Deleting the container");
//			try {
//				if(container != null)
//					container.deleteIfExists();
//			} 
//			catch (StorageException ex) {
//				System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
//			}
//
//			System.out.println("Deleting the source, and downloaded files");
//
//			if(downloadedFile != null)
//				downloadedFile.deleteOnExit();
//
//			if(sourceFile != null)
//				sourceFile.deleteOnExit();
//
//			//Closing scanner
//			sc.close();
		}
	}
}
