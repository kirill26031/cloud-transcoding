package com.example.videotranscoder.repository;

import com.example.videotranscoder.model.VideoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<VideoModel, Long> {
}
