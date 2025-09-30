package com.JobApplicationPortal.Job_portal.job_service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.JobApplicationPortal.Job_portal.job_service.Entity.Job;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }
}
