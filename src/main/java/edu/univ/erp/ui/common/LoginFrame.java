package edu.univ.erp.ui.common;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.LoginResult;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;
import edu.univ.erp.util.MessageUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        passwordField.addActionListener(e -> handleLogin());

        UITheme.styleTextField(usernameField);
        UITheme.stylePasswordField(passwordField);
        UITheme.stylePrimaryButton(loginButton);
    }

    private void setupLayout() {

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());

        JPanel loginCard = createLoginCard();
        mainPanel.add(loginCard);

        setContentPane(mainPanel);
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new MigLayout("fill, insets 40", "[grow,fill]", "[]30[]20[]15[]15[]30[]"));
        card.setBackground(new Color(UITheme.SURFACE.getRed(), UITheme.SURFACE.getGreen(), UITheme.SURFACE.getBlue(), 230));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
            new EmptyBorder(UITheme.SPACING_LG, UITheme.SPACING_XL, UITheme.SPACING_LG, UITheme.SPACING_XL)
        ));
        card.setPreferredSize(new Dimension(400, 480));

        JLabel logoLabel = new JLabel("🎓");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        logoLabel.setForeground(UITheme.SECONDARY);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logoLabel, "center, wrap");

        JLabel titleLabel = new JLabel("University ERP System");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, "center, wrap");

        JPanel usernamePanel = createInputPanel("👤", usernameField, "Username");
        card.add(usernamePanel, "growx, wrap");

        JPanel passwordPanel = createInputPanel("🔒", passwordField, "Password");
        card.add(passwordPanel, "growx, wrap");

        loginButton.setPreferredSize(new Dimension(320, 50));
        loginButton.setFont(UITheme.FONT_SUBHEADER);
        card.add(loginButton, "growx, h 50!, wrap");

        JLabel footerLabel = new JLabel("© 2024 University ERP System");
        footerLabel.setFont(UITheme.FONT_CAPTION);
        footerLabel.setForeground(UITheme.TEXT_SECONDARY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(footerLabel, "center");

        return card;
    }

    private JPanel createInputPanel(String icon, JComponent field, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setForeground(UITheme.TEXT_SECONDARY);
        iconLabel.setPreferredSize(new Dimension(30, 44));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel, BorderLayout.WEST);

        if (field instanceof JTextField) {
            JTextField tf = (JTextField) field;
            tf.putClientProperty("JTextField.placeholderText", placeholder);
        }

        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gradient = new GradientPaint(
                0, 0, UITheme.PRIMARY,
                w, h, new Color(0, 0, 0)
            );

            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, w, h);

            drawHexPattern(g2d, w, h);

            g2d.dispose();
        }

        private void drawHexPattern(Graphics2D g2d, int w, int h) {
            g2d.setColor(new Color(255, 255, 255, 5));
            int size = 40;
            for (int x = -size; x < w + size; x += size * 3) {
                for (int y = -size; y < h + size; y += size * 2) {
                    int offsetX = (y / (size * 2)) % 2 == 0 ? 0 : (int)(size * 1.5);
                    drawHexagon(g2d, x + offsetX, y, size / 2);
                }
            }
        }

        private void drawHexagon(Graphics2D g2d, int cx, int cy, int r) {
            int[] xPoints = new int[6];
            int[] yPoints = new int[6];
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 3 * i - Math.PI / 6;
                xPoints[i] = (int) (cx + r * Math.cos(angle));
                yPoints[i] = (int) (cy + r * Math.sin(angle));
            }
            g2d.drawPolygon(xPoints, yPoints, 6);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            MessageUtil.showError(this, "Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        SwingWorker<LoginResult, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResult doInBackground() {
                return AuthService.getInstance().loginWithResult(username, password);
            }

            @Override
            protected void done() {
                try {
                    LoginResult result = get();
                    if (result.isSuccess()) {
                        SessionManager.getInstance().setCurrentUser(result.getUser());
                        openDashboard(result.getUser());
                    } else {
                        if (result.isAccountLocked()) {
                            MessageUtil.showWarning(LoginFrame.this, result.getErrorMessage());
                        } else {
                            MessageUtil.showError(LoginFrame.this, result.getErrorMessage());
                        }
                        passwordField.setText("");
                    }
                } catch (Exception e) {
                    MessageUtil.showError(LoginFrame.this,
                        "Login error: " + e.getMessage());
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        worker.execute();
    }

    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            JFrame dashboard = null;

            switch (user.getRole()) {
                case ADMIN:
                    dashboard = new AdminDashboard();
                    break;
                case INSTRUCTOR:
                    Instructor instructor = new InstructorDAO().getByUserId(user.getUserId());
                    if (instructor != null) {
                        dashboard = new InstructorDashboard(instructor);
                    }
                    break;
                case STUDENT:
                    Student student = new StudentDAO().getByUserId(user.getUserId());
                    if (student != null) {
                        dashboard = new StudentDashboard(student);
                    }
                    break;
            }

            if (dashboard != null) {
                dashboard.setVisible(true);
                dispose();
            } else {
                MessageUtil.showError(this, "Failed to load user profile.");
                SessionManager.getInstance().logout();
            }
        });
    }
}
