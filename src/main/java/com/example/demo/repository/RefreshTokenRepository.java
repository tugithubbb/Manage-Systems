package com.example.demo.repository;

import com.example.demo.Entity.AdminUser;
import com.example.demo.Entity.RefreshToken;
import com.example.demo.Entity.RestaurantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByAdminUser(AdminUser adminUser);
    void deleteByRestaurantUser(RestaurantUser restaurantUser);

    List<RefreshToken> findByAdminUser (AdminUser adminUser);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.adminUser.id = :userId AND r.revoked = false")
    void revokeALLTokensByUser(@Param("userId") String userId);

}
