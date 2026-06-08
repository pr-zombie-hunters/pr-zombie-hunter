CREATE TABLE hunter_action (
    id          BIGINT AUTO_INCREMENT NOT NULL,
    pr_id       VARCHAR(50)  NOT NULL,   -- grader.pull_request.id 소프트 참조 (FK 없음 — 모듈 기동 순서 무관)
    hunter_id   VARCHAR(100) NOT NULL,
    action_type VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
