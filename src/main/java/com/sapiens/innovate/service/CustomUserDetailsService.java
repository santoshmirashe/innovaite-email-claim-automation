package com.sapiens.innovate.service;

import com.sapiens.innovate.entity.InnovaiteClaimUsers;
import com.sapiens.innovate.repository.ClaimUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
