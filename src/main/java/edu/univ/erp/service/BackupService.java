package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private final AccessControl accessControl;

    private static final String DEFAULT_BACKUP_DIR = "backups";
    private String backupDirectory;

    public BackupService() {
        this.accessControl = AccessControl.getInstance();
        this.backupDirectory = DEFAULT_BACKUP_DIR;
        ensureBackupDirectoryExists();
    }

    public BackupService(String customBackupDir) {
        this.accessControl = AccessControl.getInstance();
        this.backupDirectory = customBackupDir;
        ensureBackupDirectoryExists();
    }

    private void ensureBackupDirectoryExists() {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                logger.info("Created backup directory: {}", backupPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create backup directory", e);
        }
    }

    public BackupResult createBackup() {
        if (!accessControl.isAdmin()) {
            return new BackupResult(false, null, "Access denied. Only admins can create backups.");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = "erp_backup_" + timestamp + ".sql";
        Path backupFilePath = Paths.get(backupDirectory, backupFileName);

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFilePath.toFile()))) {

            writer.println("-- ERP Database Backup");
            writer.println("-- Created: " + new Date());
            writer.println("-- This file can be used to restore the ERP database");
            writer.println();
            writer.println("SET FOREIGN_KEY_CHECKS = 0;");
            writer.println();

            List<String> tables = getTableNames(conn);

            for (String tableName : tables) {

                if (tableName.startsWith("_") || tableName.equalsIgnoreCase("settings")) {

                    if (!tableName.equalsIgnoreCase("settings")) {
                        continue;
                    }
                }

                writer.println("-- Table: " + tableName);
                writer.println();

                exportTableStructure(conn, tableName, writer);

                exportTableData(conn, tableName, writer);

                writer.println();
            }

            writer.println("SET FOREIGN_KEY_CHECKS = 1;");
            writer.println();
            writer.println("-- End of backup");

            logger.info("Backup created successfully: {}", backupFilePath.toAbsolutePath());
            return new BackupResult(true, backupFilePath.toAbsolutePath().toString(), "Backup created successfully.");

        } catch (SQLException e) {
            logger.error("Database error during backup", e);
            return new BackupResult(false, null, "Database error: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during backup", e);
            return new BackupResult(false, null, "File error: " + e.getMessage());
        }
    }

    private List<String> getTableNames(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    private boolean isValidTableName(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }
        return tableName.matches("^[a-zA-Z0-9_]+$");
    }

    private String escapeIdentifier(String identifier) {

        return "`" + identifier.replace("`", "``") + "`";
    }

    private void exportTableStructure(Connection conn, String tableName, PrintWriter writer) throws SQLException {

        if (!isValidTableName(tableName)) {
            logger.warn("Skipping invalid table name: {}", tableName);
            return;
        }

        if (tableName.equalsIgnoreCase("password_history")) {
            logger.info("Skipping password_history table (belongs to auth_db)");
            return;
        }

        String sql = "SHOW CREATE TABLE " + escapeIdentifier(tableName);
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String createStatement = rs.getString(2);
                writer.println("DROP TABLE IF EXISTS " + escapeIdentifier(tableName) + ";");
                writer.println(createStatement + ";");
                writer.println();
            }
        } catch (SQLException e) {
            logger.warn("Skipping table {} - does not exist in current database", tableName);

        }
    }

    private void exportTableData(Connection conn, String tableName, PrintWriter writer) throws SQLException {

        if (!isValidTableName(tableName) || tableName.equalsIgnoreCase("password_history")) {
            logger.warn("Skipping invalid table name: {}", tableName);
            return;
        }

        String sql = "SELECT * FROM " + escapeIdentifier(tableName);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                StringBuilder insert = new StringBuilder("INSERT INTO " + escapeIdentifier(tableName) + " VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) insert.append(", ");
                    Object value = rs.getObject(i);
                    if (value == null) {
                        insert.append("NULL");
                    } else if (value instanceof Number) {
                        insert.append(value);
                    } else if (value instanceof byte[]) {
                        insert.append("NULL");
                    } else {

                        String strValue = value.toString().replace("'", "''").replace("\\", "\\\\");
                        insert.append("'").append(strValue).append("'");
                    }
                }
                insert.append(");");
                writer.println(insert.toString());
            }
        }
        catch (SQLException e) {
            logger.warn("Skipping table {} - query failed", tableName);
        }
    }

    public BackupResult restoreBackup(String backupFilePath) {
        if (!accessControl.isAdmin()) {
            return new BackupResult(false, null, "Access denied. Only admins can restore backups.");
        }

        Path path = Paths.get(backupFilePath);
        if (!Files.exists(path)) {
            return new BackupResult(false, null, "Backup file not found: " + backupFilePath);
        }

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {

            conn.setAutoCommit(false);

            StringBuilder sqlStatement = new StringBuilder();
            String line;

            try (Statement stmt = conn.createStatement()) {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }

                    sqlStatement.append(line);

                    if (line.endsWith(";")) {
                        String sql = sqlStatement.toString();
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            logger.warn("Error executing SQL during restore (continuing): {}", e.getMessage());

                        }
                        sqlStatement = new StringBuilder();
                    } else {
                        sqlStatement.append(" ");
                    }
                }

                conn.commit();
                logger.info("Database restored successfully from: {}", backupFilePath);
                return new BackupResult(true, backupFilePath, "Database restored successfully.");

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error during restore, rolled back", e);
                return new BackupResult(false, null, "Restore failed and rolled back: " + e.getMessage());
            }

        } catch (SQLException e) {
            logger.error("Database connection error during restore", e);
            return new BackupResult(false, null, "Database error: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading backup file", e);
            return new BackupResult(false, null, "Error reading backup file: " + e.getMessage());
        }
    }

    public List<BackupInfo> getAvailableBackups() {
        List<BackupInfo> backups = new ArrayList<>();

        try {
            Path backupPath = Paths.get(backupDirectory);
            if (Files.exists(backupPath)) {
                Files.list(backupPath)
                    .filter(p -> p.toString().endsWith(".sql"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .forEach(p -> {
                        try {
                            backups.add(new BackupInfo(
                                p.getFileName().toString(),
                                p.toAbsolutePath().toString(),
                                Files.getLastModifiedTime(p).toMillis(),
                                Files.size(p)
                            ));
                        } catch (IOException e) {
                            logger.error("Error reading backup file info", e);
                        }
                    });
            }
        } catch (IOException e) {
            logger.error("Error listing backup files", e);
        }

        return backups;
    }

    public boolean deleteBackup(String backupFilePath) {
        if (!accessControl.isAdmin()) {
            return false;
        }

        try {
            Path path = Paths.get(backupFilePath);

            if (!path.toAbsolutePath().startsWith(Paths.get(backupDirectory).toAbsolutePath())) {
                logger.warn("Attempted to delete file outside backup directory: {}", backupFilePath);
                return false;
            }

            Files.deleteIfExists(path);
            logger.info("Deleted backup file: {}", backupFilePath);
            return true;
        } catch (IOException e) {
            logger.error("Error deleting backup file", e);
            return false;
        }
    }

    public String getBackupDirectory() {
        return Paths.get(backupDirectory).toAbsolutePath().toString();
    }

    public static class BackupResult {
        private final boolean success;
        private final String filePath;
        private final String message;

        public BackupResult(boolean success, String filePath, String message) {
            this.success = success;
            this.filePath = filePath;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getFilePath() { return filePath; }
        public String getMessage() { return message; }
    }

    public static class BackupInfo {
        private final String fileName;
        private final String fullPath;
        private final long timestamp;
        private final long sizeBytes;

        public BackupInfo(String fileName, String fullPath, long timestamp, long sizeBytes) {
            this.fileName = fileName;
            this.fullPath = fullPath;
            this.timestamp = timestamp;
            this.sizeBytes = sizeBytes;
        }

        public String getFileName() { return fileName; }
        public String getFullPath() { return fullPath; }
        public long getTimestamp() { return timestamp; }
        public long getSizeBytes() { return sizeBytes; }

        public String getFormattedDate() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        }

        public String getFormattedSize() {
            if (sizeBytes < 1024) return sizeBytes + " B";
            if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        }

        @Override
        public String toString() {
            return fileName + " (" + getFormattedDate() + ", " + getFormattedSize() + ")";
        }
    }
}
