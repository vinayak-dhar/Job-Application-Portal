package com.jobportal.admin.Repository;

import com.jobportal.admin.Entity.Job;
import com.jobportal.admin.Entity.JobApplication;
import com.jobportal.admin.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByEmployee(User employee);
    Optional<JobApplication> findByEmployeeAndJob(User employee, Job job);
}
