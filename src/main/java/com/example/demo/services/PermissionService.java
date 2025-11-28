package com.example.demo.services;

import com.example.demo.Entity.Permission;
import com.example.demo.dto.response.RestaurantUserResponse;
import com.example.demo.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }
    public List<Permission> getAllUsers() {
        return permissionRepository.findAll()
                .stream()
                .collect(Collectors.toList());
    }


    public boolean isPermissionExist(Permission permission) {
        return permissionRepository.existsByModuleAndApiPathAndMethod(
                permission.getModule(),
                permission.getApiPath(),
                permission.getMethod());
    }

    public Permission fetchById(String id) {
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        if (permissionOptional.isPresent())
            return permissionOptional.get();
        return null;
    }

    public Permission create(Permission permission) {
        return this.permissionRepository.save(permission);
    }

    public Permission update(Permission permission) {
        Permission permissionDB = this.fetchById(permission.getId());
        if (permissionDB != null) {
            permissionDB.setName(permission.getName());
            permissionDB.setApiPath(permission.getApiPath());
            permissionDB.setMethod(permission.getMethod());
            permissionDB.setModule(permission.getModule());

            // update
            permissionDB = this.permissionRepository.save(permissionDB);
            return permissionDB;
        }
        return null;
    }

    public void delete(String id) {
        // delete permission_role
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        Permission currentPermission = permissionOptional.get();
        currentPermission.getRoles().forEach(role -> role.getPermissions().remove(currentPermission));

        // delete permission
        this.permissionRepository.delete(currentPermission);
    }
    public boolean isSameName(Permission permission) {
        Permission permissionDB = this.fetchById(permission.getId());
        if (permissionDB != null) {
            if (permissionDB.getName().equals(permission.getName()))
                return true;
        }
        return false;
    }

}
