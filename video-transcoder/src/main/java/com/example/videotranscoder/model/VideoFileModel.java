package com.example.videotranscoder.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Entity
@Table(name = "video_file")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoFileModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JoinColumn(name = "video_id")
    @ManyToOne(targetEntity = VideoModel.class, fetch = FetchType.LAZY)
    VideoModel video;
    @Column(name = "filename", nullable = false)
    String filename;
    @Column(name = "size_in_bytes", nullable = false)
    Long sizeInBytes;
    @Column(name = "storage_url", nullable = true)
    String storageKey;
    @Column(name = "is_original", nullable = false)
    Boolean isOriginal;
}
