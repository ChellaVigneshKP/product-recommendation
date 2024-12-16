package com.bigdata.ecom.auth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDTO {
    private boolean success;
    private UserInfo user;
    private String token;
    public UserDetailsDTO(boolean success, UserInfo user) {
        this.success = success;
        this.user = user;
    }
    public UserDetailsDTO(boolean success, UserInfo user, String token) {
        this.success = success;
        this.user = user;
        this.token = token;
    }

    @Getter
    @Setter
    public static class UserInfo {
        private Avatar avatar;
        private String _id;  // Change id to _id
        private String name;
        private String email;
        private String gender;
        private String role;
        private String createdAt;
        private int __v;  // Include the version field

        public UserInfo(User user) {
            this.avatar = user.getAvatar();
            this._id = user.getId().toString();  // Set _id
            this.name = user.getName();
            this.email = user.getEmail();
            this.gender = user.getGender();
            this.role = user.getRole();
            this.createdAt = user.getCreatedAt().toString();
            this.__v = 0;  // Set version field (__v)
        }
    }
}
