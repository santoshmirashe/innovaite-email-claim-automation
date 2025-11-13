package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.CustomUserDetailsService;
import com.sapiens.innovate.vo.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private CustomUserDetailsService service;


    // -----------------------------
    // GET /admin/users?page=0&size=10
    // -----------------------------
    @GetMapping("/users")
    public Page<UserDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getUsers(page, size);
    }

    // DTO for incoming request
    public static class RoleUpdateRequest {
        public Long id;
        public String role;
    }

    // -----------------------------
    // POST /admin/update-role
    // -----------------------------
    @PostMapping("/update-role")
    public ResponseEntity<?> updateRole(@RequestBody RoleUpdateRequest req) {
        boolean ok = service.updateRole(req.id, req.role);

        return ok ? ResponseEntity.ok("Role updated")
                : ResponseEntity.status(404).body("User not found");
    }
}
