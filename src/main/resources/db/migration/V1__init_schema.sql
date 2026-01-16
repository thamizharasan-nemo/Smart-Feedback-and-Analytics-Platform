-- Initial baseline migration
CREATE TABLE `user` (
   `user_id` int NOT NULL AUTO_INCREMENT,
   `email` varchar(255) DEFAULT NULL,
   `password` varchar(255) DEFAULT NULL,
   `role` enum('ADMIN','STUDENT') DEFAULT NULL,
   `username` varchar(255) DEFAULT NULL,
   `roll_no` varchar(255) DEFAULT NULL,
   `created_at` date DEFAULT NULL,
   `identity_no` varchar(255) DEFAULT NULL,
   PRIMARY KEY (`user_id`),
   UNIQUE KEY `UK5n7u2fry0slhlo865642lxqp1` (`roll_no`),
   UNIQUE KEY `UK6dwb0rs5b9jaxjy5ulsn5aytq` (`identity_no`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

 CREATE TABLE `course` (
    `course_id` int NOT NULL AUTO_INCREMENT,
    `course_name` varchar(255) DEFAULT NULL,
    `instructor_id` int DEFAULT NULL,
    `course_description` varchar(255) DEFAULT NULL,
    `created_by` varchar(255) DEFAULT NULL,
    `modified_by` varchar(255) DEFAULT NULL,
    `is_deleted` bit(1) NOT NULL,
    `deleted_by` varchar(255) DEFAULT NULL,
    `restored_by` varchar(255) DEFAULT NULL,
    `avg_rating` double DEFAULT NULL,
    `feedback_count` bigint DEFAULT NULL,
    `deleted_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`course_id`),
    KEY `FKqk2yq2yk124dhlsilomy36qr9` (`instructor_id`),
    CONSTRAINT `FKqk2yq2yk124dhlsilomy36qr9` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

  CREATE TABLE `enrollment` (
     `enroll_id` int NOT NULL AUTO_INCREMENT,
     `enrollment_date` date DEFAULT NULL,
     `course_course_id` int DEFAULT NULL,
     `student_user_id` int DEFAULT NULL,
     PRIMARY KEY (`enroll_id`),
     KEY `FKsrohxsncebva8ssubg604hh7c` (`course_course_id`),
     KEY `FKol08mf7ybydnd5iy6o47d8ngs` (`student_user_id`),
     CONSTRAINT `FKol08mf7ybydnd5iy6o47d8ngs` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`user_id`),
     CONSTRAINT `FKsrohxsncebva8ssubg604hh7c` FOREIGN KEY (`course_course_id`) REFERENCES `course` (`course_id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

   CREATE TABLE `instructor` (
      `instructor_id` int NOT NULL AUTO_INCREMENT,
      `instructor_name` varchar(255) DEFAULT NULL,
      `avg_rating` double NOT NULL,
      `feedback_count` bigint NOT NULL,
      `is_deleted` bit(1) DEFAULT NULL,
      `deleted_by` varchar(255) DEFAULT NULL,
      `restored_by` varchar(255) DEFAULT NULL,
      `deleted_at` datetime(6) DEFAULT NULL,
      PRIMARY KEY (`instructor_id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci


CREATE TABLE `feedback` (
   `feedback_id` int NOT NULL AUTO_INCREMENT,
   `anonymous` bit(1) NOT NULL,
   `course_comment` varchar(1000) DEFAULT NULL,
   `course_rating` int NOT NULL,
   `instructor_comment` varchar(1000) DEFAULT NULL,
   `instructor_rating` int NOT NULL,
   `submitted_at` date DEFAULT NULL,
   `course_id` int DEFAULT NULL,
   `student_id` int DEFAULT NULL,
   `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
   `instructor_id` int NOT NULL,
   `deleted_at` datetime(6) DEFAULT NULL,
   `deleted_by` varchar(255) DEFAULT NULL,
   `restored_by` varchar(255) DEFAULT NULL,
   PRIMARY KEY (`feedback_id`),
   KEY `FKwpf206pf27bxt57sgtyn5sqk` (`student_id`),
   KEY `idx_feedback_submitted_at` (`submitted_at`),
   KEY `idx_feedback_course_rating` (`course_rating`),
   KEY `idx_feedback_instructor_rating` (`instructor_rating`),
   KEY `idx_feedback_course_submitted` (`course_id`,`submitted_at`),
   KEY `idx_feedback_instructor` (`instructor_id`),
   KEY `idx_feedback_course` (`course_id`),
   CONSTRAINT `FKj74d0lvnfn5cjm4ext74dobr3` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`),
   CONSTRAINT `FKko7f08v61t5y67teh5jxxwrea` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
   CONSTRAINT `FKwpf206pf27bxt57sgtyn5sqk` FOREIGN KEY (`student_id`) REFERENCES `user` (`user_id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

    CREATE TABLE `refresh_tokens` (
       `id` int NOT NULL AUTO_INCREMENT,
       `created_at` datetime(6) NOT NULL,
       `expires_at` datetime(6) NOT NULL,
       `replaced_at` datetime(6) DEFAULT NULL,
       `revoked` bit(1) NOT NULL,
       `token` varchar(300) NOT NULL,
       `user_id` int NOT NULL,
       PRIMARY KEY (`id`),
       UNIQUE KEY `UKghpmfn23vmxfu3spu3lfg4r2d` (`token`),
       KEY `IDX7tdcd6ab5wsgoudnvj7xf1b7l` (`user_id`),
       CONSTRAINT `FKjwc9veyjcjfkej6rnnbsijfvh` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
     ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci