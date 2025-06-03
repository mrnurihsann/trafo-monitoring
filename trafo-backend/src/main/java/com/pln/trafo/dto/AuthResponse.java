package com.pln.trafo.dto;

import com.pln.trafo.entity.User;

public class AuthResponse {
    
    public String token;
    public String tokenType = "Bearer";
    public Long expiresIn;
    public UserInfo user;

    public AuthResponse() {}

    public AuthResponse(String token, Long expiresIn, User user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = new UserInfo(user);
    }

    public static class UserInfo {
        public Long id;
        public String username;
        public String email;
        public User.UserRole role;

        public UserInfo() {}

        public UserInfo(User user) {
            this.id = user.id;
            this.username = user.username;
            this.email = user.email;
            this.role = user.role;
        }
    }
}
