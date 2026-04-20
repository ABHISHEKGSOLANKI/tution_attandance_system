CREATE TABLE students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  student_class VARCHAR(50) NOT NULL,
  encrypted_fingerprint_template TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  attendance_date DATE NOT NULL,
  timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_synced BIT(1) NOT NULL DEFAULT b'0',
  CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT uk_attendance_student_date UNIQUE (student_id, attendance_date)
);

CREATE INDEX idx_attendance_synced_timestamp ON attendance (is_synced, timestamp);
