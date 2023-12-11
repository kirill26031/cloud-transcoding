package com.example.videotranscoder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

@Service
public class StorageService {

    private static final String TEMP_FOLDER = "temp-files/";
    private final S3Client s3Client;
    private final String BUCKET_NAME = "cloud-transcoding-bucket";

    @Autowired
    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFileToStorage(MultipartFile file) {
        String generatedKey = UserService.generateRandomToken(20);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(generatedKey)
                .build();

        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.fromBytes(file.getBytes());

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);

//            // Handle the response if needed
//            System.out.println("File uploaded successfully. ETag: " + putObjectResponse.eTag());
            return generatedKey;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public byte[] downloadFile(String storageKey) {
        File localFile = new File(TEMP_FOLDER + storageKey);
        if (localFile.exists()) {
            try {
                return Files.readAllBytes(localFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return downloadFileFromRemoteStorage(storageKey);
    }

    private byte[] downloadFileFromRemoteStorage(String storageKey) {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(storageKey)
                .bucket(BUCKET_NAME)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        try {
            if (shouldStoreFileToCache(storageKey, data.length)) {
                File tempFilesFolder = new File(TEMP_FOLDER);
                if (!tempFilesFolder.exists()) {
                    tempFilesFolder.mkdir();
                }
                File localFile = new File(TEMP_FOLDER + storageKey);
                OutputStream os = new FileOutputStream(localFile);
                os.write(data);
                os.close();
                cleanCacheIfNecessary();
            }
        } catch (IOException ex) {
            System.err.println("Couldn't store file " + storageKey + " to cache");
            ex.printStackTrace();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.err.println("Couldn't store file " + storageKey + " to cache");
        }
        return data;
    }

    private void cleanCacheIfNecessary() {

    }

    private boolean shouldStoreFileToCache(String storageKey, long fileSize) {
        return fileSize < 100_000_000;
    }
}
