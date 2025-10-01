package com.jobportal.employee.Repository;

import com.jobportal.employee.Entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> { }
