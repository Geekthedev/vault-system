package com.vaultsystem.dto;

import com.vaultsystem.entity.Secret;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class SecretCreateDto {
    
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    private String description;
    
    @NotBlank
    private String value;
    
    @NotNull
    private Secret.SecretType type;
    
    private String projectName;
    
    private String environment;
    
    private Set<String> tags;
    
    // Constructors
    public SecretCreateDto() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    
    public Secret.SecretType getType() { return type; }
    public void setType(Secret.SecretType type) { this.type = type; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
}