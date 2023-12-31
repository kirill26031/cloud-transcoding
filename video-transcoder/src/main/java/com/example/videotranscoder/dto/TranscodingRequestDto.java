package com.example.videotranscoder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class  TranscodingRequestDto {
    String storageKey;
    String options;
    String executorId;
    String fileExtension;

    @Override
    public String toString() {
        return storageKey + ";" + options + ";" + executorId + ";" + fileExtension;
    }
}
