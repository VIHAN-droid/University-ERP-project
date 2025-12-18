package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class InstructorService {
    private static final Logger logger = LoggerFactory.getLogger(InstructorService.class);
    private final InstructorDAO instructorDAO;
    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;
    private final AccessControl accessControl;

    public InstructorService() {
        this.instructorDAO = new InstructorDAO();
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
        this.accessControl = AccessControl.getInstance();
    }

    public List<Section> getMySections(int instructorId) {
        return sectionDAO.getByInstructor(instructorId);
    }

    public List<Enrollment> getSectionEnrollments(int sectionId) {

        return enrollmentDAO.getBySection(sectionId);
    }

    public boolean isMySection(int instructorId, int sectionId) {
        Section section = sectionDAO.getById(sectionId);
        return section != null && section.getInstructorId() != null && section.getInstructorId() == instructorId;
    }

    public String enterGrade(int enrollmentId, String component, BigDecimal score,
                           BigDecimal maxScore, BigDecimal weightage) {
        if (!accessControl.canModifyData()) {
            return accessControl.getMaintenanceModeMessage();
        }

        if (component == null || component.trim().isEmpty()) {
            return "Component name is required.";
        }

        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(maxScore) > 0) {
            return "Score must be between 0 and max score.";
        }

        if (weightage.compareTo(BigDecimal.ZERO) < 0 || weightage.compareTo(new BigDecimal("100")) > 0) {
            return "Weightage must be between 0 and 100.";
        }

        BigDecimal currentTotal = gradeDAO.getTotalWeightage(enrollmentId);
        BigDecimal newTotal = currentTotal.add(weightage);
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            return String.format("Cannot add this grade component. Total weightage would be %.2f%% (max 100%%). " +
                    "Current total: %.2f%%, trying to add: %.2f%%.",
                    newTotal, currentTotal, weightage);
        }

        if (gradeDAO.existsByComponent(enrollmentId, component.trim())) {
            return "A grade for component '" + component + "' already exists for this student. Use 'Edit Grade' to modify it.";
        }

        Grade grade = new Grade(enrollmentId, component.trim());
        grade.setScore(score);
        grade.setMaxScore(maxScore);
        grade.setWeightage(weightage);

        if (gradeDAO.create(grade)) {
            logger.info("Grade entered for enrollment {}", enrollmentId);
            return null;
        }

        return "Failed to enter grade. Please try again.";
    }

    public String updateGrade(Grade grade) {
        if (!accessControl.canModifyData()) {
            return accessControl.getMaintenanceModeMessage();
        }

        if (grade.getWeightage() != null) {
            if (grade.getWeightage().compareTo(BigDecimal.ZERO) < 0 ||
                grade.getWeightage().compareTo(new BigDecimal("100")) > 0) {
                return "Weightage must be between 0 and 100.";
            }

            Grade existingGrade = gradeDAO.getById(grade.getGradeId());
            if (existingGrade != null) {

                BigDecimal otherTotal = gradeDAO.getTotalWeightageExcluding(
                        existingGrade.getEnrollmentId(), grade.getGradeId());
                BigDecimal newTotal = otherTotal.add(grade.getWeightage());
                if (newTotal.compareTo(new BigDecimal("100")) > 0) {
                    return String.format("Cannot update this grade. Total weightage would be %.2f%% (max 100%%). " +
                            "Other components total: %.2f%%, new weightage: %.2f%%.",
                            newTotal, otherTotal, grade.getWeightage());
                }
            }
        }

        if (gradeDAO.update(grade)) {
            logger.info("Grade updated: {}", grade.getGradeId());
            return null;
        }

        return "Failed to update grade.";
    }

    public List<Grade> getGrades(int enrollmentId) {
        return gradeDAO.getByEnrollment(enrollmentId);
    }

    public BigDecimal getTotalWeightage(int enrollmentId) {
        return gradeDAO.getTotalWeightage(enrollmentId);
    }

    public BigDecimal getRemainingWeightage(int enrollmentId) {
        BigDecimal total = gradeDAO.getTotalWeightage(enrollmentId);
        return new BigDecimal("100").subtract(total);
    }

    public String getClassStatistics(int sectionId) {
        List<Enrollment> enrollments = enrollmentDAO.getBySection(sectionId);
        if (enrollments.isEmpty()) {
            return "No students enrolled.";
        }

        int totalStudents = enrollments.size();
        BigDecimal totalScore = BigDecimal.ZERO;
        int studentsWithGrades = 0;

        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeDAO.getByEnrollment(enrollment.getEnrollmentId());
            if (!grades.isEmpty()) {
                BigDecimal enrollmentTotal = BigDecimal.ZERO;
                for (Grade grade : grades) {
                    if (grade.getScore() != null && grade.getMaxScore() != null) {
                        BigDecimal percentage = grade.getScore()
                                .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
                        enrollmentTotal = enrollmentTotal.add(percentage
                                .multiply(grade.getWeightage())
                                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    }
                }
                if (enrollmentTotal.compareTo(BigDecimal.ZERO) > 0) {
                    totalScore = totalScore.add(enrollmentTotal);
                    studentsWithGrades++;
                }
            }
        }

        if (studentsWithGrades == 0) {
            return "No grades entered yet.";
        }

        BigDecimal average = totalScore.divide(new BigDecimal(studentsWithGrades), 2, RoundingMode.HALF_UP);

        return String.format("Class Statistics:\nTotal Students: %d\nStudents with Grades: %d\nClass Average: %.2f%%",
                totalStudents, studentsWithGrades, average);
    }
}
