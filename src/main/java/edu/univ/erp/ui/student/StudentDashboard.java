package edu.univ.erp.ui.student;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.common.LoginFrame;
import edu.univ.erp.ui.common.UITheme;
import edu.univ.erp.util.MessageUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final Student student;
    private final StudentService studentService;
    private final AccessControl accessControl;

    private JTabbedPane tabbedPane;
    private JTable catalogTable;
    private JTable enrollmentsTable;
    private JTable gradesTable;
    private JTable timetableTable;
    private JLabel maintenanceBanner;
    private JLabel addDropBanner;
    private JLabel cgpaLabel;
    private List<Enrollment> currentEnrollments;

    public StudentDashboard(Student student) {
        this.student = student;
        this.studentService = new StudentService();
        this.accessControl = AccessControl.getInstance();

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setTitle("University ERP - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        UITheme.styleTabbedPane(tabbedPane);

        tabbedPane.addTab("📚 Course Catalog", createCatalogPanel());
        tabbedPane.addTab("📋 My Registrations", createRegistrationsPanel());
        tabbedPane.addTab("📅 Timetable", createTimetablePanel());
        tabbedPane.addTab("📊 Grades", createGradesPanel());

        maintenanceBanner = UITheme.createMaintenanceBanner();
        maintenanceBanner.setVisible(accessControl.isMaintenanceModeEnabled());

        addDropBanner = UITheme.createAddDropClosedBanner();
        addDropBanner.setVisible(!accessControl.isAddDropEnabled());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        JPanel welcomeBanner = createWelcomeBanner();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.PRIMARY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_LG, UITheme.SPACING_MD, UITheme.SPACING_LG));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        leftPanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACING_SM, 0));
        buttonPanel.setOpaque(false);

        JButton changePasswordButton = new JButton("Change Password");
        UITheme.styleSecondaryButton(changePasswordButton);
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());

        JButton logoutButton = new JButton("Logout");
        UITheme.stylePrimaryButton(logoutButton);
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(changePasswordButton);
        buttonPanel.add(logoutButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel bannerPanel = new JPanel();
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBackground(UITheme.BACKGROUND);
        bannerPanel.add(maintenanceBanner);
        bannerPanel.add(addDropBanner);

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.add(bannerPanel, BorderLayout.NORTH);
        topArea.add(topPanel, BorderLayout.CENTER);
        topArea.add(welcomeBanner, BorderLayout.SOUTH);

        add(topArea, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                    0, 0, UITheme.SECONDARY,
                    getWidth(), 0, UITheme.ACCENT
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        banner.setLayout(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_LG, UITheme.SPACING_SM));
        banner.setPreferredSize(new Dimension(0, 60));

        JLabel welcomeLabel = new JLabel("Welcome, " + student.getRollNo());
        welcomeLabel.setFont(UITheme.FONT_SUBHEADER);
        welcomeLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel infoLabel = new JLabel("  •  " + student.getProgram() + ", Year " + student.getYear());
        infoLabel.setFont(UITheme.FONT_BODY);
        infoLabel.setForeground(new Color(255, 255, 255, 200));

        banner.add(welcomeLabel);
        banner.add(infoLabel);

        return banner;
    }

    private void showChangePasswordDialog() {
        new edu.univ.erp.ui.common.ChangePasswordDialog(this).setVisible(true);
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"Code", "Title", "Credits", "Section", "Instructor", "Day/Time", "Room", "Seats"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        catalogTable = new JTable(model);
        UITheme.styleTable(catalogTable);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton registerButton = new JButton("Register for Selected Section");
        UITheme.stylePrimaryButton(registerButton);
        registerButton.addActionListener(e -> registerForSection());

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        UITheme.styleScrollPane(scrollPane);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(registerButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"Code", "Title", "Section", "Semester", "Day/Time", "Room", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        enrollmentsTable = new JTable(model);
        UITheme.styleTable(enrollmentsTable);
        enrollmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton dropButton = new JButton("Drop Selected Section");
        dropButton.setBackground(UITheme.ERROR);
        dropButton.setForeground(UITheme.TEXT_PRIMARY);
        dropButton.setFont(UITheme.FONT_BODY_BOLD);
        dropButton.setFocusPainted(false);
        dropButton.setBorderPainted(false);
        dropButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dropButton.addActionListener(e -> dropSection());

        JButton refreshButton = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> loadEnrollments());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACING_SM, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(dropButton);
        buttonPanel.add(refreshButton);

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        UITheme.styleScrollPane(scrollPane);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(model);
        UITheme.styleTable(timetableTable);
        timetableTable.setRowHeight(70);
        timetableTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setVerticalAlignment(JLabel.TOP);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UITheme.TABLE_ROW_EVEN : UITheme.TABLE_ROW_ODD);
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                return c;
            }
        };
        for (int i = 0; i < timetableTable.getColumnCount(); i++) {
            timetableTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        timetableTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        for (int i = 1; i < timetableTable.getColumnCount(); i++) {
            timetableTable.getColumnModel().getColumn(i).setPreferredWidth(140);
        }

        JButton refreshButton = new JButton("↻ Refresh Timetable");
        UITheme.stylePrimaryButton(refreshButton);
        refreshButton.addActionListener(e -> loadTimetable());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshButton);

        JLabel infoLabel = new JLabel("  Showing your registered sections organized by day and time");
        infoLabel.setFont(UITheme.FONT_SMALL);
        infoLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(infoLabel, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        UITheme.styleScrollPane(scrollPane);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"Course Code", "Course Title", "Section", "Semester", "Final Grade", "Percentage"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradesTable = new JTable(model);
        UITheme.styleTable(gradesTable);
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gradesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showCourseGradeDetails();
                }
            }
        });

        JButton viewDetailsButton = new JButton("View Grade Details");
        UITheme.styleSecondaryButton(viewDetailsButton);
        viewDetailsButton.addActionListener(e -> showCourseGradeDetails());

        JButton refreshButton = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> loadGrades());

        JButton exportCSVButton = new JButton("Export CSV");
        UITheme.stylePrimaryButton(exportCSVButton);
        exportCSVButton.addActionListener(e -> exportTranscriptCSV());

        JButton exportPDFButton = new JButton("Export PDF");
        UITheme.stylePrimaryButton(exportPDFButton);
        exportPDFButton.addActionListener(e -> exportTranscriptPDF());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACING_SM, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportCSVButton);
        buttonPanel.add(exportPDFButton);

        JLabel hintLabel = new JLabel("  Double-click on a course to view grade component details");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(hintLabel, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(gradesTable);
        UITheme.styleScrollPane(scrollPane);

        JPanel cgpaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACING_LG, UITheme.SPACING_SM));
        cgpaPanel.setBackground(UITheme.PRIMARY);
        cgpaPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, UITheme.SECONDARY),
            BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_LG, UITheme.SPACING_MD, UITheme.SPACING_LG)
        ));

        JLabel cgpaTitleLabel = new JLabel("Overall Academic Performance:");
        cgpaTitleLabel.setFont(UITheme.FONT_BODY_BOLD);
        cgpaTitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        cgpaLabel = new JLabel("Loading...");
        cgpaLabel.setFont(new Font("Roboto", Font.BOLD, 20));
        cgpaLabel.setForeground(UITheme.ACCENT);

        JLabel avgTitleLabel = new JLabel("CGPA:");
        avgTitleLabel.setFont(UITheme.FONT_BODY_BOLD);
        avgTitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        cgpaPanel.add(cgpaTitleLabel);
        cgpaPanel.add(avgTitleLabel);
        cgpaPanel.add(cgpaLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(cgpaPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadData() {
        loadCatalog();
        loadEnrollments();
        loadTimetable();
        loadGrades();
    }

    private void loadCatalog() {
        DefaultTableModel model = (DefaultTableModel) catalogTable.getModel();
        model.setRowCount(0);

        List<Section> sections = studentService.getAvailableSections();
        for (Section section : sections) {
            String seats = section.getAvailableSeats() + " / " + section.getCapacity();
            model.addRow(new Object[]{
                section.getCourseCode(),
                section.getCourseTitle(),
                section.getCourseCredits(),
                section.getSectionCode(),
                section.getInstructorName(),
                section.getDayTime(),
                section.getRoom(),
                seats
            });
        }
    }

    private void loadEnrollments() {
        DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
        model.setRowCount(0);

        List<Enrollment> enrollments = studentService.getEnrollments(student.getStudentId());
        for (Enrollment enrollment : enrollments) {
            model.addRow(new Object[]{
                enrollment.getCourseCode(),
                enrollment.getCourseTitle(),
                enrollment.getSectionCode(),
                enrollment.getSemester() + " " + enrollment.getYear(),
                enrollment.getDayTime(),
                enrollment.getRoom(),
                enrollment.getStatus()
            });
        }
    }

    private void loadTimetable() {
        DefaultTableModel model = (DefaultTableModel) timetableTable.getModel();
        model.setRowCount(0);

        List<Enrollment> enrollments = studentService.getActiveEnrollments(student.getStudentId());

        Set<String> uniqueTimeSlots = new TreeSet<>((a, b) -> {

            try {
                int aStart = UITheme.parseTimeToMinutes(a.split(" - ")[0].trim());
                int bStart = UITheme.parseTimeToMinutes(b.split(" - ")[0].trim());
                return Integer.compare(aStart, bStart);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });

        for (Enrollment enrollment : enrollments) {
            String dayTime = enrollment.getDayTime();
            if (dayTime == null || dayTime.isEmpty()) continue;

            java.util.List<String> entries = UITheme.parseMultipleDayTimes(dayTime);
            for (String entry : entries) {
                String timePart = extractTimePart(entry);
                if (timePart != null && !timePart.isEmpty()) {

                    String formatted = formatTimeSlotForDisplay(timePart);
                    if (formatted != null) {
                        uniqueTimeSlots.add(formatted);
                    }
                }
            }
        }

        if (uniqueTimeSlots.isEmpty()) {
            String[] defaultSlots = {
                "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
                "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00",
                "16:00 - 17:00", "17:00 - 18:00"
            };
            for (String slot : defaultSlots) {
                uniqueTimeSlots.add(slot);
            }
        }

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Map<String, Integer> dayIndex = new HashMap<>();
        for (int i = 0; i < days.length; i++) {
            dayIndex.put(days[i].toLowerCase(), i + 1);
            dayIndex.put(days[i].substring(0, 3).toLowerCase(), i + 1);
        }

        Map<String, String[]> timetableGrid = new LinkedHashMap<>();
        for (String slot : uniqueTimeSlots) {
            timetableGrid.put(slot, new String[]{"", "", "", "", "", ""});
        }

        for (Enrollment enrollment : enrollments) {
            String dayTime = enrollment.getDayTime();
            if (dayTime == null || dayTime.isEmpty()) {
                continue;
            }

            String courseCode = escapeHtml(enrollment.getCourseCode());
            String sectionCode = escapeHtml(enrollment.getSectionCode());
            String room = enrollment.getRoom() != null ? escapeHtml(enrollment.getRoom()) : "";
            String courseInfo = String.format("<html><b>%s</b><br>%s<br>%s</html>",
                    courseCode, sectionCode, room);

            java.util.List<String> entries = UITheme.parseMultipleDayTimes(dayTime);
            for (String entry : entries) {
                parseSingleDayTimeEntry(entry.trim(), courseInfo, dayIndex, timetableGrid);
            }
        }

        for (Map.Entry<String, String[]> entry : timetableGrid.entrySet()) {
            String[] row = new String[7];
            row[0] = entry.getKey();
            String[] daySlots = entry.getValue();
            System.arraycopy(daySlots, 0, row, 1, daySlots.length);
            model.addRow(row);
        }
    }

    private String extractTimePart(String entry) {
        if (entry == null || entry.isEmpty()) return null;

        int timeStart = -1;
        for (int i = 0; i < entry.length(); i++) {
            if (Character.isDigit(entry.charAt(i))) {
                timeStart = i;
                break;
            }
        }

        if (timeStart > 0) {
            return entry.substring(timeStart).trim();
        }
        return null;
    }

    private String formatTimeSlotForDisplay(String timeRange) {
        if (timeRange == null || !timeRange.contains("-")) return null;
        String[] parts = timeRange.split("-");
        if (parts.length != 2) return null;
        return parts[0].trim() + " - " + parts[1].trim();
    }

    private void parseSingleDayTimeEntry(String entry, String courseInfo,
            Map<String, Integer> dayIndex, Map<String, String[]> timetableGrid) {
        if (entry == null || entry.isEmpty()) return;

        String dayPart = "";
        String timePart = "";

        int timeStart = -1;
        for (int i = 0; i < entry.length(); i++) {
            if (Character.isDigit(entry.charAt(i))) {
                timeStart = i;
                break;
            }
        }

        if (timeStart > 0) {
            dayPart = entry.substring(0, timeStart).trim();
            timePart = entry.substring(timeStart).trim();
        } else {
            dayPart = entry;
        }

        Integer dayIdx = null;
        String dayPartLower = dayPart.toLowerCase().replaceAll("[,\\s]+", "");

        if (dayPartLower.contains("mwf") || dayPartLower.equals("monwedfri")) {

            parseSingleDayTimeEntry("Mon " + timePart, courseInfo, dayIndex, timetableGrid);
            parseSingleDayTimeEntry("Wed " + timePart, courseInfo, dayIndex, timetableGrid);
            parseSingleDayTimeEntry("Fri " + timePart, courseInfo, dayIndex, timetableGrid);
            return;
        } else if (dayPartLower.contains("tth") || dayPartLower.equals("tuethu")) {
            parseSingleDayTimeEntry("Tue " + timePart, courseInfo, dayIndex, timetableGrid);
            parseSingleDayTimeEntry("Thu " + timePart, courseInfo, dayIndex, timetableGrid);
            return;
        }

        dayIdx = dayIndex.get(dayPartLower);
        if (dayIdx == null) {

            for (Map.Entry<String, Integer> e : dayIndex.entrySet()) {
                if (e.getKey().startsWith(dayPartLower) || dayPartLower.startsWith(e.getKey())) {
                    dayIdx = e.getValue();
                    break;
                }
            }
        }

        if (dayIdx == null || timePart.isEmpty()) return;

        String formattedTime = formatTimeSlotForDisplay(timePart);
        if (formattedTime == null) return;

        String[] slots = timetableGrid.get(formattedTime);
        if (slots != null && dayIdx >= 1 && dayIdx <= 6) {
            if (slots[dayIdx - 1].isEmpty()) {
                slots[dayIdx - 1] = courseInfo;
            } else {
                slots[dayIdx - 1] += "<br>" + courseInfo;
            }
        }
    }

    private String findMatchingTimeSlot(String timePart, Set<String> availableSlots) {
        if (timePart == null || timePart.isEmpty()) {
            return null;
        }

        String[] timeParts = timePart.split("-");
        String startTime = timeParts.length > 0 ? timeParts[0].trim() : timePart.trim();

        for (String slot : availableSlots) {
            String[] slotParts = slot.split("-");
            String slotStart = slotParts.length > 0 ? slotParts[0].trim() : slot.trim();
            if (slotStart.equals(startTime) ||
                normalizeTime(slotStart).equals(normalizeTime(startTime))) {
                return slot;
            }
        }

        return null;
    }

    private String normalizeTime(String time) {
        if (time == null) return "";

        time = time.trim().replaceAll("\\s+", "");
        if (time.length() == 4 && time.charAt(1) == ':') {
            time = "0" + time;
        }
        return time;
    }

    private void loadGrades() {
        DefaultTableModel model = (DefaultTableModel) gradesTable.getModel();
        model.setRowCount(0);

        currentEnrollments = studentService.getEnrollments(student.getStudentId());

        for (Enrollment enrollment : currentEnrollments) {
            List<Grade> grades = studentService.getGrades(enrollment.getEnrollmentId());

            String finalGrade;
            String percentage;

            if (grades.isEmpty()) {
                finalGrade = "No grades";
                percentage = "-";
            } else {
                String formatted = studentService.getFormattedFinalGrade(enrollment.getEnrollmentId());
                String[] parsed = parseGradeString(formatted);
                finalGrade = parsed[0];
                percentage = parsed[1];
            }

            model.addRow(new Object[]{
                enrollment.getCourseCode(),
                enrollment.getCourseTitle(),
                enrollment.getSectionCode(),
                enrollment.getSemester() + " " + enrollment.getYear(),
                finalGrade,
                percentage
            });
        }

        updateCGPADisplay();
    }

    private void updateCGPADisplay() {
        if (cgpaLabel != null) {
            String formattedCGPA = studentService.getFormattedCGPA(student.getStudentId());
            cgpaLabel.setText(formattedCGPA);

            BigDecimal cgpa = studentService.calculateCGPA(student.getStudentId());
            if (cgpa != null) {
                if (cgpa.compareTo(new BigDecimal("8.0")) >= 0) {
                    cgpaLabel.setForeground(UITheme.SUCCESS);
                } else if (cgpa.compareTo(new BigDecimal("6.0")) >= 0) {
                    cgpaLabel.setForeground(UITheme.ACCENT);
                } else if (cgpa.compareTo(new BigDecimal("4.0")) >= 0) {
                    cgpaLabel.setForeground(UITheme.WARNING);
                } else {
                    cgpaLabel.setForeground(UITheme.ERROR);
                }
            } else {
                cgpaLabel.setForeground(UITheme.TEXT_SECONDARY);
            }
        }
    }

    private String[] parseGradeString(String gradeStr) {
        if (gradeStr == null || gradeStr.equals("N/A") || !gradeStr.contains(" ") || !gradeStr.contains("(")) {
            return new String[]{"N/A", "-"};
        }
        try {
            String[] parts = gradeStr.split(" ");
            if (parts.length == 0) {
                return new String[]{"N/A", "-"};
            }
            String letterGrade = parts[0];
            int openParen = gradeStr.indexOf("(");
            int closeParen = gradeStr.indexOf(")");
            if (openParen >= 0 && closeParen > openParen) {
                String percentage = gradeStr.substring(openParen + 1, closeParen);
                return new String[]{letterGrade, percentage};
            }
        } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {

        }
        return new String[]{"N/A", "-"};
    }

    private void showCourseGradeDetails() {
        int selectedRow = gradesTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageUtil.showWarning(this, "Please select a course to view grade details.");
            return;
        }

        if (currentEnrollments == null || selectedRow >= currentEnrollments.size()) {
            MessageUtil.showWarning(this, "Invalid selection. Please refresh and try again.");
            return;
        }

        Enrollment enrollment = currentEnrollments.get(selectedRow);
        List<Grade> grades = studentService.getGrades(enrollment.getEnrollmentId());

        JDialog dialog = new JDialog(this, "Grade Details - " + enrollment.getCourseCode(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new MigLayout("wrap 2", "[][grow,fill]", "[]5[]5[]5[]"));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Course Information"));
        infoPanel.add(new JLabel("Course Code:"));
        infoPanel.add(new JLabel(enrollment.getCourseCode()));
        infoPanel.add(new JLabel("Course Title:"));
        infoPanel.add(new JLabel(enrollment.getCourseTitle()));
        infoPanel.add(new JLabel("Section:"));
        infoPanel.add(new JLabel(enrollment.getSectionCode()));
        infoPanel.add(new JLabel("Semester:"));
        infoPanel.add(new JLabel(enrollment.getSemester() + " " + enrollment.getYear()));

        String finalGradeStr = studentService.getFormattedFinalGrade(enrollment.getEnrollmentId());
        JLabel finalGradeLabel = new JLabel(finalGradeStr);
        finalGradeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        finalGradeLabel.setForeground(new Color(0, 100, 0));
        infoPanel.add(new JLabel("Final Grade:"));
        infoPanel.add(finalGradeLabel);

        String[] columns = {"Component", "Score", "Max Score", "Weightage (%)", "Percentage"};
        DefaultTableModel detailsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable detailsTable = new JTable(detailsModel);

        if (grades.isEmpty()) {
            detailsModel.addRow(new Object[]{"No grades entered yet", "-", "-", "-", "-"});
        } else {
            for (Grade grade : grades) {
                String percentage = "-";
                if (grade.getScore() != null && grade.getMaxScore() != null
                        && grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pct = grade.getScore()
                            .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
                    percentage = pct + "%";
                }
                detailsModel.addRow(new Object[]{
                    grade.getComponent(),
                    grade.getScore(),
                    grade.getMaxScore(),
                    grade.getWeightage(),
                    percentage
                });
            }
        }

        JScrollPane tableScroll = new JScrollPane(detailsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Grade Components"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void registerForSection() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageUtil.showWarning(this, "Please select a section to register.");
            return;
        }

        List<Section> sections = studentService.getAvailableSections();
        if (selectedRow >= sections.size()) {
            MessageUtil.showError(this, "Invalid selection.");
            return;
        }

        Section section = sections.get(selectedRow);
        String message = studentService.registerForSection(student.getStudentId(), section.getSectionId());

        if (message == null) {
            MessageUtil.showSuccess(this, "Successfully registered for " + section.getCourseCode());
            loadCatalog();
            loadEnrollments();
        } else {
            MessageUtil.showError(this, message);
        }
    }

    private void dropSection() {
        int selectedRow = enrollmentsTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageUtil.showWarning(this, "Please select a section to drop.");
            return;
        }

        if (!MessageUtil.showConfirmation(this, "Are you sure you want to drop this section?")) {
            return;
        }

        List<Enrollment> enrollments = studentService.getEnrollments(student.getStudentId());
        if (selectedRow >= enrollments.size()) {
            MessageUtil.showError(this, "Invalid selection.");
            return;
        }

        Enrollment enrollment = enrollments.get(selectedRow);
        String message = studentService.dropSection(enrollment.getEnrollmentId());

        if (message == null) {
            MessageUtil.showSuccess(this, "Successfully dropped section.");
            loadCatalog();
            loadEnrollments();
        } else {
            MessageUtil.showError(this, message);
        }
    }

    private void exportTranscriptCSV() {
        List<Enrollment> enrollments = studentService.getEnrollments(student.getStudentId());
        if (enrollments.isEmpty()) {
            MessageUtil.showInfo(this, "No enrollments to export.");
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(student.getRollNo() + "_transcript.csv"));

        if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {

                writer.println("STUDENT TRANSCRIPT");
                writer.println("Student: " + student.getRollNo());
                writer.println("Program: " + student.getProgram());
                writer.println("Year: " + student.getYear());
                writer.println();
                writer.println("Course Code,Course Title,Section,Semester,Final Grade,Percentage");

                for (Enrollment enrollment : enrollments) {
                    String finalGrade = studentService.calculateFinalGrade(enrollment.getEnrollmentId());
                    java.math.BigDecimal finalPercent = studentService.calculateFinalPercentage(enrollment.getEnrollmentId());
                    String percentStr = finalPercent != null ? String.format("%.2f%%", finalPercent) : "N/A";

                    writer.printf("%s,%s,%s,%s %d,%s,%s%n",
                        enrollment.getCourseCode(),
                        enrollment.getCourseTitle(),
                        enrollment.getSectionCode(),
                        enrollment.getSemester(),
                        enrollment.getYear(),
                        finalGrade,
                        percentStr);
                }

                writer.println();
                writer.println("OVERALL ACADEMIC PERFORMANCE");
                String cgpaStr = studentService.getFormattedCGPA(student.getStudentId());
                writer.println("CGPA: " + cgpaStr);

                java.math.BigDecimal avgPercent = studentService.calculateAveragePercentage(student.getStudentId());
                if (avgPercent != null) {
                    writer.println("Average Percentage: " + String.format("%.2f%%", avgPercent));
                }

                MessageUtil.showSuccess(this, "Transcript exported successfully to: " + fileChooser.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                MessageUtil.showError(this, "Failed to export transcript: " + e.getMessage());
            }
        }
    }

    private void exportTranscriptPDF() {
        List<Enrollment> enrollments = studentService.getEnrollments(student.getStudentId());
        if (enrollments.isEmpty()) {
            MessageUtil.showInfo(this, "No enrollments to export.");
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(student.getRollNo() + "_transcript.pdf"));

        if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                com.lowagie.text.Document document = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fileChooser.getSelectedFile()));
                document.open();

                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);
                com.lowagie.text.Font cgpaFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);

                document.add(new com.lowagie.text.Paragraph("STUDENT TRANSCRIPT", titleFont));
                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("Student: " + student.getRollNo(), headerFont));
                document.add(new com.lowagie.text.Paragraph("Program: " + student.getProgram(), normalFont));
                document.add(new com.lowagie.text.Paragraph("Year: " + student.getYear(), normalFont));
                document.add(new com.lowagie.text.Paragraph(" "));

                com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
                table.setWidthPercentage(100);

                table.addCell(new com.lowagie.text.Phrase("Course", headerFont));
                table.addCell(new com.lowagie.text.Phrase("Section", headerFont));
                table.addCell(new com.lowagie.text.Phrase("Semester", headerFont));
                table.addCell(new com.lowagie.text.Phrase("Grade", headerFont));
                table.addCell(new com.lowagie.text.Phrase("Percentage", headerFont));

                for (Enrollment enrollment : enrollments) {
                    String finalGrade = studentService.calculateFinalGrade(enrollment.getEnrollmentId());
                    java.math.BigDecimal finalPercent = studentService.calculateFinalPercentage(enrollment.getEnrollmentId());
                    String percentStr = finalPercent != null ? String.format("%.2f%%", finalPercent) : "N/A";

                    table.addCell(new com.lowagie.text.Phrase(enrollment.getCourseCode() + " - " + enrollment.getCourseTitle(), normalFont));
                    table.addCell(new com.lowagie.text.Phrase(enrollment.getSectionCode(), normalFont));
                    table.addCell(new com.lowagie.text.Phrase(enrollment.getSemester() + " " + enrollment.getYear(), normalFont));
                    table.addCell(new com.lowagie.text.Phrase(finalGrade, normalFont));
                    table.addCell(new com.lowagie.text.Phrase(percentStr, normalFont));
                }

                document.add(table);

                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("OVERALL ACADEMIC PERFORMANCE", headerFont));
                String cgpaStr = studentService.getFormattedCGPA(student.getStudentId());
                document.add(new com.lowagie.text.Paragraph("CGPA: " + cgpaStr, cgpaFont));

                java.math.BigDecimal avgPercent = studentService.calculateAveragePercentage(student.getStudentId());
                if (avgPercent != null) {
                    document.add(new com.lowagie.text.Paragraph("Average Percentage: " + String.format("%.2f%%", avgPercent), normalFont));
                }

                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("Grade Scale:", headerFont));
                document.add(new com.lowagie.text.Paragraph("A+ (95-100%) | A (90-94.99%) | A- (85-89.99%) | B+ (80-84.99%) | B (75-79.99%)", normalFont));
                document.add(new com.lowagie.text.Paragraph("B- (70-74.99%) | C+ (65-69.99%) | C (60-64.99%) | C- (55-59.99%) | D (50-54.99%) | F (<50%)", normalFont));

                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("CGPA Scale (10-point):", headerFont));
                document.add(new com.lowagie.text.Paragraph("A+ = 10.0 | A = 9.0 | A- = 8.5 | B+ = 8.0 | B = 7.0 | B- = 6.5", normalFont));
                document.add(new com.lowagie.text.Paragraph("C+ = 6.0 | C = 5.0 | C- = 4.5 | D = 4.0 | F = 0.0", normalFont));

                document.close();

                MessageUtil.showSuccess(this, "Transcript exported successfully to: " + fileChooser.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                MessageUtil.showError(this, "Failed to export transcript: " + e.getMessage());
            }
        }
    }

    private void logout() {
        if (MessageUtil.showConfirmation(this, "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
