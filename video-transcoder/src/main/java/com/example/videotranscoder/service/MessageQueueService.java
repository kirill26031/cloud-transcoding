package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.TranscodingRequestDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service
public class MessageQueueService {
    private final String REQUESTS_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-request";
    private final String RESULTS_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-results";
    SqsClient sqsClient;
    public MessageQueueService() {
         sqsClient = SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
    public String sendTranscodingRequest(TranscodingRequestDto transcodingRequestDto) {
        String message = transcodingRequestDto.toString();

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(REQUESTS_QUEUE_URL)
                .messageBody(message)
                .build();

        SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);

        return sendMessageResponse.messageId();
    }

    public String receiveResults(String messageId) {
        int maxWaitIterations = 20;
        int waitIterationsCounter = 0;
        String result = "ERROR";
        for (;waitIterationsCounter < maxWaitIterations; ++waitIterationsCounter) {
            ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                    ReceiveMessageRequest.builder()
                            .queueUrl(RESULTS_QUEUE_URL)
                            .maxNumberOfMessages(1)
                            .waitTimeSeconds(20)
                            .messageAttributeNames("request_message_id")
                            .build()
            );

            for (Message message : receiveMessageResponse.messages()) {
                String incomingMessageId = message.messageAttributes().get("request_message_id").stringValue();
                if (incomingMessageId.equals(messageId)) {
                    result = message.body();
                    deleteReceivedMessage(message);
                    return result;
                }
            }
        }
        System.err.println("Transcoding didn't finish in " + (maxWaitIterations * 20) + " seconds");
        return result;
    }

    public Collection<String> receiveHangingTranscodingResults() {
        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(RESULTS_QUEUE_URL)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(20)
                        .messageAttributeNames("request_message_id")
                        .build()
        );

        HashMap<String, String> messages = new HashMap<>();

        for (Message message : receiveMessageResponse.messages()) {
            String incomingMessageId = message.messageAttributes().get("request_message_id").stringValue();
            messages.put(incomingMessageId, message.body());
            try {
                deleteReceivedMessage(message);
            } catch (Exception e) {
                System.err.println("Tried to delete hanging message " + message.messageId());
            }
        }

        return messages.values();
    }

    private void deleteReceivedMessage(Message message) {
        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(RESULTS_QUEUE_URL)
                        .receiptHandle(message.receiptHandle())
                        .build()
        );
    }
}
