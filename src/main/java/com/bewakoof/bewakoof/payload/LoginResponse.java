package com.bewakoof.bewakoof.payload;


import com.bewakoof.bewakoof.dto.AppUserDTO;
import com.bewakoof.bewakoof.model.AppUser;

public class LoginResponse {
    private String token;
    private AppUserDTO userDTO;

    public LoginResponse(String token, AppUser user) {
        this.token = token;
        this.userDTO = new AppUserDTO(user);
    }

    public String getToken() {
        return token;
    }

    public AppUserDTO getUser() {
        return userDTO;
    }
}
