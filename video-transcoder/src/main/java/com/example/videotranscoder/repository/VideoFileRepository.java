package com.example.videotranscoder.repository;

import com.example.videotranscoder.model.VideoFileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoFileRepository extends JpaRepository<VideoFileModel, Long> {
    Optional<VideoFileModel> findByStorageKey(String originalStorageKey);
}
