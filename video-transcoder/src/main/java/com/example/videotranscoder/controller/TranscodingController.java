package com.example.videotranscoder.controller;

import com.example.videotranscoder.dto.VideoFileDto;
import com.example.videotranscoder.model.VideoFileModel;
import com.example.videotranscoder.service.MessageQueueService;
import com.example.videotranscoder.service.TranscodingService;
import com.example.videotranscoder.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("api/transcode")
public class TranscodingController {
    private VideoService videoService;
    private TranscodingService transcodingService;
    @Autowired
    public TranscodingController(VideoService videoService,
                                 TranscodingService transcodingService){
        this.videoService = videoService;
        this.transcodingService = transcodingService;
    }

    @PostMapping("/transcode/{video_id}")
    public ResponseEntity<VideoFileDto> transcodeVideo(@PathVariable("video_id") Long videoId,
                                                       @RequestHeader(value = "Authorization", required = true) String authorization) {
        String token = VideoService.extractToken(authorization);
        if (!videoService.ownsVideo(token, videoId)){
            return ResponseEntity.status(403).build();
        }
        VideoFileModel transcodedFile = transcodingService.transcodeVideo(videoId, "options");
        if (transcodedFile == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(videoService.toDto(transcodedFile));
    }
}
