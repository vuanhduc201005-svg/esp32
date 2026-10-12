package com.dducwsjvbe.user_service.dto.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserTokenExchangeParam  {
    String grant_type;
    String client_id;
    String client_secret;
    String scope;
    String username;
    String password;
    String refresh_token;
}
/*
@FieldDefaults(level = AccessLevel.PRIVATE) định nghĩa full field là private
 */