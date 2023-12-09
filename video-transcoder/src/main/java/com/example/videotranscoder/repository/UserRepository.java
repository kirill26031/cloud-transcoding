package com.example.videotranscoder.repository;

import com.example.videotranscoder.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

    boolean existsByEmailAddress(String emailAddress);
}
