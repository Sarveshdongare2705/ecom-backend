package com.bewakoof.bewakoof.service;

import com.bewakoof.bewakoof.model.AppUser;
import com.bewakoof.bewakoof.payload.LoginResponse;
import com.bewakoof.bewakoof.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class AppUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtservice;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private Cloudinary cloudinary;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AppUser register(AppUser appUser) {
        appUser.setPassword(encoder.encode(appUser.getPassword()));
        return userRepository.save(appUser);
    }

    public LoginResponse authenticate(AppUser user) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        if(auth.isAuthenticated()) {
            String jwtToken = jwtservice.generateToken(user.getEmail());
            AppUser savedUser = getUser(user.getEmail());
            return new LoginResponse(
                    jwtToken,
                    savedUser
            );
        }
        else {
            return new LoginResponse("Invalid email or password",null);
        }
    }

    private AppUser getUser(String email){
        return userRepository.findByEmail(email);
    }


    //update profile
    public AppUser updateUserData(String email, AppUser appUser) {
        AppUser savedUser = getUser(email);
        if(savedUser == null){
            return null;
        }
        savedUser.setUserName(appUser.getUserName());
        savedUser.setPhoneNo(appUser.getPhoneNo());
        savedUser.setGender(appUser.getGender());
        savedUser.setBirthDate(appUser.getBirthDate());
        savedUser.setAddress(appUser.getAddress());
        savedUser.setCity(appUser.getCity());
        savedUser.setPinCode(appUser.getPinCode());

        return userRepository.save(savedUser);
    }

    //update image
    public AppUser updateProfilePic(String email, MultipartFile profilePic) throws IOException {
        AppUser savedUser = getUser(email);
        if(savedUser == null){return null;}
        Map uploadResult = cloudinary.uploader().upload(profilePic.getBytes() , ObjectUtils.emptyMap());
        String imageUrl = (String) uploadResult.get("url");
        savedUser.setProfilePic(imageUrl);
        return userRepository.save(savedUser);
    }
}
