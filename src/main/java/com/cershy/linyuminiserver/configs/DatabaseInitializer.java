package com.cershy.linyuminiserver.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DatabaseInitializer {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @PostConstruct
    public void initDatabase() {
        try {
            String dbFilePath;
            if (datasourceUrl.startsWith("jdbc:sqlite:")) {
                dbFilePath = datasourceUrl.substring("jdbc:sqlite:".length());
                if (dbFilePath.contains("?")) {
                    dbFilePath = dbFilePath.substring(0, dbFilePath.indexOf("?"));
                }
            } else {
                throw new IllegalArgumentException("Invalid SQLite URL: " + datasourceUrl);
            }
            Path dbPath = Paths.get(dbFilePath);
            // 判断数据库文件是否存在
            if (Files.notExists(dbPath)) {
                System.out.println("Database file does not exist. Creating database...");
                // 创建空的数据库文件
                Files.createFile(dbPath);
            } else {
                System.out.println("Database file already exists. Skipping initialization.");
            }
            // 加载并执行初始化 SQL 文件
            executeSqlFromFile("linyu-mini.sql");
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * 从资源目录加载并执行 SQL 文件
     *
     * @param resourcePath 资源文件路径
     */
    private void executeSqlFromFile(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(resourcePath), StandardCharsets.UTF_8))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }
            String[] sqlStatements = sqlBuilder.toString().split(";");
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    jdbcTemplate.execute(sql.trim());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL file: " + resourcePath, e);
        }
    }
}