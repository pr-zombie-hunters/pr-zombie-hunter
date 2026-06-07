CREATE TABLE repositories (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner   VARCHAR(100) NOT NULL,
    name    VARCHAR(100) NOT NULL,
    sector  VARCHAR(50)  NOT NULL DEFAULT 'DEFAULT',
    tracked BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_repo (owner, name)
);

CREATE TABLE pull_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    github_pr_id        BIGINT       NOT NULL,
    title               VARCHAR(255) NOT NULL,
    author              VARCHAR(100) NOT NULL,
    repository_id       BIGINT       NOT NULL,
    url                 VARCHAR(500) NOT NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    grade               VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    requested_reviewers INT          NOT NULL DEFAULT 0,
    completed_reviews   INT          NOT NULL DEFAULT 0,
    notified_at         DATETIME,
    UNIQUE KEY uq_pr (repository_id, github_pr_id),
    CONSTRAINT fk_pr_repository FOREIGN KEY (repository_id) REFERENCES repositories (id)
);
