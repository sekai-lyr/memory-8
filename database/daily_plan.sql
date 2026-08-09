USE sekai_friend;

CREATE TABLE IF NOT EXISTS sekai_word_pro_daily_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_date DATE NOT NULL,
    task_key VARCHAR(60) NOT NULL,
    title VARCHAR(100) NOT NULL,
    detail VARCHAR(500) NOT NULL,
    route VARCHAR(100) NOT NULL,
    target_count INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_time DATETIME,
    sort_order INT NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_task (user_id, task_date, task_key),
    INDEX idx_daily_task_user_date (user_id, task_date),
    CONSTRAINT fk_daily_tasks_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
