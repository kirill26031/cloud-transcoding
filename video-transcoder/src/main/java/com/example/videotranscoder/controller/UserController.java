package com.example.videotranscoder.controller;

import com.example.videotranscoder.dto.CreateUserDto;
import com.example.videotranscoder.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody CreateUserDto userDto) {
        String token = userService.registerUser(userDto);
        if (token == null) {
            return ResponseEntity.badRequest().body("Couldn't create new user");
        }
        return ResponseEntity.ok(token);
    }
}