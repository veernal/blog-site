package com.blogsite.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Backup Service
 * Triggers database backup when record count crosses threshold
 */
@Service
@Slf4j
public class BackupService {
    
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    
    @Value("${spring.datasource.password}")
    private String databasePassword;
    
    @Value("${backup.directory:./backups}")
    private String backupDirectory;
    
    /**
     * Trigger database backup asynchronously
     */
    @Async
    public void triggerBackup() {
        try {
            log.info("Starting database backup...");
            
            // Create backup directory if not exists
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }
            
            // Generate backup filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFileName = String.format("userdb_backup_%s.sql", timestamp);
            String backupFilePath = Paths.get(backupDirectory, backupFileName).toString();
            
            // Extract database name from URL
            String dbName = extractDatabaseName(databaseUrl);
            
            // Execute mysqldump command
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-u" + databaseUsername,
                    "-p" + databasePassword,
                    dbName,
                    "-r",
                    backupFilePath
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Database backup completed successfully: {}", backupFilePath);
            } else {
                log.error("Database backup failed with exit code: {}", exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            log.error("Error during database backup", e);
        }
    }
    
    /**
     * Extract database name from JDBC URL
     */
    private String extractDatabaseName(String jdbcUrl) {
        // Example: jdbc:mysql://localhost:3306/userdb
        int lastSlash = jdbcUrl.lastIndexOf("/");
        int questionMark = jdbcUrl.indexOf("?", lastSlash);
        if (questionMark > 0) {
            return jdbcUrl.substring(lastSlash + 1, questionMark);
        }
        return jdbcUrl.substring(lastSlash + 1);
    }
}
