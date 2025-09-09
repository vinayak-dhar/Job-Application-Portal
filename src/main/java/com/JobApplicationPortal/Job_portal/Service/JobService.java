package com.JobApplicationPortal.Job_portal.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }
}
