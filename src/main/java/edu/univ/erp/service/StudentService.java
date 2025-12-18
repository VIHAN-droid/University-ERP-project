package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentDAO studentDAO;
    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;
    private final AccessControl accessControl;

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
        this.accessControl = AccessControl.getInstance();
    }

    public String registerForSection(int studentId, int sectionId) {

        if (!accessControl.canStudentAddDropCourses()) {
            if (!accessControl.isAddDropEnabled()) {
                return accessControl.getAddDropClosedMessage();
            }
            return accessControl.getMaintenanceModeMessage();
        }

        if (enrollmentDAO.exists(studentId, sectionId)) {
            return "You are already enrolled in this section. Duplicate registration is not allowed.";
        }

        Section section = sectionDAO.getById(sectionId);
        if (section == null) {
            return "Section not found.";
        }

        if (enrollmentDAO.existsForCourse(studentId, section.getCourseId())) {
            String existingSection = enrollmentDAO.getExistingSectionCode(studentId, section.getCourseId());
            String courseCode = section.getCourseCode() != null ? section.getCourseCode() : "this course";
            String errorMsg = String.format(
                "You are already enrolled in %s Section %s.\n" +
                "Students cannot enroll in multiple sections of the same course.\n" +
                "Please drop the existing section first if you wish to change sections.",
                courseCode, existingSection != null ? existingSection : "");
            return errorMsg;
        }

        if (section.isFull()) {
            return "Section is full. No seats available.";
        }

        Enrollment enrollment = new Enrollment(studentId, sectionId);
        enrollment.setDropDeadline(LocalDateTime.now().plusDays(30));

        if (enrollmentDAO.create(enrollment)) {

            sectionDAO.updateEnrolledCount(sectionId, 1);
            logger.info("Student {} registered for section {}", studentId, sectionId);
            return null;
        }

        return "Failed to register for section. Please try again.";
    }

    public String dropSection(int enrollmentId) {

        if (!accessControl.canStudentAddDropCourses()) {
            if (!accessControl.isAddDropEnabled()) {
                return accessControl.getAddDropClosedMessage();
            }
            return accessControl.getMaintenanceModeMessage();
        }

        Enrollment enrollment = enrollmentDAO.getById(enrollmentId);
        if (enrollment == null) {
            return "Enrollment not found.";
        }

        if (!enrollment.canDrop()) {
            return "Drop deadline has passed for this section.";
        }

        if (enrollmentDAO.drop(enrollmentId)) {

            sectionDAO.updateEnrolledCount(enrollment.getSectionId(), -1);
            logger.info("Dropped enrollment {}", enrollmentId);
            return null;
        }

        return "Failed to drop section. Please try again.";
    }

    public List<Enrollment> getEnrollments(int studentId) {
        return enrollmentDAO.getByStudent(studentId);
    }

    public List<Enrollment> getActiveEnrollments(int studentId) {
        List<Enrollment> all = enrollmentDAO.getByStudent(studentId);
        List<Enrollment> active = new java.util.ArrayList<>();
        for (Enrollment e : all) {
            if (e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED) {
                active.add(e);
            }
        }
        return active;
    }

    public List<Grade> getGrades(int enrollmentId) {
        return gradeDAO.getByEnrollment(enrollmentId);
    }

    public List<Section> getAvailableSections() {
        return sectionDAO.getAll();
    }

    public String calculateFinalGrade(int enrollmentId) {
        BigDecimal percentage = calculateFinalPercentage(enrollmentId);
        if (percentage == null) {
            return "N/A";
        }
        return convertToLetterGrade(percentage);
    }

    public BigDecimal calculateFinalPercentage(int enrollmentId) {
        List<Grade> grades = gradeDAO.getByEnrollment(enrollmentId);
        if (grades.isEmpty()) {
            return null;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeightage = BigDecimal.ZERO;

        for (Grade grade : grades) {
            if (grade.getScore() != null && grade.getMaxScore() != null && grade.getWeightage() != null
                && grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = grade.getScore()
                        .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

                BigDecimal weightedScore = percentage.multiply(grade.getWeightage())
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

                totalWeightedScore = totalWeightedScore.add(weightedScore);
                totalWeightage = totalWeightage.add(grade.getWeightage());
            }
        }

        if (totalWeightage.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return totalWeightedScore.setScale(2, RoundingMode.HALF_UP);
    }

    public String getFormattedFinalGrade(int enrollmentId) {
        BigDecimal percentage = calculateFinalPercentage(enrollmentId);
        if (percentage == null) {
            return "N/A";
        }
        String letterGrade = convertToLetterGrade(percentage);
        return String.format("%s (%.2f%%)", letterGrade, percentage);
    }

    private String convertToLetterGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("95")) >= 0) return "A+";
        if (score.compareTo(new BigDecimal("90")) >= 0) return "A";
        if (score.compareTo(new BigDecimal("85")) >= 0) return "A-";
        if (score.compareTo(new BigDecimal("80")) >= 0) return "B+";
        if (score.compareTo(new BigDecimal("75")) >= 0) return "B";
        if (score.compareTo(new BigDecimal("70")) >= 0) return "B-";
        if (score.compareTo(new BigDecimal("65")) >= 0) return "C+";
        if (score.compareTo(new BigDecimal("60")) >= 0) return "C";
        if (score.compareTo(new BigDecimal("55")) >= 0) return "C-";
        if (score.compareTo(new BigDecimal("50")) >= 0) return "D";
        return "F";
    }

    private BigDecimal letterGradeToGradePoints(String letterGrade) {
        if (letterGrade == null) return BigDecimal.ZERO;
        switch (letterGrade) {
            case "A+": return new BigDecimal("10.0");
            case "A":  return new BigDecimal("9.0");
            case "A-": return new BigDecimal("8.5");
            case "B+": return new BigDecimal("8.0");
            case "B":  return new BigDecimal("7.0");
            case "B-": return new BigDecimal("6.5");
            case "C+": return new BigDecimal("6.0");
            case "C":  return new BigDecimal("5.0");
            case "C-": return new BigDecimal("4.5");
            case "D":  return new BigDecimal("4.0");
            case "F":  return BigDecimal.ZERO;
            default:   return BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateCGPA(int studentId) {
        List<Enrollment> enrollments = enrollmentDAO.getByStudent(studentId);
        if (enrollments.isEmpty()) {
            return null;
        }

        BigDecimal totalGradePoints = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        int coursesWithGrades = 0;

        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeDAO.getByEnrollment(enrollment.getEnrollmentId());
            if (grades.isEmpty()) {
                continue;
            }

            BigDecimal percentage = calculateFinalPercentage(enrollment.getEnrollmentId());
            if (percentage == null) {
                continue;
            }

            String letterGrade = convertToLetterGrade(percentage);
            BigDecimal gradePoints = letterGradeToGradePoints(letterGrade);

            Section section = sectionDAO.getById(enrollment.getSectionId());
            int credits = section != null ? section.getCourseCredits() : 3;

            totalGradePoints = totalGradePoints.add(gradePoints.multiply(new BigDecimal(credits)));
            totalCredits = totalCredits.add(new BigDecimal(credits));
            coursesWithGrades++;
        }

        if (coursesWithGrades == 0 || totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return totalGradePoints.divide(totalCredits, 2, RoundingMode.HALF_UP);
    }

    public String getFormattedCGPA(int studentId) {
        BigDecimal cgpa = calculateCGPA(studentId);
        if (cgpa == null) {
            return "N/A - No graded courses";
        }
        String overallGrade = cgpaToLetterGrade(cgpa);
        return String.format("%.2f (%s)", cgpa, overallGrade);
    }

    private String cgpaToLetterGrade(BigDecimal cgpa) {
        if (cgpa.compareTo(new BigDecimal("9.5")) >= 0) return "A+";
        if (cgpa.compareTo(new BigDecimal("8.5")) >= 0) return "A";
        if (cgpa.compareTo(new BigDecimal("8.0")) >= 0) return "A-";
        if (cgpa.compareTo(new BigDecimal("7.5")) >= 0) return "B+";
        if (cgpa.compareTo(new BigDecimal("6.5")) >= 0) return "B";
        if (cgpa.compareTo(new BigDecimal("6.0")) >= 0) return "B-";
        if (cgpa.compareTo(new BigDecimal("5.5")) >= 0) return "C+";
        if (cgpa.compareTo(new BigDecimal("5.0")) >= 0) return "C";
        if (cgpa.compareTo(new BigDecimal("4.5")) >= 0) return "C-";
        if (cgpa.compareTo(new BigDecimal("4.0")) >= 0) return "D";
        return "F";
    }

    public BigDecimal calculateAveragePercentage(int studentId) {
        List<Enrollment> enrollments = enrollmentDAO.getByStudent(studentId);
        if (enrollments.isEmpty()) {
            return null;
        }

        BigDecimal totalPercentage = BigDecimal.ZERO;
        int coursesWithGrades = 0;

        for (Enrollment enrollment : enrollments) {
            BigDecimal percentage = calculateFinalPercentage(enrollment.getEnrollmentId());
            if (percentage != null) {
                totalPercentage = totalPercentage.add(percentage);
                coursesWithGrades++;
            }
        }

        if (coursesWithGrades == 0) {
            return null;
        }

        return totalPercentage.divide(new BigDecimal(coursesWithGrades), 2, RoundingMode.HALF_UP);
    }
}
