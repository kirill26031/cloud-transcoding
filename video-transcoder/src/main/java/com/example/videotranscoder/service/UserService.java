package com.example.videotranscoder.service;

import com.example.videotranscoder.dto.CreateUserDto;
import com.example.videotranscoder.model.UserModel;
import com.example.videotranscoder.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    @Autowired
    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public boolean canRegisterUser(CreateUserDto createUserDto) {
        return !userRepository.existsByEmailAddress(createUserDto.getEmailAddress());
    }

    public String registerUser(CreateUserDto createUserDto) {
        if (!canRegisterUser(createUserDto)) {
            return null;
        }
        UserModel newUser = convertToEntity(createUserDto);
        newUser.setToken(generateRandomToken(128));
        userRepository.save(newUser);
        return newUser.getToken();
    }

    public static String generateRandomToken(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        return token.substring(0, length);
    }

    private UserModel convertToEntity(CreateUserDto createUserDto) {
        UserModel user = modelMapper.map(createUserDto, UserModel.class);
        return user;
    }
}
