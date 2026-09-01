package com.csd.canteen.service;

import com.csd.canteen.dto.request.CreateUserRequest;
import com.csd.canteen.dto.request.LoginRequest;
import com.csd.canteen.dto.response.LoginResponse;
import com.csd.canteen.dto.response.UserResponse;
import com.csd.canteen.entity.User;
import com.csd.canteen.exception.ConflictException;
import com.csd.canteen.repository.UserRepository;
import com.csd.canteen.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration-minutes:480}")
    private long expirationMinutes;

    public LoginResponse login(LoginRequest request) {
    	
//    	User debugUser = userRepository.findByUsername(request.username()).orElse(null);
//    	System.out.println(debugUser);
//    	if (debugUser != null) {
//    	    boolean matches = passwordEncoder.matches(request.password(), debugUser.getPasswordHash());
//    	    System.out.println("DEBUG - Does raw password match DB hash? " + matches);
//    	}
    	
    	
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
        	//ex.printStackTrace(); 
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getUsername(), user.getDisplayName(), user.getRole().name(), expirationMinutes * 60);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole().name(), u.isEnabled()))
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("A user with username '" + request.username() + "' already exists");
        }
        User user = User.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(request.role())
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getDisplayName(), saved.getRole().name(), saved.isEnabled());
    }
}
