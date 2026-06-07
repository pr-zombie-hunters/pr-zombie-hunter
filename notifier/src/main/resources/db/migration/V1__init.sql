CREATE TABLE notifications (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pull_request_id  VARCHAR(50)  NOT NULL,   -- grader.pull_request.id와 타입 통일
    recipient_email  VARCHAR(255) NOT NULL,
    grade            VARCHAR(20)  NOT NULL,
    sent_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
