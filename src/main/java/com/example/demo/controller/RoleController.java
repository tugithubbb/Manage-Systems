package com.example.demo.controller;

import com.example.demo.Entity.Permission;
import com.example.demo.Entity.Role;
import com.example.demo.exception.IdInvalidException;
import com.example.demo.services.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @GetMapping("/roles/list")
    public ResponseEntity<List<Role>> getPermissions() {
        List<Role> roles = roleService.getAllUsers();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> create(@Valid @RequestBody Role role) throws IdInvalidException {
        // check name
        if (this.roleService.existByName(role.getName())) {
            throw new IdInvalidException("Role với name = " + role.getName() + " đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.create(role));
    }

    @PutMapping("/roles")
    public ResponseEntity<Role> update(@Valid @RequestBody Role role) throws IdInvalidException {
        // check id
        if (this.roleService.fetchById(role.getId()) == null) {
            throw new IdInvalidException("Role với id = " + role.getId() + " không tồn tại");
        }

        // check name
        // if (this.roleService.existByName(role.getName())) {
        // throw new IdInvalidException("Role với name = " + role.getName() + " đã tồn
        // tại");
        // }

        return ResponseEntity.ok().body(this.roleService.update(role));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) throws IdInvalidException {
        // check id
        if (this.roleService.fetchById(id) == null) {
            throw new IdInvalidException("Role với id = " + id + " không tồn tại");
        }
        this.roleService.delete(id);
        return ResponseEntity.ok().body(null);
    }


    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> getById(@PathVariable("id") String id) throws IdInvalidException {

        Role role = this.roleService.fetchById(id);
        if (role == null) {
            throw new IdInvalidException("Resume với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok().body(role);
    }



}
