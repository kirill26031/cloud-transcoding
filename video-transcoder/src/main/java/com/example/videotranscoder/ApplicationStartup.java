package com.example.videotranscoder;

import com.example.videotranscoder.service.TranscodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    TranscodingService transcodingService;
    @Autowired
    public ApplicationStartup(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        transcodingService.receiveHangingTranscodingRequests();
    }
}
