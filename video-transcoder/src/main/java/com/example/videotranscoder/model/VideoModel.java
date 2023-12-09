package com.example.videotranscoder.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Entity
@Table(name = "video")
@Getter
public class VideoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(targetEntity = UserModel.class)
    Long userId;
    @Column(name = "name")
    String name;
    @OneToMany(targetEntity = VideoFileModel.class, fetch = FetchType.LAZY, mappedBy = "videoId")
    List<VideoFileModel> files;
    @Column(name = "length")
    Long lengthInMilliseconds;
}
