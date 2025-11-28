package com.example.demo.repository;

import com.example.demo.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission , String> {
    boolean existsByModuleAndApiPathAndMethod(String module, String apiPath, String method);

    List<Permission> findByIdIn(List<String> id);
}
