package com.jobportal.employer.Repository;

import com.jobportal.employer.Entity.Job;
import com.jobportal.employer.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployer(User employer);
}
