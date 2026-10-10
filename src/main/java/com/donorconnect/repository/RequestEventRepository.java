package com.donorconnect.repository;

import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.RequestEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestEventRepository extends JpaRepository<RequestEvent, Long> {
    List<RequestEvent> findByRequest(BloodRequest request);
    List<RequestEvent> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
