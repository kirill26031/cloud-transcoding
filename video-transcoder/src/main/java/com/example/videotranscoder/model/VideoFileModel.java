package com.example.videotranscoder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Table(name = "video_file")
@Data
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
    String storageUrl;
}
