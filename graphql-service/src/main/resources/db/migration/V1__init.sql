CREATE TABLE hunter_actions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pr_id        BIGINT       NOT NULL,
    hunter_name  VARCHAR(100) NOT NULL,
    hunted_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
