package com.example.videotranscoder.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VideoFileDto extends NewVideoFileDto {
    Long id;
    public VideoFileDto(Long id, Long videoId, String filename, Long sizeInBytes, String storageUrl) {
        super(videoId, filename, sizeInBytes, storageUrl);
        this.id = id;
    }
}
