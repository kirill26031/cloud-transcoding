package com.example.videotranscoder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewVideoFileDto {
    Long videoId;
    String filename;
    Long sizeInBytes;
    String storageKey;
    Boolean isOriginal;
}
