package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.TranscodingRequestDto;
import com.example.videotranscoder.model.VideoFileModel;
import com.example.videotranscoder.model.VideoModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        String processedTranscodingOptions = processOptions(transcodingOptions, originalVideoFile.getFilename());
        String executor = executorsService.getAvailableExecutor();
        String messageId = messageQueueService.sendTranscodingRequest(new TranscodingRequestDto(
                originalVideoFile.getStorageKey(),
                processedTranscodingOptions,
                executor,
                getFileExtension(originalVideoFile.getFilename())
        ));
        String result = messageQueueService.receiveResults(messageId);
        executorsService.decrementTaskAmountForExecutor(executor);
        if (result.startsWith("SUCCESS")) {
            String[] parts = result.replace("SUCCESS;", "").split(";");
            VideoFileModel videoFile = videoService.createTranscodedFile(parts[0], originalVideoFile.getVideo(),
                    transcodingOptions, getFileExtension(originalVideoFile.getFilename()), Long.parseLong(parts[1]));
            return videoFile;
        }
        else {
            return null;
        }
    }

    public void receiveHangingTranscodingRequests() {
        Collection<String> transcodingResults = messageQueueService.receiveHangingTranscodingResults();
        for (String result : transcodingResults) {
            if (result.startsWith("SUCCESS")) {
                String[] parts = result.replace("SUCCESS;", "").split(";");
                Long sizeInBytes = Long.parseLong(parts[1]);
                String newStorageKey = parts[0];
                String originalStorageKey = parts[2];
                String executorId = parts[3];
                this.executorsService.decrementTaskAmountForExecutor(executorId);
                VideoFileModel originalVideoFile = this.videoService.getVideoFileByStorageKey(originalStorageKey);
                if (originalVideoFile != null) {
                    videoService.createTranscodedFile(newStorageKey, originalVideoFile.getVideo(),
                            "unknown", getFileExtension(originalVideoFile.getFilename()), sizeInBytes);
                    System.out.println("Processed hanging transcoding result");
                }
            } else {
                String[] parts = result.replace("ERROR;", "").split(";");
                String executorId = parts[0];
                this.executorsService.decrementTaskAmountForExecutor(executorId);
            }
        }
    }

    public String processOptions(String options, String filename) {
        return "-i {input} -y -vf scale=" + options + " {output}";
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public List<String> SCALE_OPTIONS = List.of(
            "3840:2160",
            "2560:1440",
            "1920:1080",
            "1280:720",
            "720:480",
            "720:576",
            "854:480",
            "640:360",
            "426:240"
    );
}
