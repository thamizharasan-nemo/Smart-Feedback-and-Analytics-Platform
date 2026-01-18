-- Initial baseline migration
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(50),
    username VARCHAR(255),
    roll_no VARCHAR(255),
    created_at DATE,
    identity_no VARCHAR(255),
    CONSTRAINT uk_users_roll_no UNIQUE (roll_no),
    CONSTRAINT uk_users_identity_no UNIQUE (identity_no)
);

CREATE TABLE instructor (
    instructor_id INT AUTO_INCREMENT PRIMARY KEY,
    instructor_name VARCHAR(255),
    avg_rating DOUBLE NOT NULL,
    feedback_count BIGINT NOT NULL,
    is_deleted BIT(1),
    deleted_by VARCHAR(255),
    restored_by VARCHAR(255),
    deleted_at DATETIME(6)
);


CREATE TABLE course (
     course_id INT AUTO_INCREMENT PRIMARY KEY,
     course_name VARCHAR(255),
     instructor_id INT,
     course_description VARCHAR(255),
     created_by VARCHAR(255),
     modified_by VARCHAR(255),
     is_deleted BIT(1) NOT NULL,
     deleted_by VARCHAR(255),
     restored_by VARCHAR(255),
     avg_rating DOUBLE,
     feedback_count BIGINT,
     deleted_at DATETIME(6),
     CONSTRAINT fk_course_instructor
         FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id)
);

CREATE TABLE enrollment (
    enroll_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_date DATE,
    course_id INT,
    student_id INT,
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES course(course_id),
    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES users(user_id)
);


CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    anonymous BIT(1) NOT NULL,
    course_comment VARCHAR(1000),
    course_rating INT NOT NULL,
    instructor_comment VARCHAR(1000),
    instructor_rating INT NOT NULL,
    submitted_at DATE,
    course_id INT,
    student_id INT,
    instructor_id INT NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(255),
    restored_by VARCHAR(255),
    CONSTRAINT fk_feedback_course
        FOREIGN KEY (course_id) REFERENCES course(course_id),
    CONSTRAINT fk_feedback_student
        FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_feedback_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id)
);

CREATE TABLE refresh_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    replaced_at DATETIME(6),
    revoked BIT(1) NOT NULL,
    token VARCHAR(300) NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT uk_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);