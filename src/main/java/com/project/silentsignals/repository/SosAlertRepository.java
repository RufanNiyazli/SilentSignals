package com.project.silentsignals.repository;

import com.project.silentsignals.entity.SosAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SosAlertRepository extends JpaRepository<SosAlert, Long> {
    List<SosAlert> findByUser_EmailOrderByCreatedAtDesc(String userEmail);
}
