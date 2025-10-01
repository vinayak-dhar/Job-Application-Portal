package com.jobportal.admin.Repository;

import com.jobportal.admin.Entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> { }
