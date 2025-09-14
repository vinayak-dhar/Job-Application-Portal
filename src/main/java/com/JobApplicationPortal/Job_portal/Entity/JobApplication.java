package com.JobApplicationPortal.Job_portal.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    // Employee (Many applications → One employee)
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    // Job (Many applications → One job)
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
}
