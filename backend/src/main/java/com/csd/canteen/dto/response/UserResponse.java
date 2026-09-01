package com.csd.canteen.dto.response;

public record UserResponse(Long id, String username, String displayName, String role, boolean enabled) {}
