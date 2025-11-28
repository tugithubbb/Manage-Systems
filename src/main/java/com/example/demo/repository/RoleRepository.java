package com.example.demo.repository;

import com.example.demo.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,String> {
    boolean existsByName(String name);

    Role findByName(String name);
}
