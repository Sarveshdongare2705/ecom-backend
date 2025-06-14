package com.bewakoof.bewakoof.model;

import com.bewakoof.bewakoof.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private UserRole role;


    @Column(nullable = false)
    private String userName;

    @Column(unique=true , nullable=false)
    private String email;

    @Column(nullable=false)
    private String password;

    @Column(unique=true , nullable=false)
    private String phoneNo;

    private String gender;
    private Date birthDate;
    private Integer age;

    private String address;
    private String city;
    private Integer pinCode;


    private String profilePic;

    public AppUser() {}
}
