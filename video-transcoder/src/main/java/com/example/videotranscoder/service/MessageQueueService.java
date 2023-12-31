package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.TranscodingRequestDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.security.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
public class MessageQueueService {
    private final String REQUESTS_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-request";
    private final String RESULTS_QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-results";

    private final String AVAILABILITY_REQUESTS = "https://sqs.eu-central-1.amazonaws.com/070541150151/Request-worker-availibility-report";
    private final String AVAILABILITY_RESPONSES = "https://sqs.eu-central-1.amazonaws.com/070541150151/Worker-availability-report";

    private final Long MAX_EXPECTED_AMOUNT_OF_EXECUTERS = Long.valueOf(4);
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
                    deleteReceivedMessage(message, RESULTS_QUEUE_URL);
                    return result;
                }
            }
        }
        System.err.println("Transcoding didn't finish in " + (maxWaitIterations * 20) + " seconds");
        return result;
    }

    public Collection<String> receiveHangingTranscodingResults() {
        int maxWaitIterations = 10;
        int waitIterationsCounter = 0;

        HashMap<String, String> messages = new HashMap<>();

        for (;waitIterationsCounter < maxWaitIterations; ++waitIterationsCounter) {
            ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                    ReceiveMessageRequest.builder()
                            .queueUrl(RESULTS_QUEUE_URL)
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(20)
                            .messageAttributeNames("request_message_id")
                            .build()
            );

            if (!receiveMessageResponse.hasMessages()) {
                break;
            }

            for (Message message : receiveMessageResponse.messages()) {
                String incomingMessageId = message.messageAttributes().get("request_message_id").stringValue();
                messages.put(incomingMessageId, message.body());
                try {
                    deleteReceivedMessage(message, RESULTS_QUEUE_URL);
                } catch (Exception e) {
                    System.err.println("Tried to delete hanging message " + message.messageId());
                }
            }
        }

        return messages.values();
    }

    private void deleteReceivedMessage(Message message, String queue) {
        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(queue)
                        .receiptHandle(message.receiptHandle())
                        .build()
        );
    }

    public void sendAvailabilityRequest() {
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(AVAILABILITY_REQUESTS)
                .messageBody("Anyone alive, please, respond.")
                .build();

        SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
    }

    public List<String> gatherExecutors() {
        int maxWaitIterations = 6;
        int waitIterationsCounter = 0;
        HashSet<String> executors = new HashSet<>();

        for (;waitIterationsCounter < maxWaitIterations && executors.size() < MAX_EXPECTED_AMOUNT_OF_EXECUTERS; ++waitIterationsCounter) {
            ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                    ReceiveMessageRequest.builder()
                            .queueUrl(AVAILABILITY_RESPONSES)
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(20)
                            .attributeNames(QueueAttributeName.ALL)
                            .messageAttributeNames("executor_id")
                            .build()
            );


            for (Message message : receiveMessageResponse.messages()) {
                String executorId = message.messageAttributes().get("executor_id").stringValue();
                if (!executors.contains(executorId)) {
                    String timestamp = message.attributes().get(MessageSystemAttributeName.SENT_TIMESTAMP);
                    Date sentTime = new Date(Long.parseLong(timestamp));
                    if ((new Date()).getTime() - sentTime.getTime() <= 1000 * 60 * 60) {
                        // Check that response is not older than 1 hour
                        executors.add(executorId);
                    }
                }

                try {
                    deleteReceivedMessage(message, AVAILABILITY_RESPONSES);
                } catch (Exception e) {
                    System.err.println("Error while deleting message in availability report responses queue");
                }
            }
        }
        return executors.stream().toList();
    }
}
