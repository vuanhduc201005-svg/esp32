package com.dducwsjvbe.web_ui.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class TokenExchangeResponse {
    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("expires_in")
    Integer expiresIn;

    @JsonProperty("refresh_expires_in")
    Integer refreshExpiresIn;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("id_token")
    String idToken;

    @JsonProperty("scope")
    String scope;

    @JsonProperty("refresh_token")
    String refreshToken;

}
/*
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
chuyển accessToken thành access_token chuẩn response keycloak
 */
//exchange client token response
