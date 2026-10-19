
-- =====================================================
-- Student Task Manager
-- Database setup script
-- MySQL 8
--
-- This script creates the database and tasks table.
-- It does not delete existing tasks.
-- =====================================================

CREATE DATABASE IF NOT EXISTS student_task_manager;

USE student_task_manager;

-- Create the tasks table if it does not already exist.
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    due_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Display the database and tables for verification.
SELECT DATABASE() AS current_database;

SHOW TABLES;

-- Display the existing tasks.
SELECT
    id,
    title,
    description,
    due_date,
    completed,
    created_at
FROM tasks
ORDER BY id DESC;
