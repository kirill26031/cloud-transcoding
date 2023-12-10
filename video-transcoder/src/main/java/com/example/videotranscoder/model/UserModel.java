package com.example.videotranscoder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Entity
@Table(name = "transcoder_user")
@Data
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "email_address", nullable = false)
    String emailAddress;
    @Column(name = "token", nullable = true)
    String token;
    @OneToMany(targetEntity = VideoModel.class, mappedBy = "user", fetch = FetchType.LAZY)
    List<VideoModel> videos;
}
