package com.donorconnect.repository;

import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.RequestStatus;
import com.donorconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequester(User requester);
    long countByStatus(RequestStatus status);
}
