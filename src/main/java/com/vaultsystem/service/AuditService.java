package com.vaultsystem.service;

import com.vaultsystem.dto.AuditLogDto;
import com.vaultsystem.entity.AuditLog;
import com.vaultsystem.entity.User;
import com.vaultsystem.repository.AuditLogRepository;
import com.vaultsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public void logAction(User user, AuditLog.ActionType action, String resource, 
                         Long resourceId, String ipAddress) {
        logAction(user, action, resource, resourceId, ipAddress, null, null);
    }
    
    @Transactional
    public void logAction(User user, AuditLog.ActionType action, String resource, 
                         Long resourceId, String ipAddress, String userAgent, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setResource(resource);
        auditLog.setResourceId(resourceId);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setDetails(details);
        
        auditLogRepository.save(auditLog);
    }
    
    public Page<AuditLogDto> getAuditLogs(String username, AuditLog.ActionType action, 
                                         LocalDateTime startDate, LocalDateTime endDate, 
                                         Pageable pageable) {
        Specification<AuditLog> spec = Specification.where(null);
        
        if (username != null && !username.isEmpty()) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("user"), user));
        }
        
        if (action != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("action"), action));
        }
        
        if (startDate != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
        }
        
        if (endDate != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
        }
        
        return auditLogRepository.findAll(spec, pageable)
                .map(this::convertToDto);
    }
    
    public Page<AuditLogDto> getUserAuditLogs(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return auditLogRepository.findByUserOrderByTimestampDesc(user, pageable)
                .map(this::convertToDto);
    }
    
    private AuditLogDto convertToDto(AuditLog auditLog) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(auditLog.getId());
        dto.setUsername(auditLog.getUser().getUsername());
        dto.setAction(auditLog.getAction());
        dto.setResource(auditLog.getResource());
        dto.setResourceId(auditLog.getResourceId());
        dto.setDetails(auditLog.getDetails());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }
}