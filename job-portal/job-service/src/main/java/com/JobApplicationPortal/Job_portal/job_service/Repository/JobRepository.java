package com.JobApplicationPortal.Job_portal.job_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.JobApplicationPortal.Job_portal.job_service.Entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> { }
