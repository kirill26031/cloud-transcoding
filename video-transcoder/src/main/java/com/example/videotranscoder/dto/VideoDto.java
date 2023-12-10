package com.example.videotranscoder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoDto extends NewVideoDto{
    Long id;

    List<VideoFileDto> files;
}
