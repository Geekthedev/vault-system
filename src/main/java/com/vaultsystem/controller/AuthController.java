package com.vaultsystem.controller;

import com.vaultsystem.dto.AuthRequestDto;
import com.vaultsystem.dto.AuthResponseDto;
import com.vaultsystem.dto.RegisterRequestDto;
import com.vaultsystem.entity.AuditLog;
import com.vaultsystem.entity.User;
import com.vaultsystem.service.AuditService;
import com.vaultsystem.service.JwtService;
import com.vaultsystem.service.UserService;
import com.vaultsystem.util.RateLimitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final RateLimitingService rateLimitingService;
    
    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                         JwtService jwtService, AuditService auditService, 
                         RateLimitingService rateLimitingService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.rateLimitingService = rateLimitingService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request,
                                                   HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        
        if (!rateLimitingService.tryConsume(ipAddress)) {
            return ResponseEntity.status(429).build();
        }
        
        User user = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        
        auditService.logAction(user, AuditLog.ActionType.LOGIN, "Auth", null, ipAddress,
                              httpRequest.getHeader("User-Agent"), "User registered and logged in");
        
        return ResponseEntity.ok(new AuthResponseDto(token, user.getUsername(), user.getRole()));
    }
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request,
                                               HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        
        if (!rateLimitingService.tryConsume(ipAddress)) {
            return ResponseEntity.status(429).build();
        }
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
            User user = userService.getUserByUsername(request.getUsername()).orElseThrow();
            String token = jwtService.generateToken(userDetails);
            
            userService.resetFailedLoginAttempts(request.getUsername());
            auditService.logAction(user, AuditLog.ActionType.LOGIN, "Auth", null, ipAddress,
                                  httpRequest.getHeader("User-Agent"), "Successful login");
            
            return ResponseEntity.ok(new AuthResponseDto(token, user.getUsername(), user.getRole()));
            
        } catch (BadCredentialsException e) {
            User user = userService.getUserByUsername(request.getUsername()).orElse(null);
            if (user != null) {
                userService.incrementFailedLoginAttempts(request.getUsername());
                auditService.logAction(user, AuditLog.ActionType.FAILED_LOGIN, "Auth", null, ipAddress,
                                      httpRequest.getHeader("User-Agent"), "Failed login attempt");
            }
            return ResponseEntity.status(401).build();
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}