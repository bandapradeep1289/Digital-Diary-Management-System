CREATE DATABASE IF NOT EXISTS digital_diary;
USE digital_diary;

CREATE TABLE IF NOT EXISTS users (
    user_id  INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS entries (
    entry_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    entry_date DATE NOT NULL,
    title      VARCHAR(150),
    content    TEXT,
    mood       VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
