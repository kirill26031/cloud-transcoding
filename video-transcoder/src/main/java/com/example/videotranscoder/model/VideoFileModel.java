package com.example.videotranscoder.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigInteger;

@Entity
@Table(name = "video_file")
@Getter
public class VideoFileModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JoinColumn(name = "video_id", referencedColumnName = "id")
    @ManyToOne(targetEntity = VideoModel.class)
    Long videoId;
    @Column(name = "filename", nullable = false)
    String filename;
    @Column(name = "size_in_bytes", nullable = false)
    Long sizeInBytes;
    @Column(name = "storage_url", nullable = true)
    String storageUrl;
}
