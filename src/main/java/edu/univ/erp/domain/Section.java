package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Section {
    private int sectionId;
    private int courseId;
    private Integer instructorId;
    private String sectionCode;
    private String dayTime;
    private String room;
    private int capacity;
    private int enrolledCount;
    private String semester;
    private int year;
    private SectionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String courseCode;
    private String courseTitle;
    private int courseCredits;
    private String instructorName;

    public enum SectionStatus {
        ACTIVE, INACTIVE, COMPLETED
    }

    public Section() {
        this.status = SectionStatus.ACTIVE;
        this.enrolledCount = 0;
    }

    public Section(int courseId, String sectionCode, int capacity, String semester, int year) {
        this();
        this.courseId = courseId;
        this.sectionCode = sectionCode;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Integer getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Integer instructorId) {
        this.instructorId = instructorId;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
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

    public SectionStatus getStatus() {
        return status;
    }

    public void setStatus(SectionStatus status) {
        this.status = status;
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

    public int getCourseCredits() {
        return courseCredits;
    }

    public void setCourseCredits(int courseCredits) {
        this.courseCredits = courseCredits;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public boolean isFull() {
        return enrolledCount >= capacity;
    }

    public int getAvailableSeats() {
        return capacity - enrolledCount;
    }

    @Override
    public String toString() {
        return "Section{" +"sectionId=" + sectionId +", courseCode='" + courseCode + '\'' +
                ", sectionCode='" + sectionCode + '\'' +", semester='" + semester + '\'' +
                ", year=" + year +", capacity=" + capacity +", enrolledCount=" + enrolledCount +'}';
    }
}
