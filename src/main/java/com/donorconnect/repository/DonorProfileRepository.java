package com.donorconnect.repository;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.DonorProfile;
import com.donorconnect.model.ParticipationStatus;
import com.donorconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {
    Optional<DonorProfile> findByUser(User user);
    Optional<DonorProfile> findByUserId(Long userId);
    List<DonorProfile> findByBloodGroupAndParticipationStatusAndConsentToContactTrue(BloodGroup bloodGroup, ParticipationStatus participationStatus);
}
