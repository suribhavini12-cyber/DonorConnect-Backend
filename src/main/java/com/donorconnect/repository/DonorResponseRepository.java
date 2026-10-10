package com.donorconnect.repository;

import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.DonorResponse;
import com.donorconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorResponseRepository extends JpaRepository<DonorResponse, Long> {
    List<DonorResponse> findByRequest(BloodRequest request);
    List<DonorResponse> findByRequestId(Long requestId);
    List<DonorResponse> findByDonor(User donor);
    List<DonorResponse> findByDonorId(Long donorId);
    Optional<DonorResponse> findByRequestIdAndDonorId(Long requestId, Long donorId);
    boolean existsByRequestIdAndDonorId(Long requestId, Long donorId);
}
