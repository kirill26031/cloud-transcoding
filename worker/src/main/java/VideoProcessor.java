import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoProcessor {
    public static final String REQUESTS_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-request";
    public static final String RESPONSES_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-results";
    public static final String OUTPUT_FOLDER = "output/";

    private static ArrayList<String> processedMessageIds = new ArrayList<>();
    public static void main(String[] args) {
        try {
            String thisExecutorId = CliUtils.getExecutorId();
            System.out.println("This executor id: " + thisExecutorId);

            File outputFolder = new File(OUTPUT_FOLDER);
            if (!outputFolder.exists()) {
                outputFolder.mkdir();
            }

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
                    if (processedMessageIds.contains(message.messageId())) {
                        continue;
                    }
                    addMessageIdToProcessed(message.messageId());
                    // Process the video data here
                    String[] messageParts = message.body().split(";");
                    String storageKey = messageParts[0];
                    String options = messageParts[1];
                    String executorId = messageParts[2];
                    String fileExtension = messageParts[3];

                    if (executorId.equals(thisExecutorId)) {
                        String responseMessage = "";
//                        System.out.println("Received task " + message);
                        File downloadedVideo = storageService.downloadFile(storageKey);
//                        System.out.println("Downloaded video to " + downloadedVideo.toPath());

                        String newStorageKey = StorageService.generateRandomToken(20);
                        File outputFile = new File(outputFolder.getAbsolutePath() + "/" + newStorageKey);
                        boolean executionResult = CliUtils.executeVideoConversion(options,
                                downloadedVideo.getAbsolutePath(),
                                outputFile.getAbsolutePath());
                        if (executionResult) {
                            storageService.uploadFileToStorage(outputFile);

                            responseMessage = "SUCCESS;" + newStorageKey + ";" + outputFile.length();
//                            outputFile.delete();
                        } else {
                            responseMessage = "ERROR";
                        }

                        Map<String, MessageAttributeValue> attributes = new HashMap<>();
                        attributes.put("request_message_id",
                                MessageAttributeValue.builder()
                                        .dataType("String")
                                        .stringValue(message.messageId())
                                        .build());
                        sendResponse(sqsClient, responseMessage, attributes);

                        // Delete processed message
                        deleteProcessedMessage(sqsClient, message);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void addMessageIdToProcessed(String messageId) {
        processedMessageIds.add(messageId);
        if (processedMessageIds.size() > 1000) {
            for(int i=0; i < 100; ++i) {
                processedMessageIds.remove(0);
            }
        }
    }

    private static void sendResponse(SqsClient sqsClient, String responseMessage, Map<String, MessageAttributeValue> attributes) {
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(RESPONSES_QUEUE_URL)
                        .messageBody(responseMessage)
                        .messageAttributes(attributes)
                        .build()
        );
    }

    private static void deleteProcessedMessage(SqsClient sqsClient, Message message) {
        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(REQUESTS_QUEUE_URL)
                        .receiptHandle(message.receiptHandle())
                        .build()
        );
    }

    private static String wrapInQuotes(String text) {
        return "\"" + text + "\"";
    }
}
