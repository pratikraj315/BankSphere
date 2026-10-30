package com.project.banksphere.service;

import com.project.banksphere.dto.request.LoginRequest;
import com.project.banksphere.dto.request.RegisterRequest;
import com.project.banksphere.dto.response.LoginResponse;
import com.project.banksphere.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}