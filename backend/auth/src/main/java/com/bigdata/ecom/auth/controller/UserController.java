package com.bigdata.ecom.auth.controller;

import com.bigdata.ecom.auth.model.*;
import com.bigdata.ecom.auth.repository.UserRepository;
import com.bigdata.ecom.auth.util.JwtUtil;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String JWT_COOKIE_NAME = "token";
    private Cloudinary cloudinary;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.cloudinary = cloudinary;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(HttpServletRequest request) {
        Cookie jwtCookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        if (jwtCookie == null) {
            UserApiResponse response = new UserApiResponse(false, "Please Login to Access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        try {
            String token = jwtCookie.getValue();
            Long userID = jwtUtil.validateToken(token);
            Optional<User> user = userRepository.findById(userID);
            if (user.isPresent()) {
                return ResponseEntity.ok(new UserDetailsDTO(true, new UserDetailsDTO.UserInfo(user.get())));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new UserApiResponse(false, "User not found"));
            }
        } catch (Exception e) {
            logger.error("Error occurred while fetching user details: {} ", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserApiResponse(false, "Error occurred while fetching user details"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UserApiResponse(false, "Please Enter Email and Password"));
        }

        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new UserApiResponse(false, "Invalid Email or Password"));
        }

        String token = jwtUtil.generateToken(user.getId());
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(1))
                .path("/")
                .build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
        UserDetailsDTO.UserInfo userInfo = new UserDetailsDTO.UserInfo(user);
        UserDetailsDTO userDTO = new UserDetailsDTO(true, userInfo, token);
        return ResponseEntity.ok().headers(responseHeaders).body(userDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .path("/")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new UserApiResponse(true, "Logged out successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam("name") String name,
                                          @RequestParam("email") String email,
                                          @RequestParam("gender") String gender,
                                          @RequestParam("password") String password,
                                          @RequestParam("avatar") MultipartFile avatarFile) {
        logger.info("Registering user with name: {}", email);
        try {
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserApiResponse(false, "Email already exists"));
            }
            Map<Object, Object> uploadResult = cloudinary.uploader().upload(avatarFile.getBytes(), ObjectUtils.asMap(
                    "folder", "avatars",
                    "width", 150,
                    "crop", "scale"
            ));

            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("secure_url");
            Avatar avatar = new Avatar();
            avatar.setPublicId(publicId);
            avatar.setUrl(url);
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setGender(gender);
            user.setPassword(passwordEncoder.encode(password));
            user.setAvatar(avatar);
            userRepository.save(user);
            String token = jwtUtil.generateToken(user.getId());
            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(Duration.ofDays(1))
                    .path("/")
                    .build();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
            UserDetailsDTO.UserInfo userInfo = new UserDetailsDTO.UserInfo(user);
            UserDetailsDTO userDTO = new UserDetailsDTO(true, userInfo, token);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(responseHeaders)
                    .body(userDTO);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UserApiResponse(false, "File upload error"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UserApiResponse(false, "Error occurred while registering user"));
        }

    }
}
