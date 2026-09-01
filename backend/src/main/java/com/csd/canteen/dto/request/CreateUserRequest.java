package com.csd.canteen.dto.request;

import com.csd.canteen.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
        @NotBlank(message = "displayName is required") String displayName,
        @NotNull(message = "role is required") Role role
) {}
