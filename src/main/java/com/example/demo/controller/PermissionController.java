package com.example.demo.controller;

import com.example.demo.Entity.Permission;
import com.example.demo.exception.IdInvalidException;
import com.example.demo.services.PermissionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
    PermissionService permissionService;
    @PostMapping("/permissions")
    public ResponseEntity<Permission> create(@Valid @RequestBody Permission permission) throws IdInvalidException {
        // check exist
        if (this.permissionService.isPermissionExist(permission)) {
            throw new IdInvalidException("Permission đã tồn tại.");
        }

        // create new permission
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.create(permission));
    }

    @PutMapping("/permissions")
    public ResponseEntity<Permission> update(@Valid @RequestBody Permission permission) throws IdInvalidException {
        // check exist by id
        if (this.permissionService.fetchById(permission.getId()) == null) {
            throw new IdInvalidException("Permission với id = " + permission.getId() + " không tồn tại.");
        }

        // check exist by module, apiPath and method
        if (this.permissionService.isPermissionExist(permission)) {
            // check name
            if (this.permissionService.isSameName(permission)) {
                throw new IdInvalidException("Permission đã tồn tại.");
            }
        }

        // update permission
        return ResponseEntity.ok().body(this.permissionService.update(permission));
    }

    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) throws IdInvalidException {
        // check exist by id
        if (this.permissionService.fetchById(id) == null) {
            throw new IdInvalidException("Permission với id = " + id + " không tồn tại.");
        }
        this.permissionService.delete(id);
        return ResponseEntity.ok().body(null);
    }
    @GetMapping("/permissions/list")
    public ResponseEntity<List<Permission>> getPermissions() {
        List<Permission> permissions = permissionService.getAllUsers();
        return ResponseEntity.ok(permissions);
    }
}
