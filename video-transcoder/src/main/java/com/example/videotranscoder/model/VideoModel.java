package com.example.videotranscoder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Entity
@Table(name = "video")
@Data
public class VideoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(targetEntity = UserModel.class, fetch = FetchType.LAZY)
    UserModel user;
    @Column(name = "name")
    String name;
    @OneToMany(targetEntity = VideoFileModel.class, fetch = FetchType.LAZY, mappedBy = "video")
    List<VideoFileModel> files;
    @Column(name = "length")
    Long lengthInMilliseconds;
}
