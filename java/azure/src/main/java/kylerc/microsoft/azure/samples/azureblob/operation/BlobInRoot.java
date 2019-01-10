package kylerc.microsoft.azure.samples.azureblob.operation;

import java.io.File;
import java.io.FileInputStream;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

class BlobInRoot {
	public void upload() throws Exception {
		String connectionString = "DefaultEndpointsProtocol=https;"
				+ "AccountName=storacctsuswest;AccountKey=<your storage account key>";
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
		CloudBlobClient client = storageAccount.createCloudBlobClient();

		CloudBlobContainer container = client.getContainerReference("$root");
        container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, null, null);

        CloudBlockBlob blob = container.getBlockBlobReference("image1.jpg");
        File sourceFile = new File("c:\\temp\\aa.jpg");
        blob.upload(
                new FileInputStream(sourceFile),
                sourceFile.length());
		
	}
	
	public static void main(String[] args) throws Exception {
		BlobInRoot blob = new BlobInRoot();
		blob.upload();
	}	
}
