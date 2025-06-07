package com.vaultsystem.service;

import com.vaultsystem.dto.SecretCreateDto;
import com.vaultsystem.dto.SecretDto;
import com.vaultsystem.entity.Secret;
import com.vaultsystem.entity.User;
import com.vaultsystem.repository.SecretRepository;
import com.vaultsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretServiceTest {
    
    @Mock
    private SecretRepository secretRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EncryptionService encryptionService;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private SecretService secretService;
    
    private User testUser;
    private Secret testSecret;
    private SecretCreateDto createDto;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testSecret = new Secret();
        testSecret.setId(1L);
        testSecret.setName("Test Secret");
        testSecret.setEncryptedValue("encrypted_value");
        testSecret.setType(Secret.SecretType.API_KEY);
        testSecret.setUser(testUser);
        
        createDto = new SecretCreateDto();
        createDto.setName("New Secret");
        createDto.setValue("secret_value");
        createDto.setType(Secret.SecretType.API_KEY);
        createDto.setProjectName("Test Project");
        createDto.setTags(Set.of("test", "api"));
    }
    
    @Test
    void createSecret_ShouldCreateAndReturnSecret() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(encryptionService.encrypt("secret_value")).thenReturn("encrypted_value");
        when(secretRepository.save(any(Secret.class))).thenReturn(testSecret);
        
        // When
        SecretDto result = secretService.createSecret(createDto, "testuser", "127.0.0.1");
        
        // Then
        assertNotNull(result);
        assertEquals("Test Secret", result.getName());
        assertEquals(Secret.SecretType.API_KEY, result.getType());
        verify(auditService).logAction(eq(testUser), any(), eq("Secret"), any(), eq("127.0.0.1"));
    }
    
    @Test
    void getSecret_ShouldReturnSecretWithoutValue() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(secretRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testSecret));
        
        // When
        SecretDto result = secretService.getSecret(1L, "testuser", "127.0.0.1", false);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Secret", result.getName());
        assertNull(result.getValue());
        verify(auditService, never()).logAction(any(), any(), any(), any(), any());
    }
    
    @Test
    void getSecret_ShouldReturnSecretWithValueAndLogAccess() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(secretRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testSecret));
        when(encryptionService.decrypt("encrypted_value")).thenReturn("decrypted_value");
        
        // When
        SecretDto result = secretService.getSecret(1L, "testuser", "127.0.0.1", true);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Secret", result.getName());
        assertEquals("decrypted_value", result.getValue());
        verify(auditService).logAction(eq(testUser), any(), eq("Secret"), eq(1L), eq("127.0.0.1"));
        verify(secretRepository).save(testSecret);
    }
    
    @Test
    void deleteSecret_ShouldDeleteSecretAndLogAction() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(secretRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testSecret));
        
        // When
        secretService.deleteSecret(1L, "testuser", "127.0.0.1");
        
        // Then
        verify(secretRepository).delete(testSecret);
        verify(auditService).logAction(eq(testUser), any(), eq("Secret"), eq(1L), eq("127.0.0.1"));
    }
    
    @Test
    void createSecret_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> secretService.createSecret(createDto, "testuser", "127.0.0.1"));
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void getSecret_SecretNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(secretRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> secretService.getSecret(1L, "testuser", "127.0.0.1", false));
        
        assertEquals("Secret not found", exception.getMessage());
    }
}