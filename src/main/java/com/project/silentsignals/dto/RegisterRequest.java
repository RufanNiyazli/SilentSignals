package com.project.silentsignals.dto;

import com.project.silentsignals.role.RoleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Role type cannot be null")
    private RoleType roleType;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Phone number should be valid")
    private String phoneNumber;
}
