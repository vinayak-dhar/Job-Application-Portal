package com.JobApplicationPortal.Job_portal.job_service.Repository;

import com.JobApplicationPortal.Job_portal.job_service.Entity.Job;
import com.JobApplicationPortal.Job_portal.job_service.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.job_service.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByEmployee(User employee);
    Optional<JobApplication> findByEmployeeAndJob(User employee, Job job);
}
