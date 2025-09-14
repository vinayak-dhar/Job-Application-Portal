package com.JobApplicationPortal.Job_portal.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.JobApplicationPortal.Job_portal.Entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> { }
