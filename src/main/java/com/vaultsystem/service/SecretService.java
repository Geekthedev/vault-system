package com.vaultsystem.service;

import com.vaultsystem.dto.SecretCreateDto;
import com.vaultsystem.dto.SecretDto;
import com.vaultsystem.dto.SecretUpdateDto;
import com.vaultsystem.entity.Secret;
import com.vaultsystem.entity.User;
import com.vaultsystem.repository.SecretRepository;
import com.vaultsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SecretService {
    
    private final SecretRepository secretRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final AuditService auditService;
    
    public SecretService(SecretRepository secretRepository, UserRepository userRepository,
                        EncryptionService encryptionService, AuditService auditService) {
        this.secretRepository = secretRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.auditService = auditService;
    }
    
    @Transactional
    public SecretDto createSecret(SecretCreateDto createDto, String username, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String encryptedValue = encryptionService.encrypt(createDto.getValue());
        
        Secret secret = new Secret();
        secret.setName(createDto.getName());
        secret.setDescription(createDto.getDescription());
        secret.setEncryptedValue(encryptedValue);
        secret.setType(createDto.getType());
        secret.setProjectName(createDto.getProjectName());
        secret.setEnvironment(createDto.getEnvironment());
        secret.setTags(createDto.getTags());
        secret.setUser(user);
        
        Secret savedSecret = secretRepository.save(secret);
        
        auditService.logAction(user, AuditLog.ActionType.CREATE_SECRET, 
                              "Secret", savedSecret.getId(), ipAddress);
        
        return convertToDto(savedSecret, false);
    }
    
    @Transactional
    public SecretDto getSecret(Long secretId, String username, String ipAddress, boolean includeValue) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Secret secret = secretRepository.findByIdAndUser(secretId, user)
                .orElseThrow(() -> new RuntimeException("Secret not found"));
        
        if (includeValue) {
            secret.setLastAccessedAt(LocalDateTime.now());
            secretRepository.save(secret);
            
            auditService.logAction(user, AuditLog.ActionType.READ_SECRET, 
                                  "Secret", secret.getId(), ipAddress);
        }
        
        return convertToDto(secret, includeValue);
    }
    
    @Transactional
    public SecretDto updateSecret(Long secretId, SecretUpdateDto updateDto, String username, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Secret secret = secretRepository.findByIdAndUser(secretId, user)
                .orElseThrow(() -> new RuntimeException("Secret not found"));
        
        if (updateDto.getName() != null) {
            secret.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            secret.setDescription(updateDto.getDescription());
        }
        if (updateDto.getValue() != null) {
            secret.setEncryptedValue(encryptionService.encrypt(updateDto.getValue()));
        }
        if (updateDto.getType() != null) {
            secret.setType(updateDto.getType());
        }
        if (updateDto.getProjectName() != null) {
            secret.setProjectName(updateDto.getProjectName());
        }
        if (updateDto.getEnvironment() != null) {
            secret.setEnvironment(updateDto.getEnvironment());
        }
        if (updateDto.getTags() != null) {
            secret.setTags(updateDto.getTags());
        }
        
        Secret updatedSecret = secretRepository.save(secret);
        
        auditService.logAction(user, AuditLog.ActionType.UPDATE_SECRET, 
                              "Secret", secret.getId(), ipAddress);
        
        return convertToDto(updatedSecret, false);
    }
    
    @Transactional
    public void deleteSecret(Long secretId, String username, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Secret secret = secretRepository.findByIdAndUser(secretId, user)
                .orElseThrow(() -> new RuntimeException("Secret not found"));
        
        secretRepository.delete(secret);
        
        auditService.logAction(user, AuditLog.ActionType.DELETE_SECRET, 
                              "Secret", secret.getId(), ipAddress);
    }
    
    public Page<SecretDto> getSecrets(String username, String projectName, Secret.SecretType type, 
                                     String search, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Specification<Secret> spec = Specification.where(null);
        
        spec = spec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user"), user));
        
        if (projectName != null && !projectName.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("projectName"), projectName));
        }
        
        if (type != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("type"), type));
        }
        
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), 
                                       "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), 
                                       "%" + search.toLowerCase() + "%")
                ));
        }
        
        return secretRepository.findAll(spec, pageable)
                .map(secret -> convertToDto(secret, false));
    }
    
    public List<String> getProjectNames(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return secretRepository.findDistinctProjectNamesByUser(user);
    }
    
    private SecretDto convertToDto(Secret secret, boolean includeValue) {
        SecretDto dto = new SecretDto();
        dto.setId(secret.getId());
        dto.setName(secret.getName());
        dto.setDescription(secret.getDescription());
        dto.setType(secret.getType());
        dto.setProjectName(secret.getProjectName());
        dto.setEnvironment(secret.getEnvironment());
        dto.setTags(secret.getTags());
        dto.setCreatedAt(secret.getCreatedAt());
        dto.setUpdatedAt(secret.getUpdatedAt());
        dto.setLastAccessedAt(secret.getLastAccessedAt());
        
        if (includeValue) {
            dto.setValue(encryptionService.decrypt(secret.getEncryptedValue()));
        }
        
        return dto;
    }
}