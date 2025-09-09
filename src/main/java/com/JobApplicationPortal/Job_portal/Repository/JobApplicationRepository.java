package com.JobApplicationPortal.Job_portal.Repository;

import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByEmployeeEmail(String email);
}
