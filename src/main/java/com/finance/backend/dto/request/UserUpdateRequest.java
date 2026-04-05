package com.finance.backend.dto.request;

import com.finance.backend.enums.Role;
import com.finance.backend.enums.UserStatus;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "Email must be valid")
    private String email;

    private String fullName;

    private Role role;

    private UserStatus status;
}
