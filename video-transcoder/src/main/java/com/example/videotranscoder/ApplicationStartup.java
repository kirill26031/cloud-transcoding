package com.example.videotranscoder;

import com.example.videotranscoder.service.ExecutorsService;
import com.example.videotranscoder.service.MessageQueueService;
import com.example.videotranscoder.service.TranscodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    TranscodingService transcodingService;
    MessageQueueService messageQueueService;
    ExecutorsService executorsService;
    @Autowired
    public ApplicationStartup(TranscodingService transcodingService,
                              MessageQueueService messageQueueService,
                              ExecutorsService executorsService) {
        this.transcodingService = transcodingService;
        this.messageQueueService = messageQueueService;
        this.executorsService = executorsService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        transcodingService.receiveHangingTranscodingRequests();
        messageQueueService.sendAvailabilityRequest();
        executorsService.setExecutors(messageQueueService.gatherExecutors());
        System.out.println("Gathered " + executorsService.getExecutorIds().size() + " executors");
    }
}
