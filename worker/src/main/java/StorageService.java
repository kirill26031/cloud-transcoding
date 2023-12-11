import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class StorageService {
    private final String BUCKET_NAME = "cloud-transcoding-bucket";
    private final String TEMP_FOLDER = "~/input-files/";

    private final S3Client s3Client = S3Client.builder()
            .region(Region.EU_CENTRAL_1)
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    public String uploadFileToStorage(File file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(file.getName())
                .build();

        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.fromBytes(Files.readAllBytes(file.toPath()));

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);
            return file.getName();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public File downloadFile(String storageKey) {
        File localFile = new File(TEMP_FOLDER + storageKey);
        if (localFile.exists()) {
            return localFile;
        }
        return downloadFileFromRemoteStorage(storageKey);
    }


    public File downloadFileFromRemoteStorage(String storageKey) {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(storageKey)
                .bucket(BUCKET_NAME)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        try {
            File tempFilesFolder = new File(TEMP_FOLDER);
            if (!tempFilesFolder.exists()) {
                tempFilesFolder.mkdir();
            }
            File localFile = new File(TEMP_FOLDER + storageKey);
            OutputStream os = new FileOutputStream(localFile);
            os.write(data);
            os.close();
            return localFile;
        } catch (IOException ex) {
            System.err.println("Couldn't store file " + storageKey + " to input folder");
            ex.printStackTrace();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.err.println("Couldn't store file " + storageKey + " to input folder");
        }
        return null;
    }


    public static String generateRandomToken(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        return token.substring(0, length);
    }
}
