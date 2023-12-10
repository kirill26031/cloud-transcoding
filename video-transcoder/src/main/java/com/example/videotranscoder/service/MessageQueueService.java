package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.TranscodingRequestDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class MessageQueueService {
    private final String QUEUE_URL = "https://sqs.eu-central-1.amazonaws.com/070541150151/transcoding-request";
    SqsClient sqsClient;
    public MessageQueueService() {
         sqsClient = SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
    public void sendTranscodingRequest(TranscodingRequestDto transcodingRequestDto) {
        String message = transcodingRequestDto.toString();

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .messageBody(message)
                .build();

        SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);

//        System.out.println("Message sent. MessageId: " + sendMessageResponse.messageId());
    }
}
