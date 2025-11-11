package com.sapiens.innovate.controller;


import com.sapiens.innovate.entity.InnovaiteClaimUsers;
import com.sapiens.innovate.model.Role;
import com.sapiens.innovate.repository.ClaimUsersRepository;
import com.sapiens.innovate.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {


    @Autowired
    private ClaimUsersRepository claimUsersRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.getOrDefault("role", "ROLE_USER");


        if (claimUsersRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", String.format("Username %s already exists", username)));
        }


        InnovaiteClaimUsers u = new InnovaiteClaimUsers(username, passwordEncoder.encode(password), Role.valueOf(role));
        claimUsersRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "user created"));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");


        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));


        InnovaiteClaimUsers user = claimUsersRepository.findByUsername(username).orElseThrow();
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());


        return ResponseEntity.ok(Map.of("token", token));
    }
}