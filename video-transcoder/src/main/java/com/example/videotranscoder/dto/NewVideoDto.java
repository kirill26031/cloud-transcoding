package com.example.videotranscoder.dto;

import com.example.videotranscoder.model.VideoFileModel;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewVideoDto {
    String name;
    Long lengthInMilliseconds;
}
