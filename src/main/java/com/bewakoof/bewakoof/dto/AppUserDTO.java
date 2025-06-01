package com.bewakoof.bewakoof.dto;

import com.bewakoof.bewakoof.model.AppUser;
import lombok.Data;

import java.util.Date;

@Data
public class AppUserDTO {
    private Long userId;
    private String userName;
    private String email;
    private String phoneNo;

    private String gender;
    private Date birthDate;
    private Integer age;

    private String address;
    private String city;
    private Integer pinCode;

    private String profilePic;

    public AppUserDTO(AppUser user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.phoneNo = user.getPhoneNo();
        this.gender = user.getGender();
        this.birthDate = user.getBirthDate();
        this.age = user.getAge();
        this.address = user.getAddress();
        this.city = user.getCity();
        this.pinCode = user.getPinCode();
        this.profilePic = user.getProfilePic();
    }
}
