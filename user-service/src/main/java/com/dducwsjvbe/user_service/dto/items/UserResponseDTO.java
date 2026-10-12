package com.dducwsjvbe.user_service.dto.items;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String name;
}
