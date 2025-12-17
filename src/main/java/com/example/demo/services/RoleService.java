package com.example.demo.services;

import com.example.demo.Entity.Permission;
import com.example.demo.Entity.Role;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role create(Role role) {
        // check permissions
        if (role.getPermissions() != null) {
            List<String> reqPermissions = role.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            role.setPermissions(dbPermissions);
        }

        return this.roleRepository.save(role);
    }

    public Role fetchById(String id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (roleOptional.isPresent())
            return roleOptional.get();
        return null;
    }

    public Role update(Role role) {
        Role roleDB = this.fetchById(role.getId());

        roleDB.setName(role.getName());
        roleDB.setDescription(role.getDescription());
        roleDB.setActive(role.isActive());

        // update permissions
        if (role.getPermissions() != null) {
            if (role.getPermissions().isEmpty()) {
                roleDB.setPermissions(new ArrayList<>());
            } else {
                List<String> reqPermissionIds = role.getPermissions()
                        .stream()
                        .map(Permission::getId)
                        .collect(Collectors.toList());

                List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissionIds);
                roleDB.setPermissions(dbPermissions); // gán list từ DB
            }
        }

        return this.roleRepository.save(roleDB);
    }


    public void delete(String id) {
        this.roleRepository.deleteById(id);
    }

    public List<Role> getAllUsers() {
        return roleRepository.findAll()
                .stream()
                .collect(Collectors.toList());
    }
    //hello
}
