package com.JobApplicationPortal.Job_portal.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Core Job Details
    private String jobTitle;
    private String companyName;
    @Column(length = 1000)
    private String jobDescription;
    private String jobLocation;
    private String workplaceType; // On-site, Remote, Hybrid

    // Candidate Requirements
    private String skillsAndQualifications;
    private String experienceLevel;
    private String education;

    // Compensation and Benefits
    private String salaryRange;
    private String jobType; // Full-time, Part-time, etc.

    // Employer (Many jobs → One Employer)
    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;

    // Applications
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobApplication> applications;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getJobLocation() { return jobLocation; }
    public void setJobLocation(String jobLocation) { this.jobLocation = jobLocation; }

    public String getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

    public String getSkillsAndQualifications() { return skillsAndQualifications; }
    public void setSkillsAndQualifications(String skillsAndQualifications) { this.skillsAndQualifications = skillsAndQualifications; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getJobType() { return jobType; }

    public User getEmployer() {
        return employer;
    }

    public void setEmployer(User employer) {
        this.employer = employer;
    }

    public void setJobType(String jobType) { this.jobType = jobType; }
}
