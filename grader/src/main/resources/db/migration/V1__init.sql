CREATE TABLE pull_request (
    id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    last_activity_at TIMESTAMP NOT NULL,
    zombie_grade VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE zombie_grade_history (
    id BIGINT AUTO_INCREMENT NOT NULL,
    pr_id VARCHAR(50) NOT NULL,
    from_grade VARCHAR(20) NOT NULL,
    to_grade VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_zombie_grade_pr FOREIGN KEY (pr_id) REFERENCES pull_request (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
