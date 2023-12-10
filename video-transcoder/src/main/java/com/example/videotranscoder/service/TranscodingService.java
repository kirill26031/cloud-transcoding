package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.TranscodingRequestDto;
import com.example.videotranscoder.model.VideoFileModel;
import org.springframework.stereotype.Service;

@Service
public class TranscodingService {
    private MessageQueueService messageQueueService;
    private VideoService videoService;
    private ExecutorsService executorsService;

    public TranscodingService(MessageQueueService messageQueueService,
                              VideoService videoService,
                              ExecutorsService executorsService) {
        this.messageQueueService = messageQueueService;
        this.videoService = videoService;
        this.executorsService = executorsService;
    }

    public VideoFileModel transcodeVideo(Long videoId, String transcodingOptions) {
        VideoFileModel originalVideoFile = videoService.getVideo(videoId)
                .getFiles().stream().filter(VideoFileModel::getIsOriginal).findFirst().orElse(null);
        if (originalVideoFile == null) {
            System.err.println("Couldn't find original file! videoId = " + videoId);
            return null;
        }
        messageQueueService.sendTranscodingRequest(new TranscodingRequestDto(
                originalVideoFile.getStorageKey(),
                transcodingOptions,
                executorsService.getAvailableExecutor()
        ));
        return null;
    }
}
