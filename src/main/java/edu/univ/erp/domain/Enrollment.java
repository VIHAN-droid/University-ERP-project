package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Enrollment {
    private int enrollmentId;
    private int studentId;
    private int sectionId;
    private EnrollmentStatus status;
    private LocalDateTime enrolledDate;
    private LocalDateTime dropDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String courseCode;
    private String courseTitle;
    private String sectionCode;
    private String semester;
    private int year;
    private String dayTime;
    private String room;
    private String studentRollNo;
    private String studentProgram;

    public enum EnrollmentStatus {
        ENROLLED, DROPPED, COMPLETED
    }

    public Enrollment() {
        this.status = EnrollmentStatus.ENROLLED;
    }

    public Enrollment(int studentId, int sectionId) {
        this();
        this.studentId = studentId;
        this.sectionId = sectionId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledDate() {
        return enrolledDate;
    }

    public void setEnrolledDate(LocalDateTime enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    public LocalDateTime getDropDeadline() {
        return dropDeadline;
    }

    public void setDropDeadline(LocalDateTime dropDeadline) {
        this.dropDeadline = dropDeadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentProgram() {
        return studentProgram;
    }

    public void setStudentProgram(String studentProgram) {
        this.studentProgram = studentProgram;
    }

    public boolean canDrop() {
        return status == EnrollmentStatus.ENROLLED &&
               (dropDeadline == null || LocalDateTime.now().isBefore(dropDeadline));
    }

    @Override
    public String toString() {
        return "Enrollment{" +"enrollmentId=" + enrollmentId +", studentId=" + studentId +", sectionId="
        + sectionId +", status=" + status +'}';
    }
}
