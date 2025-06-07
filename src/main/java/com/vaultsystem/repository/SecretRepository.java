package com.vaultsystem.repository;

import com.vaultsystem.entity.Secret;
import com.vaultsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecretRepository extends JpaRepository<Secret, Long>, JpaSpecificationExecutor<Secret> {
    
    Optional<Secret> findByIdAndUser(Long id, User user);
    
    Page<Secret> findByUser(User user, Pageable pageable);
    
    List<Secret> findByUserAndProjectName(User user, String projectName);
    
    @Query("SELECT DISTINCT s.projectName FROM Secret s WHERE s.user = :user AND s.projectName IS NOT NULL")
    List<String> findDistinctProjectNamesByUser(@Param("user") User user);
    
    long countByUser(User user);
}