package com.pln.trafo.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    
    @NotBlank(message = "Username tidak boleh kosong")
    public String username;
    
    @NotBlank(message = "Password tidak boleh kosong")
    public String password;

    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
