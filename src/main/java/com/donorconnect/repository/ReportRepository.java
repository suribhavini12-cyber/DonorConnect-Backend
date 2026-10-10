package com.donorconnect.repository;

import com.donorconnect.model.Report;
import com.donorconnect.model.ReportStatus;
import com.donorconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporter(User reporter);
    List<Report> findAllByOrderByCreatedAtDesc();
    long countByStatus(ReportStatus status);
}
