package com.dducwsjvbe.user_service.dto.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Credential {
    String type;
    String value;
    boolean temporary;
}
/*
body create user
 */