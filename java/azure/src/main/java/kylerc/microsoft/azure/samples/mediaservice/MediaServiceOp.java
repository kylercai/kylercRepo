package kylerc.microsoft.azure.samples.mediaservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientSymmetricKey;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenCredentials;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenProvider;
import com.microsoft.windowsazure.services.media.authentication.AzureEnvironments;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.MediaProcessor;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;
import com.microsoft.windowsazure.services.media.models.Task;

public class MediaServiceOp
{
    // Media Services account credentials configuration
    private static String tenant = "xxx.partner.onmschina.cn";
    private static String clientId = "<your client id>";
    private static String clientKey = "<your client key>";
    private static String restApiEndpoint = "https://ckams001.restv2.chinanorth.media.chinacloudapi.cn/api/";

    // Media Services API
    private static MediaContract mediaService;

    // Encoder configuration
    // This is using the default Adaptive Streaming encoding preset. 
    // You can choose to use a custom preset, or any other sample defined preset. 
    // In addition you can use other processors, like Speech Analyzer, or Redactor if desired.
    private static String preferredEncoder = "Media Encoder Standard";
    private static String encodingPreset = "Adaptive Streaming";
    private static int MAX_ENCODED_ASSET_FILENAME_LENGTH = 32;
    
    public static void main(String[] args)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        encodingPreset = "Content Adaptive Multiple Bitrate MP4";

        String assetName = "tryVideo006";
        String encodedAssetName = String.format("%s as %s", assetName, encodingPreset);
        String uploadAssetFile = "d:\\74e36683-e375-4bb8-a26c-10b037dedb34.mp4";
        String assetFilename = "";
        AssetInfo asset = null;
        AssetInfo assetEncoded = null;
        
        try {
            // Setup Azure AD Service Principal Symmetric Key Credentials
            AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
                    tenant,
                    new AzureAdClientSymmetricKey(clientId, clientKey),
                    AzureEnvironments.AZURE_CHINA_CLOUD_ENVIRONMENT);

            AzureAdTokenProvider provider = new AzureAdTokenProvider(credentials, executorService);

            // Create a new configuration with the credentials
            Configuration configuration = MediaConfiguration.configureWithAzureAdTokenProvider(
                    new URI(restApiEndpoint),
                    provider);

            // Create the media service provisioned with the new configuration
            mediaService = MediaService.create(configuration);
            
            // create an asset to store upload media file if not exists
            asset = getAssetInfo(assetName);
            if ( asset == null ) {
            	asset = createAsset(assetName);
        		System.out.println("Create Asset Id: " + asset.getId());
            }
            
            // 1:upload & 2:get streaming locator & 3:encoding
            if ( asset != null ) {
                // upload file to an asset, and return the asset file name stored in the asset
            	assetFilename = uploadAsset(asset, uploadAssetFile);
                System.out.println("Uploaded Asset: " + assetFilename + " Id=" + asset.getId());

                // encoding&transforming asset to encoded asset
                assetEncoded = encode(asset, encodedAssetName, assetFilename);
                System.out.println("Encoded Asset Id=" + assetEncoded.getId());

                //String streamingURL = getStreamingOriginLocator(encodedAssetName);
                //String streamingURL = getStreamingOriginLocator(assetEncoded, assetFilename);
                //System.out.println("Origin Locator streaming URL: " + streamingURL);
            }
            
            // Create the Progressive Origin Locator
            ArrayList<String> progressiveURLs = getProgressiveOriginLocator(assetEncoded);
            for ( String url : progressiveURLs ) {
	            System.out.println("progressive locator url: " + url);
            }

            System.out.println("Sample completed!");
            
        } catch (ServiceException se) {
            System.out.println("ServiceException encountered.");
            System.out.println(se.toString());
        } catch (Exception e) {
            System.out.println("Exception encountered.");
            System.out.println(e.toString());
        } finally {
            executorService.shutdown();
        }
    }

    private static AssetInfo createAsset(String assetName) throws ServiceException {
        AssetInfo resultAsset;

        // Create an Asset
        resultAsset = mediaService.create(Asset.create().setName(assetName).setAlternateId("altId"));
        System.out.println("Created Asset " + assetName);

        return resultAsset;
    }

    private static String uploadAsset(AssetInfo asset, String fileName)
        throws ServiceException, FileNotFoundException, NoSuchAlgorithmException {

        WritableBlobContainerContract uploader;
        AccessPolicyInfo uploadAccessPolicy;
        LocatorInfo uploadLocator = null;

        // Create an AccessPolicy that provides Write access for 15 minutes
        uploadAccessPolicy = mediaService
            .create(AccessPolicy.create("uploadAccessPolicy", 15.0, EnumSet.of(AccessPolicyPermission.WRITE)));

        // Create a Locator using the AccessPolicy and Asset
        uploadLocator = mediaService
            .create(Locator.create(uploadAccessPolicy.getId(), asset.getId(), LocatorType.SAS));

        // Create the Blob Writer using the Locator
        uploader = mediaService.createBlobWriter(uploadLocator);

        File file = new File(fileName);

        // The local file that will be uploaded to your Media Services account
        InputStream input = new FileInputStream(file);

        System.out.println("Uploading " + fileName);

        // Upload the local file to the media asset
        String assetFilename = file.getName();
        uploader.createBlockBlob(assetFilename, input);

        // Inform Media Services about the uploaded files
        mediaService.action(AssetFile.createFileInfos(asset.getId()));
        System.out.println("Uploaded Asset File " + fileName);

        mediaService.delete(Locator.delete(uploadLocator.getId()));
        mediaService.delete(AccessPolicy.delete(uploadAccessPolicy.getId()));

        return assetFilename;
    }
    
    private static AssetInfo getAssetInfo(String assetName) throws ServiceException {
    	AssetInfo assetInfo = null;
    	ListResult<AssetInfo> assetInfos = mediaService.list(Asset.list());
    	
    	for (AssetInfo info : assetInfos) {
    		if ( assetName.equals(info.getName()) ) {
    			assetInfo = info;
    			break;
    		}
    	}
    	
    	return assetInfo;
    }
    
    // Create a Job that contains a Task to transform the Asset
    private static AssetInfo encode(AssetInfo assetToEncode, String encodedAssetName, String assetFilename)
        throws ServiceException, InterruptedException {

        // Retrieve the list of Media Processors that match the name
        ListResult<MediaProcessorInfo> mediaProcessors = mediaService
                        .list(MediaProcessor.list().set("$filter", String.format("Name eq '%s'", preferredEncoder)));

        // Use the latest version of the Media Processor
        MediaProcessorInfo mediaProcessor = null;
        for (MediaProcessorInfo info : mediaProcessors) {
            if (null == mediaProcessor || info.getVersion().compareTo(mediaProcessor.getVersion()) > 0) {
                mediaProcessor = info;
            }
        }

        System.out.println("Using Media Processor: " + mediaProcessor.getName() + " " + mediaProcessor.getVersion());

        // Create a task with the specified Media Processor
        String taskXml = "<taskBody><inputAsset>JobInputAsset(0)</inputAsset>"
                + "<outputAsset assetCreationOptions=\"0\"" // AssetCreationOptions.None
                + " assetName=\"" + encodedAssetName + "\">JobOutputAsset(0)</outputAsset></taskBody>";

        encodingPreset = "Content Adaptive Multiple Bitrate MP4";
        Task.CreateBatchOperation task = Task.create(mediaProcessor.getId(), taskXml)
                .setConfiguration(encodingPreset).setName("Encoding");

        // Create the Job; this automatically schedules and runs it.
        Job.Creator jobCreator = Job.create()
                .setName(String.format("Encoding %s to %s", assetToEncode.getName(), encodingPreset))
                .addInputMediaAsset(assetToEncode.getId()).setPriority(2).addTaskCreator(task);
        JobInfo job = mediaService.create(jobCreator);

        String jobId = job.getId();
        System.out.println("Created Job with Id: " + jobId);

        // Check to see if the Job has completed
        checkJobStatus(jobId, assetFilename);
        // Done with the Job

        // Retrieve the output Asset
        ListResult<AssetInfo> outputAssets = mediaService.list(Asset.list(job.getOutputAssetsLink()));
        return outputAssets.get(0);
    }

    public static ArrayList<String> getProgressiveOriginLocator(AssetInfo assetEncoded) throws ServiceException, IOException {
    	ArrayList<String> progressiveURLs = new ArrayList<String>();

        // Create a 30-day read only AccessPolicy
        double durationInMinutes = 60 * 24 * 30;
        AccessPolicyInfo accessPolicy = mediaService
                .create(AccessPolicy.create("progressiveAccessPolicy", durationInMinutes, EnumSet.of(AccessPolicyPermission.READ)));

        // Create a SAS Locator using the AccessPolicy and Asset
        LocatorInfo sasLocator = mediaService
                .create(Locator.create(accessPolicy.getId(), assetEncoded.getId(), LocatorType.SAS));

        // List all the Asset Files
        ListResult<AssetFileInfo> assetFiles = mediaService.list(AssetFile.list(assetEncoded.getAssetFilesLink()));

        String url = "";
        for (AssetFileInfo file : assetFiles) {
            url = sasLocator.getBaseUri() + "/" + file.getName() + sasLocator.getContentAccessToken();
            progressiveURLs.add(url);
        }
	
        return progressiveURLs;
    }
    
    public static String getStreamingOriginLocator(AssetInfo assetEncoded, String assetFilename) throws ServiceException {
        String streamingAssetFileName = "";
        int posDot = assetFilename.lastIndexOf('.');
        
        if ( posDot != -1 ) {
        	streamingAssetFileName = assetFilename.substring(0, posDot);
        } else {
        	streamingAssetFileName = assetFilename;
        }
        
        // if the asset file name length is greater than MAX_ENCODED_ASSET_FILENAME_LENGTH characters, 
        // we truncate to the first MAX_ENCODED_ASSET_FILENAME_LENGTH characters
        if ( streamingAssetFileName.length() > MAX_ENCODED_ASSET_FILENAME_LENGTH ) {
        	streamingAssetFileName = streamingAssetFileName.substring(0, MAX_ENCODED_ASSET_FILENAME_LENGTH);
        }
        streamingAssetFileName = streamingAssetFileName + ".ism";
        
        AccessPolicyInfo originAccessPolicy;
        LocatorInfo originLocator = null;

        // Create a 30-day read only AccessPolicy
        double durationInMinutes = 60 * 24 * 30;
        originAccessPolicy = mediaService.create(
                AccessPolicy.create("Streaming policy", durationInMinutes, EnumSet.of(AccessPolicyPermission.READ)));

        // Create a Locator using the AccessPolicy and Asset
        originLocator = mediaService
                .create(Locator.create(originAccessPolicy.getId(), assetEncoded.getId(), LocatorType.OnDemandOrigin));

        // Create a Smooth Streaming base URL
        return originLocator.getPath() + streamingAssetFileName + "/manifest";
    }

    private static void checkJobStatus(String jobId, String assetFilename) throws InterruptedException, ServiceException {
        boolean done = false;
        JobState jobState = null;
        String streamingURL = null;
        
        while (!done) {
            // Sleep for several seconds
            Thread.sleep(10000);
            
            JobInfo job = mediaService.get(Job.get(jobId));
            ListResult<AssetInfo> outputAssets = mediaService.list(Asset.list(job.getOutputAssetsLink()));

            if ( (streamingURL==null) && !outputAssets.isEmpty() ) {
                // Now you can create the Streaming Origin Locator
                // without waiting for the encoding process to complete
            	
            	AssetInfo encodedAsset =  outputAssets.get(0);
                streamingURL = getStreamingOriginLocator(encodedAsset, assetFilename);
                System.out.println("Origin Locator streaming URL: " + streamingURL);
            }

            // Query the updated Job state
            jobState = mediaService.get(Job.get(jobId)).getState();
            System.out.println("Job state: " + jobState);

            if (jobState == JobState.Finished || jobState == JobState.Canceled || jobState == JobState.Error) {
                done = true;
            }
        }
    }
}