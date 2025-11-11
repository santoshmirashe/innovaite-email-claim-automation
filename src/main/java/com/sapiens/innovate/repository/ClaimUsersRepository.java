package com.sapiens.innovate.repository;

import com.sapiens.innovate.entity.InnovaiteClaimUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimUsersRepository extends JpaRepository<InnovaiteClaimUsers,Long> {

    Optional<InnovaiteClaimUsers> findByUsername(String username);

}
