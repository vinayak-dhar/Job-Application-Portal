package com.JobApplicationPortal.Job_portal.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.JobApplicationPortal.Job_portal.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}


