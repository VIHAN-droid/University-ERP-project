package edu.univ.erp;

import com.formdev.flatlaf.FlatDarkLaf;
import edu.univ.erp.ui.common.LoginFrame;
import edu.univ.erp.ui.common.UITheme;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting University ERP System...");

        try {

            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.arc", UITheme.BORDER_RADIUS);
            UIManager.put("Component.arc", UITheme.BORDER_RADIUS);
            UIManager.put("TextComponent.arc", UITheme.BORDER_RADIUS);
            UIManager.put("TextField.arc", UITheme.BORDER_RADIUS);
            UIManager.put("PasswordField.arc", UITheme.BORDER_RADIUS);
            UIManager.put("ComboBox.arc", UITheme.BORDER_RADIUS);

            UIManager.put("Panel.background", UITheme.BACKGROUND);
            UIManager.put("TextField.background", UITheme.SURFACE_DARK);
            UIManager.put("PasswordField.background", UITheme.SURFACE_DARK);
            UIManager.put("TextArea.background", UITheme.SURFACE_DARK);
            UIManager.put("ComboBox.background", UITheme.SURFACE_DARK);
            UIManager.put("Table.background", UITheme.SURFACE);
            UIManager.put("TableHeader.background", UITheme.SECONDARY);
            UIManager.put("ScrollPane.background", UITheme.BACKGROUND);
            UIManager.put("TabbedPane.background", UITheme.BACKGROUND);
            UIManager.put("TabbedPane.selectedBackground", UITheme.SURFACE);
            UIManager.put("TabbedPane.focusColor", UITheme.SECONDARY);
            UIManager.put("TabbedPane.hoverColor", UITheme.SURFACE_LIGHT);

            UIManager.put("Component.focusColor", UITheme.SECONDARY);
            UIManager.put("Button.default.focusColor", UITheme.SECONDARY);

            UIManager.put("Button.default.background", UITheme.SECONDARY);
            UIManager.put("Button.default.foreground", UITheme.TEXT_PRIMARY);
            UIManager.put("Button.background", UITheme.SURFACE);
            UIManager.put("Button.foreground", UITheme.TEXT_PRIMARY);

            UIManager.put("List.selectionBackground", UITheme.TABLE_SELECTED);
            UIManager.put("Table.selectionBackground", UITheme.TABLE_SELECTED);
            UIManager.put("Tree.selectionBackground", UITheme.TABLE_SELECTED);

            FlatDarkLaf.setup();
        } catch (Exception e) {
            logger.warn("Failed to set FlatLaf look and feel, using default", e);
        }

        SwingUtilities.invokeLater(() -> {
            try {

                DatabaseManager.getInstance();

                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

                logger.info("Application started successfully");
            } catch (Exception e) {
                logger.error("Error starting application", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            DatabaseManager.getInstance().close();
        }));
    }
}
