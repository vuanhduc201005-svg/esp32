package com.dducwsjvbe.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequestDto {
    @NotBlank(message = "username not null")
    String username;
    @NotBlank(message = "password not null")
    String password;
}
