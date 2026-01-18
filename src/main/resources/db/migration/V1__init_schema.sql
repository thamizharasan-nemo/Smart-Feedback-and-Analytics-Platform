-- Initial baseline migration
CREATE TABLE `users` (
   `user_id` INT NOT NULL AUTO_INCREMENT,
   `email` VARCHAR(255) DEFAULT NULL,
   `password` VARCHAR(255) DEFAULT NULL,
   `role` VARCHAR(20) NOT NULL,
   `username` VARCHAR(255) DEFAULT NULL,
   `roll_no` VARCHAR(255) DEFAULT NULL,
   `created_at` DATE DEFAULT NULL,
   `identity_no` VARCHAR(255) DEFAULT NULL,
   PRIMARY KEY (`user_id`),
   UNIQUE KEY `UK_user_roll_no` (`roll_no`),
   UNIQUE KEY `UK_user_identity_no` (`identity_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `instructor` (
   `instructor_id` INT NOT NULL AUTO_INCREMENT,
   `instructor_name` VARCHAR(255),
   `avg_rating` DOUBLE NOT NULL,
   `feedback_count` BIGINT NOT NULL,
   `is_deleted` BIT(1),
   `deleted_by` VARCHAR(255),
   `restored_by` VARCHAR(255),
   `deleted_at` DATETIME(6),
   PRIMARY KEY (`instructor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `course` (
   `course_id` INT NOT NULL AUTO_INCREMENT,
   `course_name` VARCHAR(255),
   `course_description` VARCHAR(255),
   `instructor_id` INT,
   `created_by` VARCHAR(255),
   `modified_by` VARCHAR(255),
   `is_deleted` BIT(1) NOT NULL,
   `deleted_by` VARCHAR(255),
   `restored_by` VARCHAR(255),
   `avg_rating` DOUBLE,
   `feedback_count` BIGINT,
   `deleted_at` DATETIME(6),
   PRIMARY KEY (`course_id`),
   CONSTRAINT `FK_course_instructor`
       FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enrollment` (
   `enroll_id` int NOT NULL AUTO_INCREMENT,
   `enrollment_date` date DEFAULT NULL,
   `course_course_id` int DEFAULT NULL,
   `student_user_id` int DEFAULT NULL,
   PRIMARY KEY (`enroll_id`),
   KEY `FK_course` (`course_course_id`),
   KEY `FK_student` (`student_user_id`),
   CONSTRAINT `FK_student`
       FOREIGN KEY (`student_user_id`) REFERENCES `users` (`user_id`),
   CONSTRAINT `FK_course`
       FOREIGN KEY (`course_course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `feedback` (
   `feedback_id` INT NOT NULL AUTO_INCREMENT,
   `anonymous` BIT(1) NOT NULL,
   `course_comment` VARCHAR(1000),
   `course_rating` INT NOT NULL,
   `instructor_comment` VARCHAR(1000),
   `instructor_rating` INT NOT NULL,
   `submitted_at` DATE,
   `course_id` INT,
   `student_id` INT,
   `instructor_id` INT NOT NULL,
   `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
   `deleted_at` DATETIME(6),
   `deleted_by` VARCHAR(255),
   `restored_by` VARCHAR(255),
   PRIMARY KEY (`feedback_id`),

   CONSTRAINT `FK_feedback_course`
       FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),

   CONSTRAINT `FK_feedback_student`
       FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`),

   CONSTRAINT `FK_feedback_instructor`
       FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refresh_tokens` (
   `id` INT NOT NULL AUTO_INCREMENT,
   `token` VARCHAR(300) NOT NULL,
   `created_at` DATETIME(6) NOT NULL,
   `expires_at` DATETIME(6) NOT NULL,
   `replaced_at` DATETIME(6),
   `revoked` BIT(1) NOT NULL,
   `user_id` INT NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `UK_refresh_token` (`token`),
   CONSTRAINT `FK_refresh_user`
       FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;