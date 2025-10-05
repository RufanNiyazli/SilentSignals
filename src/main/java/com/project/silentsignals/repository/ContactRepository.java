package com.project.silentsignals.repository;

import com.project.silentsignals.entity.Contact;
import com.project.silentsignals.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByOwner(User owner);
    @Query("SELECT c FROM Contact c " +
            "JOIN FETCH c.contactUser " +
            "WHERE c.owner.id = :userId")
    List<Contact> findByOwnerIdWithContactUser(@Param("userId") Long userId);
}
