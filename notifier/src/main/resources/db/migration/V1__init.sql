CREATE TABLE notifications (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pull_request_id  BIGINT       NOT NULL,
    recipient_email  VARCHAR(255) NOT NULL,
    grade            VARCHAR(20)  NOT NULL,
    sent_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
