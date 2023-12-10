package com.example.videotranscoder.controller;

import com.example.videotranscoder.dto.NewVideoFileDto;
import com.example.videotranscoder.dto.VideoFileDto;
import com.example.videotranscoder.model.VideoFileModel;
import com.example.videotranscoder.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController()
@RequestMapping("/api/video-files")
public class VideoFileController {
    private VideoService videoService;

    @Autowired
    public VideoFileController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoFileDto> getVideoFile(@PathVariable("id") Long id,
                                                     @RequestHeader(value = "Authorization", required = true) String authorization) {
        VideoFileModel videoFile = videoService.getVideoFile(id, VideoService.extractToken(authorization));
        if (videoFile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(videoService.toDto(videoFile));
    }

    @PostMapping("/upload")
    public ResponseEntity<VideoFileDto> uploadVideoFile(@RequestParam("file") MultipartFile file,
                                                        @RequestHeader(value = "Authorization", required = true) String authorization) {
        String filename = file.getOriginalFilename();
        Long fileSize = file.getSize();
        NewVideoFileDto videoFileDto = new NewVideoFileDto(null, filename, fileSize, null);
        VideoFileModel videoFile =
                videoService.createVideoFile(videoFileDto, VideoService.extractToken(authorization), file);

        return ResponseEntity.ok(videoService.toDto(videoFile));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadVideoFile(@PathVariable("fileId") Long fileId,
                                                      @RequestHeader(value = "Authorization", required = true) String authorization) {
        VideoFileModel videoFile = videoService.getVideoFile(fileId, VideoService.extractToken(authorization));
        byte[] fileContent = videoService.downloadVideoFile(fileId, VideoService.extractToken(authorization));
        if (fileContent == null) {
            return ResponseEntity.notFound().build();
        }
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + videoFile.getFilename())
                .contentLength(fileContent.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
