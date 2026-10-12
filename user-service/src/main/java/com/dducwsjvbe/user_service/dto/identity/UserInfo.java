package com.dducwsjvbe.user_service.dto.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfo {
    String username;
    List<String> roles;
}
