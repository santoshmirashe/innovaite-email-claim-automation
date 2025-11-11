package com.sapiens.innovate.entity;

import com.sapiens.innovate.model.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INNOVAITE_CLAIM_USERS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class InnovaiteClaimUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    private String username;


    @Column(nullable = false)
    private String password; // store BCrypt-hashed password


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    public InnovaiteClaimUsers(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
