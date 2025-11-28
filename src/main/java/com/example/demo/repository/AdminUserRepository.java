package com.example.demo.repository;

import com.example.demo.Entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser,String> {
    Optional<AdminUser> findByUsername(String username);
//    Optional<AdminUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT a FROM AdminUser a WHERE a.deletedAt IS NULL")
    List<AdminUser> findAllActive();
    @Query("SELECT a FROM AdminUser a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<AdminUser> findByIdAndNotDeleted(String id);
}
