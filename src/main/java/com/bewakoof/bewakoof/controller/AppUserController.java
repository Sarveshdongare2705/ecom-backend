package com.bewakoof.bewakoof.controller;

import com.bewakoof.bewakoof.model.AppUser;
import com.bewakoof.bewakoof.payload.LoginResponse;
import com.bewakoof.bewakoof.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
public class AppUserController {

    @Autowired
    private AppUserService appUserService;


    @PostMapping("/user/register")
    public AppUser register(@RequestBody AppUser appUser) {
        return appUserService.register(appUser);
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestBody AppUser appUser) {
        LoginResponse response = appUserService.authenticate(appUser);

        if (response.getUser() == null) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update-profile")
    public AppUser updateProfile(@AuthenticationPrincipal UserDetails userDetails ,  @RequestBody AppUser updateData) {
        String email = userDetails.getUsername();
        AppUser result = appUserService.updateUserData(email, updateData);
        if(result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return result;
    }

    @PutMapping("/update-profilePic")
    public AppUser updateProfilePic(@AuthenticationPrincipal UserDetails userDetails ,  @RequestPart MultipartFile file) throws IOException {
        String email = userDetails.getUsername();
        AppUser result = appUserService.updateProfilePic(email , file);
        if(result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return result;
    }



}
