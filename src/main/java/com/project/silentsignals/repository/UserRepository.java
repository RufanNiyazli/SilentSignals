package com.project.silentsignals.repository;

import com.project.silentsignals.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.contacts c " +
            "LEFT JOIN FETCH c.contactUser " +
            "WHERE u.email = :email")
    Optional<User> findUserByEmailWithAssociations(@Param("email") String email);
    boolean existsByEmail(String email);
}