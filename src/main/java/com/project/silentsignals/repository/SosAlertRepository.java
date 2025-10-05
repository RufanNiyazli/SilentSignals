package com.project.silentsignals.repository;

import com.project.silentsignals.entity.SosAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SosAlertRepository extends JpaRepository<SosAlert, Long> {
    List<SosAlert> findByUser_EmailOrderByCreatedAtDesc(String userEmail);
    @Query("SELECT s FROM SosAlert s " +
            "JOIN FETCH s.user u " +
            "LEFT JOIN FETCH u.contacts c " +
            "LEFT JOIN FETCH c.contactUser " +
            "WHERE s.id = :alertId")
    Optional<SosAlert> findByIdWithUserAndContacts(@Param("alertId") Long alertId);
}
