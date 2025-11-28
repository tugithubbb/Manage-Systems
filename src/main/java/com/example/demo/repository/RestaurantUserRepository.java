package com.example.demo.repository;

import com.example.demo.Entity.RestaurantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantUserRepository extends JpaRepository<RestaurantUser,String> {
    boolean existsByUsernameAndDeletedAtIsNull(String username);

    Optional<RestaurantUser> findByUsername(String username);

}
