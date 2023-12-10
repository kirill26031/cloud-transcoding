import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.io.File;

public class VideoProcessor {
    public static final String REQUESTS_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-request";
    public static final String RESPONSES_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-results";
    public static void main(String[] args) {
        try {
            String thisExecutorId = CliUtils.getExecutorId();
            System.out.println("This executor id: " + thisExecutorId);

            StorageService storageService = new StorageService();

            SqsClient sqsClient = SqsClient.builder()
                    .region(Region.EU_CENTRAL_1)
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();

            while (true) {
                // Receive messages from the input queue
                ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(REQUESTS_QUEUE_URL)
                                .maxNumberOfMessages(1)
                                .waitTimeSeconds(20)
                                .build()
                );

                for (Message message : receiveMessageResponse.messages()) {
                    // Process the video data here
                    String[] messageParts = message.body().split(";");
                    String storageKey = messageParts[0];
                    String options = messageParts[1];
                    String executorId = messageParts[2];

                    System.out.println("Received task " + message);

                    if (executorId.equals(thisExecutorId)) {
                        File downloadedVideo = storageService.downloadFile(storageKey);
                        System.out.println("Downloaded video to " + downloadedVideo.toPath());
                    }
                }
            }
            String thisExecutorId = CliUtils.getExecutorId();
            System.out.println("This executor id: " + thisExecutorId);

            StorageService storageService = new StorageService();

            SqsClient sqsClient = SqsClient.builder()
                    .region(Region.EU_CENTRAL_1)
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();

            while (true) {
                // Receive messages from the input queue
                ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(REQUESTS_QUEUE_URL)
                                .maxNumberOfMessages(1)
                                .waitTimeSeconds(20)
                                .build()
                );

                for (Message message : receiveMessageResponse.messages()) {
                    // Process the video data here
                    String[] messageParts = message.body().split(";");
                    String storageKey = messageParts[0];
                    String options = messageParts[1];
                    String executorId = messageParts[2];

                    System.out.println("Received task " + message);

                    if (executorId.equals(thisExecutorId)) {
                        File downloadedVideo = storageService.downloadFile(storageKey);
                        System.out.println("Downloaded video to " + downloadedVideo.toPath());
                    }
                }
            }
            System.exit(0);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
