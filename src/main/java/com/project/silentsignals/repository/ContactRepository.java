package com.project.silentsignals.repository;

import com.project.silentsignals.entity.Contact;
import com.project.silentsignals.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByOwner(User owner);
}
