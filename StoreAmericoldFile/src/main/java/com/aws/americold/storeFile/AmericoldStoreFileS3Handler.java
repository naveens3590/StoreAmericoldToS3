package com.aws.americold.storeFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.aws.americold.service.CommonService;

public class AmericoldStoreFileS3Handler implements RequestHandler<Object, Object> { 

	public Object handleRequest(Object input, Context context) {
		System.out.println("Americold Order Store Data to S3 Start **************"+input.toString());
		storeDataToS3File(input);
		System.out.println("Americold Order Store Data to S3 End **************");
		return input;
	}
	public static Object storeDataToS3File(Object data) {
		try {
			List<String> americoldStringLst = new ArrayList<String>();
			data=data.toString().replace("{", "##");
			if (null!=data.toString()) {
				String[] processString=data.toString().trim().replace("{", "").replace("}", "").replace("[", "").replace("]", "").split("##");
				americoldStringLst=Arrays.asList(processString);
			}
			for (String string : americoldStringLst) {
				System.out.println(string.trim().replace("{", "").replace("}", "").replace("[", "").replace("]", "").replace(",", ""));
				
			}
			
			// credentials object identifying user for authentication
			// user must have AWSConnector and AmazonS3FullAccess for
			// this example to work
			AWSCredentials credentials = new BasicAWSCredentials(CommonConstants.ACCESS_KEY_ID,
					CommonConstants.ACCESS_SEC_KEY);

			// create a client connection based on credentials
			// AmazonS3 s3client = new AmazonS3Client(credentials);

			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_2).build();

			// create bucket - name must be unique for all S3 users
			String bucketName = CommonConstants.BUCKET_NAME;
			// s3client.createBucket(bucketName);
			// create folder into bucket
			String folderName = CommonConstants.FOLDER_NAME;
			Path tempFile = Files.createTempFile("Americold", ".txt");
			Files.write(tempFile, americoldStringLst.toString().getBytes(StandardCharsets.UTF_8));
			CommonService.createFolder(bucketName, folderName, s3client, CommonConstants.SUFFIX + tempFile.getFileName());

			String fileName = folderName + CommonConstants.SUFFIX + tempFile.getFileName().toString();
			s3client.putObject(new PutObjectRequest(bucketName, fileName, tempFile.toFile())
					.withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (Exception e) {
			e.printStackTrace();
			return "EXCEPTION";
		}
		System.out.println("Americold Order Store Data to S3 End **************");
		return data;
	}
}
