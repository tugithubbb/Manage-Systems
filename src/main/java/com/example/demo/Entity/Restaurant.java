package com.example.demo.Entity;

import com.example.demo.services.AuthenticationService;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "restaurants")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String address;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private AdminUser adminUser; // FK to admin_user

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "restaurant")
    private List<RestaurantUser> users;

//    @PrePersist
//    public void handleBeforeCreate() {
//        this.createdAt = AuthenticationService.getCurrentUserLogin().isPresent() == true
//                ? AuthenticationService.getCurrentUserLogin().get()
//                : "";
//
//        this.createdAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    public void handleBeforeUpdate() {
//        this.updatedBy = SecurityUtil.getCurrentUserLogin()
//                .orElse("");
//        this.updatedAt = LocalDateTime.now();  // dùng LocalDateTime thay vì Instant
//    }
}
