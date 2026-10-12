package com.dducwsjvbe.web_ui.dto;


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
