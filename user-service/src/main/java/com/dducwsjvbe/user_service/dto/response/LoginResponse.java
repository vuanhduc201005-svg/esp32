package com.dducwsjvbe.user_service.dto.response;

import com.dducwsjvbe.user_service.dto.identity.TokenExchangeResponse;
import com.dducwsjvbe.user_service.dto.identity.UserInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    TokenExchangeResponse token;
    UserInfo user;
}
