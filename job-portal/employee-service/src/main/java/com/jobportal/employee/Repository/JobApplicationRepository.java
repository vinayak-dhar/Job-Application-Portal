package com.jobportal.employee.Repository;

import com.jobportal.employee.Entity.Job;
import com.jobportal.employee.Entity.JobApplication;
import com.jobportal.employee.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByEmployee(User employee);
    Optional<JobApplication> findByEmployeeAndJob(User employee, Job job);
}
