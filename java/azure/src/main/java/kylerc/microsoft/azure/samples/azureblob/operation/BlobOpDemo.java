//package org.fran.cloud.azure.storage;
package kylerc.microsoft.azure.samples.azureblob.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.EnumSet;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class BlobOpDemo {

    public static final String storageConnectionString ="<Your Azure storage account connection string>";

    public static void main(String[] args){
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            CloudBlobContainer container = serviceClient.getContainerReference("myimages");
            container.createIfNotExists();
            if ( container.downloadPermissions().getPublicAccess().compareTo(BlobContainerPublicAccessType.CONTAINER) == 0 ) {
            	// this means the container is public accessible
            }
            
            //**********************************
            // get blob list with prefix
            String prefix = "DSC";
            for (ListBlobItem item : container.listBlobs(prefix, true,
                    EnumSet.allOf(BlobListingDetails.class), null, null)) {
                CloudBlockBlob blob = (CloudBlockBlob) item;
                System.out.println(blob.getName());
            }
            //**********************************

            //**********************************
            // File upload to blob
            CloudBlockBlob blob = container.getBlockBlobReference("image1.jpg");
            File sourceFile = new File("c:\\temp\\aa.jpg");
            blob.upload(
                    new FileInputStream(sourceFile),
                    sourceFile.length());
            //**********************************

            //**********************************
            // set response header for blob
            BlobProperties properties = blob.getProperties();
            properties.setContentType("image/jpg");
            properties.setCacheControl("max-age=3600");
            blob.uploadProperties();
            //**********************************

            //**********************************
            // Download the blob.
            File destinationFile = new File(sourceFile.getParentFile(), "image1Download.tmp");
            blob.downloadToFile(destinationFile.getAbsolutePath());
            //**********************************
            
            //**********************************
            // Delete the blob.
            blob.delete();
            //**********************************
        }
        catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        }
        catch (StorageException storageException) {
            storageException.printStackTrace();
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
}
