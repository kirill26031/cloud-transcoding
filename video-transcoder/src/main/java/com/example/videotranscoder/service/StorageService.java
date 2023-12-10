package com.example.videotranscoder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

@Service
public class StorageService {

    private final S3Client s3Client;
    private final String bucketName = "cloud-transcoding-bucket";

    @Autowired
    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFileToStorage(MultipartFile file) {
        String generatedKey = UserService.generateRandomToken(128);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(generatedKey)
                .build();

        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.fromBytes(file.getBytes());

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);

            // Handle the response if needed
            System.out.println("File uploaded successfully. ETag: " + putObjectResponse.eTag());
            return generatedKey;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
