package com.sapiens.innovate.service;

import com.sapiens.innovate.entity.InnovaiteClaimUsers;
import com.sapiens.innovate.model.Role;
import com.sapiens.innovate.repository.ClaimUsersRepository;
import com.sapiens.innovate.vo.UserDTO;
import com.sapiens.innovate.vo.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ClaimUsersRepository claimUsersRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        InnovaiteClaimUsers innovaiteClaimUser = claimUsersRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        GrantedAuthority ga = new SimpleGrantedAuthority(innovaiteClaimUser.getRole().name());
        return new User(innovaiteClaimUser.getUsername(), innovaiteClaimUser.getPassword(), List.of(ga));
    }

    // Fetch paginated users as DTOs
    public Page<UserDTO> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<InnovaiteClaimUsers> users = claimUsersRepository.findAll(pageable);

        return users.map(UserMapper::toDTO);
    }

    // Update user role
    public boolean updateRole(Long id, String role) {
        return claimUsersRepository.findById(id)
                .map(u -> {
                    u.setRole(Role.valueOf(role));
                    claimUsersRepository.save(u);
                    return true;
                })
                .orElse(false);
    }
}
