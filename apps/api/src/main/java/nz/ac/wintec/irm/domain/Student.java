package nz.ac.wintec.irm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "app_user_id", nullable = false, unique = true)
    private Integer appUserId;

    @Column(name = "name")
    private String name;

    @Column(name = "wintec_id", length = 20)
    private String wintecId;

    @Column(name = "gender", length = 16)
    private String gender;

    @Column(name = "student_type", length = 32)
    private String studentType;

    @Column(name = "personal_email")
    private String personalEmail;

    @Column(name = "student_email")
    private String studentEmail;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "personal_statement", length = 512)
    private String personalStatement;

    @Column(name = "cv_link")
    private String cvLink;

    @Column(name = "linkedin_link")
    private String linkedinLink;

    @Column(name = "portfolio_link")
    private String portfolioLink;

    @Column(name = "github_link")
    private String githubLink;

    @Column(name = "average_grade", length = 10)
    private String averageGrade;

    @Column(name = "programme_of_study")
    private String programmeOfStudy;

    @Column(name = "area_of_study")
    private String areaOfStudy;

    @Column(name = "internship_options")
    private String internshipOptions;

    @Column(name = "preferred_companies")
    private String preferredCompanies;

    @Column(name = "first_preference")
    private String firstPreference;

    @Column(name = "second_preference")
    private String secondPreference;

    @Column(name = "skills")
    private String skills;

    @Column(name = "favourite_courses")
    private String favouriteCourses;

    @Column(name = "reference", length = 50)
    private String reference;

    @Column(name = "speciality")
    private String speciality;

    @Column(name = "qualification_type")
    private String qualificationType;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "review_status", length = 50)
    private String reviewStatus;

    @Column(name = "interview_readiness", length = 5)
    private String interviewReadiness;

    @Column(name = "app_status")
    private Integer appStatus;

    @Column(name = "intern_status")
    private Integer internStatus;

    protected Student() {
    }

    public Student(Integer appUserId) {
        this.appUserId = appUserId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public Integer getAppUserId() {
        return appUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWintecId() {
        return wintecId;
    }

    public void setWintecId(String wintecId) {
        this.wintecId = wintecId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPersonalStatement() {
        return personalStatement;
    }

    public void setPersonalStatement(String personalStatement) {
        this.personalStatement = personalStatement;
    }

    public String getCvLink() {
        return cvLink;
    }

    public void setCvLink(String cvLink) {
        this.cvLink = cvLink;
    }

    public String getLinkedinLink() {
        return linkedinLink;
    }

    public void setLinkedinLink(String linkedinLink) {
        this.linkedinLink = linkedinLink;
    }

    public String getPortfolioLink() {
        return portfolioLink;
    }

    public void setPortfolioLink(String portfolioLink) {
        this.portfolioLink = portfolioLink;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }

    public String getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(String averageGrade) {
        this.averageGrade = averageGrade;
    }

    public String getProgrammeOfStudy() {
        return programmeOfStudy;
    }

    public void setProgrammeOfStudy(String programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    public String getAreaOfStudy() {
        return areaOfStudy;
    }

    public void setAreaOfStudy(String areaOfStudy) {
        this.areaOfStudy = areaOfStudy;
    }

    public String getInternshipOptions() {
        return internshipOptions;
    }

    public void setInternshipOptions(String internshipOptions) {
        this.internshipOptions = internshipOptions;
    }

    public String getPreferredCompanies() {
        return preferredCompanies;
    }

    public void setPreferredCompanies(String preferredCompanies) {
        this.preferredCompanies = preferredCompanies;
    }

    public String getFirstPreference() {
        return firstPreference;
    }

    public void setFirstPreference(String firstPreference) {
        this.firstPreference = firstPreference;
    }

    public String getSecondPreference() {
        return secondPreference;
    }

    public void setSecondPreference(String secondPreference) {
        this.secondPreference = secondPreference;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getFavouriteCourses() {
        return favouriteCourses;
    }

    public void setFavouriteCourses(String favouriteCourses) {
        this.favouriteCourses = favouriteCourses;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getQualificationType() {
        return qualificationType;
    }

    public void setQualificationType(String qualificationType) {
        this.qualificationType = qualificationType;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getInterviewReadiness() {
        return interviewReadiness;
    }

    public void setInterviewReadiness(String interviewReadiness) {
        this.interviewReadiness = interviewReadiness;
    }

    public Integer getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(Integer appStatus) {
        this.appStatus = appStatus;
    }

    public Integer getInternStatus() {
        return internStatus;
    }

    public void setInternStatus(Integer internStatus) {
        this.internStatus = internStatus;
    }
}
