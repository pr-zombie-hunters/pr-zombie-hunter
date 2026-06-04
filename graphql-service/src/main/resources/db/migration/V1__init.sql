CREATE TABLE hunter_action (
    id BIGINT AUTO_INCREMENT NOT NULL,
    pr_id VARCHAR(50) NOT NULL,
    hunter_id VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_hunter_action_pr FOREIGN KEY (pr_id) REFERENCES pull_request (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
