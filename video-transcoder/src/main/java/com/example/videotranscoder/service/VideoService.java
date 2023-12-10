package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.NewVideoDto;
import com.example.videotranscoder.dto.NewVideoFileDto;
import com.example.videotranscoder.dto.VideoDto;
import com.example.videotranscoder.dto.VideoFileDto;
import com.example.videotranscoder.model.UserModel;
import com.example.videotranscoder.model.VideoFileModel;
import com.example.videotranscoder.model.VideoModel;
import com.example.videotranscoder.repository.UserRepository;
import com.example.videotranscoder.repository.VideoFileRepository;
import com.example.videotranscoder.repository.VideoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {
    private VideoFileRepository videoFileRepository;
    private VideoRepository videoRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private StorageService storageService;

    @Autowired
    public VideoService(VideoFileRepository videoFileRepository,
                        VideoRepository videoRepository,
                        UserRepository userRepository,
                        ModelMapper modelMapper,
                        StorageService storageService) {
        this.videoFileRepository = videoFileRepository;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.storageService = storageService;
    }
    public VideoFileModel createVideoFile(NewVideoFileDto videoFileDto, String token, MultipartFile file) {
        UserModel user = userRepository.findByToken(token);
        if (user == null) {
            return null;
        }
        NewVideoDto videoDto = new NewVideoDto(videoFileDto.getFilename(), calculateVideoLength(file));
        VideoModel video = convertToEntity(videoDto);
        video.setUser(user);
        video = videoRepository.save(video);
        videoFileDto.setVideoId(video.getId());
        String s3Key = storageService.uploadFileToStorage(file);
        videoFileDto.setStorageUrl(s3Key);
        VideoFileModel videoFile = videoFileRepository.save(convertToEntity(videoFileDto));
        return videoFile;
    }

    private Long calculateVideoLength(MultipartFile file) {
        return Long.valueOf(0);
    }

    private VideoModel convertToEntity(NewVideoDto videoDto) {
        return modelMapper.map(videoDto, VideoModel.class);
    }

    private VideoFileModel convertToEntity(NewVideoFileDto videoFileDto) {
        return modelMapper.map(videoFileDto, VideoFileModel.class);
    }

    public VideoFileDto toDto(VideoFileModel videoFile) {
        return modelMapper.map(videoFile, VideoFileDto.class);
    }

    public VideoFileModel getVideoFile(Long id, String token) {
        UserModel user = userRepository.findByToken(token);
        if (user == null) {
            return null;
        }
        VideoFileModel videoFile = videoFileRepository.findById(id).orElse(null);
        if (videoFile == null || !videoFile.getVideo().getUser().getId().equals(user.getId())) {
            return null;
        }
        return videoFile;
    }

    public VideoModel getVideo(Long id, String token) {
        UserModel user = userRepository.findByToken(token);
        if (user == null) {
            return null;
        }
        VideoModel video = videoRepository.findById(id).orElse(null);
        if (video == null || !video.getUser().getId().equals(user.getId())) {
            return null;
        }
        return video;
    }

    public VideoDto toDto(VideoModel video) {
        return modelMapper.map(video, VideoDto.class);
    }

    public static String extractToken(String authorization) {
        return authorization.replace("Bearer ", "");
    }
}
