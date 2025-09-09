package com.JobApplicationPortal.Job_portal.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private String employeeEmail;
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

