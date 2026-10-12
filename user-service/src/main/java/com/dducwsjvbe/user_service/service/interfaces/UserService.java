package com.dducwsjvbe.user_service.service.interfaces;

import com.dducwsjvbe.user_service.dto.request.LoginRequestDto;
import com.dducwsjvbe.user_service.dto.response.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequestDto request);

}
