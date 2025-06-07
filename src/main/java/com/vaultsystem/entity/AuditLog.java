package com.vaultsystem.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;
    
    @Column(nullable = false)
    private String resource;
    
    @Column
    private Long resourceId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column
    private String userAgent;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(User user, ActionType action, String resource, String ipAddress) {
        this.user = user;
        this.action = action;
        this.resource = resource;
        this.ipAddress = ipAddress;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public enum ActionType {
        LOGIN, LOGOUT, CREATE_SECRET, READ_SECRET, UPDATE_SECRET, DELETE_SECRET, 
        EXPORT_DATA, SEARCH_SECRETS, FAILED_LOGIN, ACCOUNT_LOCKED
    }
}