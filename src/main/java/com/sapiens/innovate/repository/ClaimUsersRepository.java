package com.sapiens.innovate.repository;

import com.sapiens.innovate.entity.InnovaiteClaimUsers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimUsersRepository extends JpaRepository<InnovaiteClaimUsers,Long> {

    Optional<InnovaiteClaimUsers> findByUsername(String username);

    // Optional: fetch only users with specific role
    Page<InnovaiteClaimUsers> findByRole(String role, Pageable pageable);

    @Query("SELECT u FROM InnovaiteClaimUsers u WHERE u.username <> :username")
    Page<InnovaiteClaimUsers> findAllExceptUser(@Param("username") String username, Pageable pageable);

}
