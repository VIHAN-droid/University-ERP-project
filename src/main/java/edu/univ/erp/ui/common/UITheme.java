package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {

    public static final Color PRIMARY = new Color(0x0A, 0x19, 0x29);
    public static final Color SECONDARY = new Color(0x21, 0x96, 0xF3);
    public static final Color ACCENT = new Color(0x00, 0xBC, 0xD4);

    public static final Color BACKGROUND = new Color(0x1E, 0x1E, 0x1E);
    public static final Color SURFACE = new Color(0x26, 0x32, 0x38);
    public static final Color SURFACE_LIGHT = new Color(0x2D, 0x3A, 0x42);
    public static final Color SURFACE_DARK = new Color(0x1A, 0x1A, 0x1A);

    public static final Color TEXT_PRIMARY = new Color(0xFF, 0xFF, 0xFF);
    public static final Color TEXT_SECONDARY = new Color(0xB0, 0xBE, 0xC5);
    public static final Color TEXT_DISABLED = new Color(0x6B, 0x6B, 0x6B);

    public static final Color SUCCESS = new Color(0x4C, 0xAF, 0x50);
    public static final Color WARNING = new Color(0xFF, 0xC1, 0x07);
    public static final Color ERROR = new Color(0xF4, 0x43, 0x36);

    public static final Color TABLE_HEADER = SECONDARY;
    public static final Color TABLE_ROW_EVEN = new Color(0x2D, 0x2D, 0x2D);
    public static final Color TABLE_ROW_ODD = new Color(0x24, 0x24, 0x24);
    public static final Color TABLE_HOVER = new Color(0x21, 0x96, 0xF3, 25);
    public static final Color TABLE_SELECTED = new Color(0x21, 0x96, 0xF3, 50);

    public static final Color BUTTON_PRIMARY_START = SECONDARY;
    public static final Color BUTTON_PRIMARY_END = ACCENT;
    public static final Color BUTTON_SECONDARY = new Color(0x3A, 0x3A, 0x3A);

    public static final Color BORDER_DEFAULT = new Color(0x3A, 0x3A, 0x3A);
    public static final Color BORDER_FOCUS = SECONDARY;

    public static final Font FONT_TITLE = new Font("Roboto", Font.BOLD, 28);
    public static final Font FONT_HEADER = new Font("Roboto", Font.BOLD, 24);
    public static final Font FONT_SUBHEADER = new Font("Roboto", Font.PLAIN, 18);
    public static final Font FONT_BODY = new Font("Inter", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Inter", Font.BOLD, 14);
    public static final Font FONT_CAPTION = new Font("Inter", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("Inter", Font.PLAIN, 11);

    public static final int BORDER_RADIUS = 8;
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;
    public static final int SPACING_XL = 32;
    public static final int SPACING_XXL = 48;

    public static final int SIDEBAR_WIDTH = 240;
    public static final int BUTTON_HEIGHT = 40;
    public static final int INPUT_HEIGHT = 44;

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
        panel.setForeground(TEXT_PRIMARY);
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setForeground(TEXT_PRIMARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
            BorderFactory.createEmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD)
        ));
    }

    public static void stylePrimaryButton(JButton button) {
        stylePrimaryButtonBase(button);
    }

    public static void stylePrimaryButton(JToggleButton button) {
        stylePrimaryButtonBase(button);
    }

    private static void stylePrimaryButtonBase(AbstractButton button) {
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(SECONDARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, BUTTON_HEIGHT));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY);
            }
        });
    }

    public static void styleSecondaryButton(JButton button) {
        styleSecondaryButtonBase(button);
    }

    public static void styleSecondaryButton(JToggleButton button) {
        styleSecondaryButtonBase(button);
    }

    private static void styleSecondaryButtonBase(AbstractButton button) {
        button.setFont(FONT_BODY);
        button.setForeground(SECONDARY);
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(SECONDARY, 1));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, BUTTON_HEIGHT));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x21, 0x96, 0xF3, 30));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SURFACE);
            }
        });
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(SURFACE_DARK);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
            BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, INPUT_HEIGHT));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_FOCUS, 2),
                    BorderFactory.createEmptyBorder(SPACING_SM - 1, SPACING_MD - 1, SPACING_SM - 1, SPACING_MD - 1)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
                    BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
                ));
            }
        });
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(SURFACE_DARK);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
            BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, INPUT_HEIGHT));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_FOCUS, 2),
                    BorderFactory.createEmptyBorder(SPACING_SM - 1, SPACING_MD - 1, SPACING_SM - 1, SPACING_MD - 1)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
                    BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
                ));
            }
        });
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(FONT_BODY);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(SURFACE_DARK);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT, 1));
    }

    public static void styleTitleLabel(JLabel label) {
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
    }

    public static void styleHeaderLabel(JLabel label) {
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_PRIMARY);
    }

    public static void styleBodyLabel(JLabel label) {
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
    }

    public static void styleSecondaryLabel(JLabel label) {
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(SURFACE);
        table.setSelectionBackground(TABLE_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_DEFAULT);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setForeground(TEXT_PRIMARY);
        header.setBackground(TABLE_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(TABLE_SELECTED);
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, SPACING_SM, 0, SPACING_SM));
                return c;
            }
        });
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(BACKGROUND);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT, 1));
    }

    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setFont(FONT_BODY);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setBackground(BACKGROUND);
    }

    public static JLabel createMaintenanceBanner() {
        JLabel banner = new JLabel("⚠ MAINTENANCE MODE - Read-Only Access ⚠");
        banner.setFont(FONT_BODY_BOLD);
        banner.setForeground(PRIMARY);
        banner.setBackground(WARNING);
        banner.setOpaque(true);
        banner.setHorizontalAlignment(SwingConstants.CENTER);
        banner.setBorder(BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD));
        return banner;
    }

    public static JLabel createAddDropClosedBanner() {
        JLabel banner = new JLabel("🔒 ADD/DROP PERIOD CLOSED - Registration and course drops are not allowed");
        banner.setFont(FONT_BODY_BOLD);
        banner.setForeground(TEXT_PRIMARY);
        banner.setBackground(ERROR);
        banner.setOpaque(true);
        banner.setHorizontalAlignment(SwingConstants.CENTER);
        banner.setBorder(BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD));
        return banner;
    }

    public static JLabel createSuccessLabel(String text) {
        JLabel label = new JLabel("✓ " + text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
        label.setBackground(SUCCESS);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD));
        return label;
    }

    public static Border createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_BODY_BOLD,
            TEXT_SECONDARY
        );
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
            BorderFactory.createEmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD)
        );
    }

    public static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    public static final String[] DAYS_SHORT = {
        "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    public static final String[] DAY_COMBINATIONS = {
        "MWF", "TTh", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
        "Mon,Wed", "Tue,Thu", "Mon,Wed,Fri", "Tue,Thu,Sat"
    };

    public static final String[] TIME_SLOTS = {
        "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
        "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
        "16:00-17:00", "17:00-18:00"
    };

    public static String formatDayTime(String days, String time) {
        if (days == null || days.isEmpty()) return time;
        if (time == null || time.isEmpty()) return days;
        return days + " " + time;
    }

    public static String formatMultipleDayTimes(java.util.List<String> entries) {
        if (entries == null || entries.isEmpty()) return "";
        return String.join("; ", entries);
    }

    public static java.util.List<String> parseMultipleDayTimes(String combined) {
        java.util.List<String> entries = new java.util.ArrayList<>();
        if (combined == null || combined.isEmpty()) return entries;
        String[] parts = combined.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    public static boolean isValidTime(String time) {
        if (time == null || time.isEmpty()) return false;
        return time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    public static boolean isValidTimeRange(String timeRange) {
        if (timeRange == null || timeRange.isEmpty()) return false;
        if (!timeRange.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            return false;
        }
        String[] parts = timeRange.split("-");
        if (parts.length != 2) return false;
        String startTime = parts[0].trim();
        String endTime = parts[1].trim();

        try {
            int startMinutes = parseTimeToMinutes(startTime);
            int endMinutes = parseTimeToMinutes(endTime);
            return endMinutes > startMinutes;
        } catch (Exception e) {
            return false;
        }
    }

    public static int parseTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    public static String shortToFullDayName(String shortName) {
        switch (shortName.toLowerCase()) {
            case "mon": return "Monday";
            case "tue": return "Tuesday";
            case "wed": return "Wednesday";
            case "thu": return "Thursday";
            case "fri": return "Friday";
            case "sat": return "Saturday";
            case "sun": return "Sunday";
            default: return shortName;
        }
    }

    public static String fullToShortDayName(String fullName) {
        switch (fullName.toLowerCase()) {
            case "monday": return "Mon";
            case "tuesday": return "Tue";
            case "wednesday": return "Wed";
            case "thursday": return "Thu";
            case "friday": return "Fri";
            case "saturday": return "Sat";
            case "sunday": return "Sun";
            default: return fullName.length() > 3 ? fullName.substring(0, 3) : fullName;
        }
    }

    private UITheme() {

    }
}
