package com.bigdata.ecom.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Please enter your name")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Please enter your email")
    @Email(message = "Please provide a valid email")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotNull(message = "Please enter your gender")
    @Column(nullable = false, length = 10)
    private String gender;

    @NotNull(message = "Please enter your password")
    @Size(min = 8, message = "Password should have at least 8 characters")
    @Column(nullable = false)
    private String password;

    @Embedded
    private Avatar avatar;

    @Column(nullable = false)
    private String role = "user";

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt = new Date();

    @Column(nullable = true)
    private String resetPasswordToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    private Date resetPasswordExpire;
}
