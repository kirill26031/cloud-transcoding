package com.example.videotranscoder.controller;

import com.example.videotranscoder.dto.NewVideoFileDto;
import com.example.videotranscoder.dto.VideoDto;
import com.example.videotranscoder.dto.VideoFileDto;
import com.example.videotranscoder.model.VideoFileModel;
import com.example.videotranscoder.model.VideoModel;
import com.example.videotranscoder.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/api/videos")
public class VideoController {
    private VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDto> getVideo(@PathVariable("id") Long id,
                                             @RequestHeader(value = "Authorization", required = true) String authorization) {
        VideoModel video = videoService.getVideo(id, VideoService.extractToken(authorization));
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(videoService.toDto(video));
    }
}
