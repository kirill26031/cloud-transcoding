import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.security.Timestamp;
import java.util.*;

public class SqsMessageReceiverThread extends Thread {

    private final String AVAILABILITY_REQUESTS = "https://sqs.eu-central-1.amazonaws.com/070541150151/Request-worker-availibility-report";
    private final String AVAILABILITY_RESPONSES = "https://sqs.eu-central-1.amazonaws.com/070541150151/Worker-availability-report";

    public SqsMessageReceiverThread() {
    }

    HashSet<String> receivedMessageIds = new HashSet<>();

    @Override
    public void run() {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        while (!Thread.interrupted()) {
            ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
                    ReceiveMessageRequest.builder()
                            .queueUrl(AVAILABILITY_REQUESTS)
                            .attributeNames(QueueAttributeName.ALL)
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(20)
                            .build()
            );
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {

            }

            for (Message message : receiveMessageResponse.messages()) {
                if (!receivedMessageIds.contains(message.messageId())) {
                    receivedMessageIds.add(message.messageId());
                    sendResponseWithIdentifier(sqsClient);
                }
                else {
                    String timestamp = message.attributes().get(MessageSystemAttributeName.SENT_TIMESTAMP);
                    Date sentTime = new Date(Long.parseLong(timestamp));
                    if ((new Date()).getTime() - sentTime.getTime() > 1000 * 60 * 3) {
                        // Delete message after 3 minutes
                        deleteMessage(sqsClient, message);
                    }
                }
            }
        }
    }

    private void sendResponseWithIdentifier(SqsClient sqsClient) {
        String thisExecutorId = CliUtils.getExecutorId();

        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put("executor_id",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(thisExecutorId)
                        .build());
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(AVAILABILITY_RESPONSES)
                        .messageBody("I'm alive!")
                        .messageAttributes(attributes)
                        .build()
        );
    }

    private void deleteMessage(SqsClient sqsClient, Message message) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(AVAILABILITY_REQUESTS)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
        catch (Exception e) {
            System.err.println("Couldn't delete message in AVAILABILITY_REQUESTS queue");
        }
    }
}
