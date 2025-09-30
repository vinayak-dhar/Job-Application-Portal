package com.JobApplicationPortal.Job_portal.job_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.JobApplicationPortal.Job_portal.job_service.Entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}


