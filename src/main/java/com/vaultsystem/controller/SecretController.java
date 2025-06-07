package com.vaultsystem.controller;

import com.vaultsystem.dto.SecretCreateDto;
import com.vaultsystem.dto.SecretDto;
import com.vaultsystem.dto.SecretUpdateDto;
import com.vaultsystem.entity.Secret;
import com.vaultsystem.service.SecretService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/secrets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Secrets", description = "Secret management endpoints")
public class SecretController {
    
    private final SecretService secretService;
    
    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new secret")
    public ResponseEntity<SecretDto> createSecret(@Valid @RequestBody SecretCreateDto createDto,
                                                 Authentication authentication,
                                                 HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        SecretDto secret = secretService.createSecret(createDto, authentication.getName(), ipAddress);
        return ResponseEntity.ok(secret);
    }
    
    @GetMapping
    @Operation(summary = "Get all secrets for the authenticated user")
    public ResponseEntity<Page<SecretDto>> getSecrets(
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Secret.SecretType type,
            @RequestParam(required = false) String search,
            @Parameter(hidden = true) Pageable pageable,
            Authentication authentication) {
        
        Page<SecretDto> secrets = secretService.getSecrets(
            authentication.getName(), projectName, type, search, pageable);
        return ResponseEntity.ok(secrets);
    }
    
    @GetMapping("/{secretId}")
    @Operation(summary = "Get a specific secret")
    public ResponseEntity<SecretDto> getSecret(@PathVariable Long secretId,
                                              @RequestParam(defaultValue = "false") boolean includeValue,
                                              Authentication authentication,
                                              HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        SecretDto secret = secretService.getSecret(secretId, authentication.getName(), 
                                                  ipAddress, includeValue);
        return ResponseEntity.ok(secret);
    }
    
    @PutMapping("/{secretId}")
    @Operation(summary = "Update a secret")
    public ResponseEntity<SecretDto> updateSecret(@PathVariable Long secretId,
                                                 @Valid @RequestBody SecretUpdateDto updateDto,
                                                 Authentication authentication,
                                                 HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        SecretDto secret = secretService.updateSecret(secretId, updateDto, 
                                                     authentication.getName(), ipAddress);
        return ResponseEntity.ok(secret);
    }
    
    @DeleteMapping("/{secretId}")
    @Operation(summary = "Delete a secret")
    public ResponseEntity<Void> deleteSecret(@PathVariable Long secretId,
                                            Authentication authentication,
                                            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        secretService.deleteSecret(secretId, authentication.getName(), ipAddress);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/projects")
    @Operation(summary = "Get all project names for the authenticated user")
    public ResponseEntity<List<String>> getProjectNames(Authentication authentication) {
        List<String> projectNames = secretService.getProjectNames(authentication.getName());
        return ResponseEntity.ok(projectNames);
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